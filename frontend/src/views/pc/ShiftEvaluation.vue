<template>
  <div class="shift-evaluation-page">
    <el-card class="mb-4 shadow-sm rounded-lg border-0" :body-style="{ padding: '24px' }">
      <template #header>
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <el-button 
              @click="goBack"
              class="rounded-lg"
            >
              <el-icon><ArrowLeft /></el-icon>
              返回
            </el-button>
            <span class="text-xl font-bold text-gray-800">班后评价</span>
          </div>
          <el-button 
            type="primary" 
            @click="handleSubmit"
            class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200"
            :loading="submitting"
            :disabled="!hasChanges"
          >
            <el-icon><Check /></el-icon>
            提交评价
          </el-button>
        </div>
      </template>

      <div v-if="shiftDetail" class="shift-info mb-6">
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="班次名称">{{ shiftDetail.shiftName }}</el-descriptions-item>
          <el-descriptions-item label="排班日期">{{ shiftDetail.shiftDate }}</el-descriptions-item>
          <el-descriptions-item label="班次类型">
            {{ shiftDetail.shiftType === 'night' ? '夜班' : '白班' }}
          </el-descriptions-item>
          <el-descriptions-item label="时间">
            {{ shiftDetail.startTime || '-' }} ~ {{ shiftDetail.endTime || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(shiftDetail.status)" size="small">
              {{ getStatusText(shiftDetail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="成员数">{{ shiftDetail.members?.length || 0 }}人</el-descriptions-item>
        </el-descriptions>
      </div>

      <el-alert 
        title="评价说明" 
        type="info" 
        :closable="false"
        show-icon
        class="mb-6"
      >
        <template #default>
          <div class="text-sm">
            <p>• 班后评价由主管进行，工人无法修改自己的技能熟练度</p>
            <p>• 熟练度调整：+1 表示进步，0 表示保持，-1 表示退步</p>
            <p>• 评价提交后会自动更新工人的技能熟练度</p>
          </div>
        </template>
      </el-alert>

      <div v-loading="loading" class="evaluation-list">
        <div 
          v-for="member in evaluationList" 
          :key="member.userId"
          class="member-evaluation-card mb-6"
        >
          <div class="member-header flex items-center justify-between mb-4">
            <div class="flex items-center gap-3">
              <el-avatar :size="40" class="bg-blue-500">
                {{ member.nickname?.charAt(0) }}
              </el-avatar>
              <div>
                <span class="text-lg font-medium text-gray-800">{{ member.nickname }}</span>
                <span class="text-sm text-gray-500 ml-2">{{ member.position || '工人' }}</span>
              </div>
            </div>
            <el-rate 
              v-model="member.overallScore" 
              :max="5"
              show-text
              class="small-rate"
            />
          </div>

          <div class="skill-evaluations">
            <el-table 
              :data="member.skills.filter(s => s.hasSkill)" 
              border
              size="small"
            >
              <el-table-column prop="skillName" label="技能" width="120">
                <template #default="{ row }">
                  <div class="flex items-center gap-2">
                    <span>{{ row.skillName }}</span>
                    <el-tag 
                      v-if="row.skillType === 'key'" 
                      type="danger" 
                      size="small"
                    >
                      关键
                    </el-tag>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="当前熟练度" width="200" align="center">
                <template #default="{ row }">
                  <div class="current-proficiency">
                    <el-progress 
                      :percentage="row.proficiency * 20" 
                      :show-text="false"
                      :stroke-width="10"
                      class="inline-block w-24"
                    />
                    <span class="ml-2 text-sm">
                      {{ getProficiencyText(row.proficiency) }}
                    </span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="熟练度调整" width="200" align="center">
                <template #default="{ row }">
                  <el-radio-group v-model="row.proficiencyChange" size="small">
                    <el-radio-button :label="-1">退步 -1</el-radio-button>
                    <el-radio-button :label="0">保持 0</el-radio-button>
                    <el-radio-button :label="1">进步 +1</el-radio-button>
                  </el-radio-group>
                </template>
              </el-table-column>
              <el-table-column label="评价备注" min-width="200">
                <template #default="{ row }">
                  <el-input 
                    v-model="row.comment" 
                    placeholder="请输入评价备注"
                    size="small"
                    type="textarea"
                    :rows="2"
                  />
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="member-comment mt-4">
            <el-input
              v-model="member.overallComment"
              placeholder="整体评价（选填）"
              type="textarea"
              :rows="2"
            />
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Check } from '@element-plus/icons-vue'
import { shiftApi, shiftEvaluationApi } from '@/api/shift'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const shiftDetail = ref(null)
const evaluationList = ref([])
const originalEvaluations = ref([])

const shiftId = computed(() => route.params.id)

const hasChanges = computed(() => {
  return evaluationList.value.some(member => {
    const original = originalEvaluations.value.find(o => o.userId === member.userId)
    if (!original) return member.overallScore || member.overallComment || 
      member.skills.some(s => s.proficiencyChange !== 0 || s.comment)
    return member.overallScore !== original.overallScore ||
      member.overallComment !== original.overallComment ||
      member.skills.some(s => {
        const origSkill = original.skills.find(os => os.skillId === s.skillId)
        if (!origSkill) return s.proficiencyChange !== 0 || s.comment
        return s.proficiencyChange !== origSkill.proficiencyChange || 
               s.comment !== origSkill.comment
      })
  })
})

const loadShiftDetail = async () => {
  loading.value = true
  try {
    const res = await shiftApi.getDetail(shiftId.value)
    if (res.code === 200) {
      shiftDetail.value = res.data
      initEvaluationList(res.data.members || [])
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('加载排班信息失败')
  } finally {
    loading.value = false
  }
}

const initEvaluationList = (members) => {
  evaluationList.value = members.map(member => ({
    userId: member.userId,
    nickname: member.nickname,
    position: member.position,
    overallScore: 3,
    overallComment: '',
    skills: member.skills.filter(s => s.hasSkill).map(skill => ({
      ...skill,
      proficiencyChange: 0,
      comment: '',
      originalProficiency: skill.proficiency
    }))
  }))
  
  originalEvaluations.value = JSON.parse(JSON.stringify(evaluationList.value))
}

const getProficiencyText = (level) => {
  const texts = {
    1: '入门',
    2: '初级',
    3: '中级',
    4: '高级',
    5: '精通'
  }
  return texts[level] || '-'
}

const getStatusType = (status) => {
  const types = {
    draft: 'info',
    published: 'success',
    completed: 'warning'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    draft: '草稿',
    published: '已发布',
    completed: '已完成'
  }
  return texts[status] || status
}

const handleSubmit = async () => {
  const evaluations = []
  for (const member of evaluationList.value) {
    for (const skill of member.skills) {
      if (skill.proficiencyChange !== 0 || skill.comment) {
        evaluations.push({
          userId: member.userId,
          skillId: skill.skillId,
          proficiencyChange: skill.proficiencyChange,
          score: member.overallScore,
          comment: skill.comment || member.overallComment
        })
      }
    }
  }

  if (evaluations.length === 0) {
    ElMessage.warning('请至少填写一项评价')
    return
  }

  submitting.value = true
  try {
    const res = await shiftEvaluationApi.batchEvaluate(shiftId.value, {
      evaluations,
      evaluatorId: 1
    })
    if (res.code === 200) {
      ElMessage.success('评价提交成功，技能熟练度已更新')
      await shiftApi.complete(shiftId.value, 1)
      router.push('/pc/shift')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('评价提交失败')
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadShiftDetail()
})
</script>

<style scoped>
.shift-evaluation-page {
  max-width: 1200px;
  margin: 0 auto;
}

.member-evaluation-card {
  padding: 20px;
  background: #fafafa;
  border-radius: 12px;
  border: 1px solid #ebeef5;
}

.member-header {
  padding-bottom: 12px;
  border-bottom: 1px dashed #dcdfe6;
}

.current-proficiency {
  display: flex;
  align-items: center;
  justify-content: center;
}

.small-rate {
  --el-rate-icon-size: 16px;
}

.member-comment {
  padding-top: 12px;
  border-top: 1px dashed #dcdfe6;
}

:deep(.el-card) {
  border-radius: 12px;
}

:deep(.el-table) {
  border-radius: 8px;
}

:deep(.el-button) {
  border-radius: 6px;
}

:deep(.el-input__wrapper) {
  border-radius: 6px;
}
</style>
