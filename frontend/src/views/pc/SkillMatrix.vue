<template>
  <div class="skill-matrix-page">
    <el-card class="mb-4 shadow-sm rounded-lg border-0" :body-style="{ padding: '24px' }">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-xl font-bold text-gray-800">技能矩阵</span>
          <el-button 
            type="warning" 
            @click="showRiskWarning"
            class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200"
          >
            <el-icon><Warning /></el-icon>
            风险预警
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="mb-6">
        <el-form-item label="工人姓名">
          <el-input 
            v-model="searchForm.nickname" 
            placeholder="请输入工人姓名" 
            clearable
            class="rounded-lg"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button 
            type="primary" 
            @click="handleSearch"
            class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200"
          >
            查询
          </el-button>
          <el-button 
            @click="handleReset"
            class="rounded-lg"
          >
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <div class="risk-banner mb-4" v-if="hasRisk">
        <el-alert 
          title="替补风险预警" 
          type="warning" 
          :closable="false"
          show-icon
        >
          <template #default>
            <div class="risk-items">
              <span v-for="risk in criticalRisks" :key="risk.skillId" class="risk-item">
                <el-tag type="danger" size="small">{{ risk.skillName }}: {{ risk.userCount }}人</el-tag>
              </span>
            </div>
          </template>
        </el-alert>
      </div>

      <el-table 
        :data="tableData" 
        v-loading="loading" 
        border
        class="rounded-lg overflow-hidden"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
      >
        <el-table-column prop="nickname" label="工人姓名" width="120" fixed="left" />
        <el-table-column 
          v-for="skill in skills" 
          :key="skill.id" 
          :label="skill.skillName"
          :min-width="150"
          align="center"
        >
          <template #default="{ row }">
            <div class="skill-cell">
              <template v-if="getSkillDetail(row, skill.id)?.hasSkill">
                <div class="proficiency-bar">
                  <div 
                    class="proficiency-fill"
                    :class="getProficiencyClass(getSkillDetail(row, skill.id)?.proficiency)"
                    :style="{ width: (getSkillDetail(row, skill.id)?.proficiency || 0) * 20 + '%' }"
                  ></div>
                </div>
                <span class="proficiency-text">
                  {{ getProficiencyText(getSkillDetail(row, skill.id)?.proficiency) }}
                </span>
                <el-tag 
                  v-if="getSkillDetail(row, skill.id)?.certified === 1" 
                  type="success" 
                  size="small"
                  class="ml-1"
                >
                  持证
                </el-tag>
              </template>
              <template v-else>
                <span class="text-gray-400">未掌握</span>
              </template>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              link 
              @click="handleEditSkill(row)"
              class="hover:text-blue-600 transition-colors duration-200"
            >
              编辑技能
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-6 flex justify-end">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          class="rounded-lg"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog 
      v-model="editDialogVisible" 
      title="编辑工人技能" 
      width="500px"
      class="rounded-lg"
      :close-on-click-modal="false"
    >
      <div class="skill-edit-form">
        <div class="mb-4">
          <span class="font-medium">工人：</span>
          <span>{{ currentWorker?.nickname }}</span>
        </div>
        <div v-for="skill in skills" :key="skill.id" class="skill-edit-item">
          <div class="skill-edit-header">
            <span class="font-medium">{{ skill.skillName }}</span>
            <el-tag v-if="skill.skillType === 'key'" type="danger" size="small">关键技能</el-tag>
          </div>
          <div class="skill-edit-body">
            <el-form label-width="80px">
              <el-form-item label="熟练度">
                <el-rate 
                  v-model="skillEditMap[skill.id].proficiency" 
                  :max="5"
                  show-text
                  :texts="['入门', '初级', '中级', '高级', '精通']"
                />
              </el-form-item>
              <el-form-item label="持证">
                <el-switch v-model="skillEditMap[skill.id].certified" />
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="editDialogVisible = false" class="rounded-lg">取消</el-button>
          <el-button 
            type="primary" 
            @click="handleSaveSkill"
            class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200"
            :loading="saving"
          >
            保存
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog 
      v-model="riskDialogVisible" 
      title="技能风险预警" 
      width="600px"
      class="rounded-lg"
    >
      <el-table :data="riskList" border>
        <el-table-column prop="skillName" label="技能名称" width="120" />
        <el-table-column prop="skillType" label="技能类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.skillType === 'key' ? 'danger' : 'info'" size="small">
              {{ row.skillType === 'key' ? '关键' : '普通' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userCount" label="掌握人数" width="100" align="center" />
        <el-table-column prop="certifiedCount" label="持证人数" width="100" align="center" />
        <el-table-column prop="riskLevel" label="风险等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getRiskType(row.riskLevel)" size="small">
              {{ getRiskText(row.riskLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="riskMessage" label="风险说明" min-width="200" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'
import { userSkillApi, skillApi } from '@/api/skill'

const loading = ref(false)
const saving = ref(false)
const tableData = ref([])
const skills = ref([])
const riskList = ref([])

const editDialogVisible = ref(false)
const riskDialogVisible = ref(false)
const currentWorker = ref(null)
const skillEditMap = reactive({})

const searchForm = reactive({
  nickname: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const criticalRisks = computed(() => {
  return riskList.value.filter(r => r.isRisk && r.riskLevel === 'critical')
})

const hasRisk = computed(() => {
  return riskList.value.some(r => r.isRisk)
})

const loadSkills = async () => {
  try {
    const res = await skillApi.list()
    if (res.code === 200) {
      skills.value = res.data
    }
  } catch (error) {
    console.error(error)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await userSkillApi.getMatrixPage({
      current: pagination.current,
      size: pagination.size,
      nickname: searchForm.nickname
    })
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadRiskWarning = async () => {
  try {
    const res = await userSkillApi.getRiskWarning()
    if (res.code === 200) {
      riskList.value = res.data
    }
  } catch (error) {
    console.error(error)
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  searchForm.nickname = ''
  handleSearch()
}

const getSkillDetail = (row, skillId) => {
  if (!row.skills) return null
  return row.skills.find(s => s.skillId === skillId)
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

const getProficiencyClass = (level) => {
  if (level >= 4) return 'high'
  if (level >= 3) return 'medium'
  return 'low'
}

const handleEditSkill = (row) => {
  currentWorker.value = row
  for (const skill of skills.value) {
    const detail = getSkillDetail(row, skill.id)
    skillEditMap[skill.id] = {
      proficiency: detail?.proficiency || 0,
      certified: detail?.certified === 1
    }
  }
  editDialogVisible.value = true
}

const handleSaveSkill = async () => {
  saving.value = true
  try {
    for (const skill of skills.value) {
      const edit = skillEditMap[skill.id]
      if (edit.proficiency > 0) {
        await userSkillApi.save({
          userId: currentWorker.value.userId,
          skillId: skill.id,
          proficiency: edit.proficiency,
          certified: edit.certified ? 1 : 0
        })
      }
    }
    ElMessage.success('保存成功')
    editDialogVisible.value = false
    loadData()
    loadRiskWarning()
  } catch (error) {
    console.error(error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const showRiskWarning = () => {
  riskDialogVisible.value = true
}

const getRiskType = (level) => {
  const types = {
    critical: 'danger',
    warning: 'warning',
    normal: 'success'
  }
  return types[level] || 'info'
}

const getRiskText = (level) => {
  const texts = {
    critical: '严重',
    warning: '警告',
    normal: '正常'
  }
  return texts[level] || '正常'
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadData()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadData()
}

onMounted(() => {
  loadSkills()
  loadData()
  loadRiskWarning()
})
</script>

<style scoped>
.skill-matrix-page {
  max-width: 1400px;
  margin: 0 auto;
}

.skill-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.proficiency-bar {
  width: 80px;
  height: 8px;
  background: #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
}

.proficiency-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s;
}

.proficiency-fill.low {
  background: #f56c6c;
}

.proficiency-fill.medium {
  background: #e6a23c;
}

.proficiency-fill.high {
  background: #67c23a;
}

.proficiency-text {
  font-size: 12px;
  color: #606266;
}

.skill-edit-item {
  margin-bottom: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.skill-edit-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.risk-banner {
  margin-bottom: 16px;
}

.risk-items {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.risk-item {
  display: inline-block;
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

:deep(.el-dialog) {
  border-radius: 12px;
}

:deep(.el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-dialog__body) {
  padding: 24px;
}
</style>
