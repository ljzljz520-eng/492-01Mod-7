<template>
  <div class="shift-page">
    <el-card class="mb-4 shadow-sm rounded-lg border-0" :body-style="{ padding: '24px' }">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-xl font-bold text-gray-800">排班管理</span>
          <el-button 
            type="primary" 
            @click="handleAdd"
            class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200"
          >
            <el-icon><Plus /></el-icon>
            新建排班
          </el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="mb-6">
        <el-form-item label="班次名称">
          <el-input 
            v-model="searchForm.shiftName" 
            placeholder="请输入班次名称" 
            clearable
            class="rounded-lg"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select 
            v-model="searchForm.status" 
            placeholder="请选择状态" 
            clearable 
            style="width: 150px"
            class="rounded-lg"
          >
            <el-option label="草稿" value="draft" />
            <el-option label="已发布" value="published" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            class="rounded-lg"
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

      <el-table 
        :data="tableData" 
        v-loading="loading" 
        border
        class="rounded-lg overflow-hidden"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
      >
        <el-table-column prop="shiftName" label="班次名称" min-width="150" />
        <el-table-column prop="shiftDate" label="排班日期" width="120" />
        <el-table-column prop="shiftType" label="班次类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.shiftType === 'night' ? 'warning' : 'success'" size="small">
              {{ row.shiftType === 'night' ? '夜班' : '白班' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="150">
          <template #default="{ row }">
            {{ row.startTime || '-' }} ~ {{ row.endTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              link 
              @click="handleView(row)"
              class="hover:text-blue-600 transition-colors duration-200"
            >
              详情
            </el-button>
            <el-button 
              type="success" 
              size="small" 
              link 
              @click="handleEdit(row)"
              class="hover:text-green-600 transition-colors duration-200"
              v-if="row.status === 'draft'"
            >
              编辑
            </el-button>
            <el-button 
              type="warning" 
              size="small" 
              link 
              @click="handlePublish(row)"
              class="hover:text-yellow-600 transition-colors duration-200"
              v-if="row.status === 'draft'"
            >
              发布
            </el-button>
            <el-button 
              type="success" 
              size="small" 
              link 
              @click="handleEvaluate(row)"
              class="hover:text-green-600 transition-colors duration-200"
              v-if="row.status === 'published'"
            >
              班后评价
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              link 
              @click="handleDelete(row)"
              class="hover:text-red-600 transition-colors duration-200"
            >
              删除
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
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="900px"
      class="rounded-lg"
      :close-on-click-modal="false"
    >
      <el-form 
        :model="formData" 
        :rules="formRules" 
        ref="formRef"
        label-width="100px"
        class="px-2"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="班次名称" prop="shiftName">
              <el-input 
                v-model="formData.shiftName" 
                placeholder="请输入班次名称"
                class="rounded-lg"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排班日期" prop="shiftDate">
              <el-date-picker
                v-model="formData.shiftDate"
                type="date"
                placeholder="请选择排班日期"
                class="w-full rounded-lg"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="班次类型" prop="shiftType">
              <el-radio-group v-model="formData.shiftType">
                <el-radio value="day">白班</el-radio>
                <el-radio value="night">夜班</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="时间">
              <el-time-picker
                v-model="formData.startTime"
                placeholder="开始时间"
                value-format="HH:mm:ss"
                style="width: 45%"
              />
              <span class="mx-2">-</span>
              <el-time-picker
                v-model="formData.endTime"
                placeholder="结束时间"
                value-format="HH:mm:ss"
                style="width: 45%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input 
            v-model="formData.remark" 
            type="textarea" 
            :rows="2"
            placeholder="请输入备注"
            class="rounded-lg"
          />
        </el-form-item>
      </el-form>

      <div class="skill-recommend-section mt-6">
        <div class="flex items-center justify-between mb-4">
          <span class="text-lg font-medium text-gray-800">排班成员</span>
          <div class="flex items-center gap-3">
            <el-select 
              v-model="selectedSkills" 
              multiple 
              placeholder="选择所需技能"
              style="width: 300px"
              @change="handleSkillChange"
            >
              <el-option 
                v-for="skill in skills" 
                :key="skill.id" 
                :label="skill.skillName + (skill.skillType === 'key' ? ' (关键)' : '')" 
                :value="skill.id"
              />
            </el-select>
            <el-button 
              type="primary" 
              size="small"
              @click="handleRecommend"
              :disabled="selectedSkills.length === 0"
            >
              <el-icon><MagicStick /></el-icon>
              智能推荐
            </el-button>
          </div>
        </div>

        <div class="risk-tip mb-4" v-if="shiftRiskList.length > 0">
          <el-alert 
            title="替补风险提醒" 
            type="warning" 
            :closable="false"
            show-icon
          >
            <template #default>
              <div class="risk-skills">
                <span v-for="risk in shiftRiskList" :key="risk.skillId" class="risk-skill">
                  <el-tag type="danger" size="small">
                    {{ risk.skillName }}: {{ risk.userCount }}人
                  </el-tag>
                </span>
                <span class="text-sm text-gray-600 ml-2">
                  以上关键技能人手不足，建议增加备份人员
                </span>
              </div>
            </template>
          </el-alert>
        </div>

        <div class="recommend-list mb-4" v-if="recommendList.length > 0">
          <div class="text-sm text-gray-600 mb-2">
            推荐人员（按匹配度排序）：
          </div>
          <div class="flex flex-wrap gap-2">
            <div 
              v-for="worker in recommendList" 
              :key="worker.userId"
              class="recommend-card"
              :class="{ 'selected': selectedUserIds.includes(worker.userId) }"
              @click="toggleRecommendWorker(worker)"
            >
              <div class="flex items-center justify-between">
                <span class="font-medium">{{ worker.nickname }}</span>
                <el-tag size="small" type="success">匹配度: {{ worker.matchScore }}</el-tag>
              </div>
              <div class="text-xs text-gray-500 mt-1">
                <span v-for="skill in worker.skills" :key="skill.skillId" class="mr-2">
                  {{ skill.skillName }}: Lv.{{ skill.proficiency }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <div class="selected-members">
          <div class="text-sm text-gray-600 mb-2">
            已选成员 ({{ selectedUserIds.length }}人):
          </div>
          <el-tag 
            v-for="userId in selectedUserIds" 
            :key="userId"
            closable
            type="primary"
            class="mr-2 mb-2"
            @close="removeMember(userId)"
          >
            {{ getUserName(userId) }}
          </el-tag>
          <el-button 
            v-if="selectedUserIds.length === 0"
            type="primary" 
            link
            size="small"
            @click="workerDialogVisible = true"
          >
            添加成员
          </el-button>
        </div>
      </div>

      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button 
            @click="dialogVisible = false"
            class="rounded-lg"
          >
            取消
          </el-button>
          <el-button 
            type="primary" 
            @click="handleSubmit"
            class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200"
            :loading="submitting"
          >
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog 
      v-model="workerDialogVisible" 
      title="选择工人" 
      width="700px"
      class="rounded-lg"
    >
      <el-table 
        :data="workerList" 
        border
        @selection-change="handleWorkerSelectionChange"
        ref="workerTableRef"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="nickname" label="姓名" width="100" />
        <el-table-column label="技能">
          <template #default="{ row }">
            <el-tag 
              v-for="skill in row.skills" 
              :key="skill.skillId"
              size="small"
              class="mr-1 mb-1"
              :type="skill.skillType === 'key' ? 'danger' : 'info'"
              v-if="skill.hasSkill"
            >
              {{ skill.skillName }} Lv.{{ skill.proficiency }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="workerDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmWorkerSelection">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog 
      v-model="detailDialogVisible" 
      title="排班详情" 
      width="800px"
      class="rounded-lg"
    >
      <div v-if="shiftDetail" class="shift-detail">
        <el-descriptions :column="2" border>
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
          <el-descriptions-item label="主管">{{ shiftDetail.supervisorName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ shiftDetail.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div class="mt-6">
          <h4 class="text-lg font-medium mb-4">排班成员 ({{ shiftDetail.members?.length || 0 }}人)</h4>
          <el-table :data="shiftDetail.members || []" border>
            <el-table-column prop="nickname" label="姓名" width="120" />
            <el-table-column prop="position" label="岗位" width="120" />
            <el-table-column label="技能">
              <template #default="{ row }">
                <el-tag 
                  v-for="skill in row.skills" 
                  :key="skill.skillId"
                  size="small"
                  class="mr-1 mb-1"
                  :type="skill.skillType === 'key' ? 'danger' : 'info'"
                  v-if="skill.hasSkill"
                >
                  {{ skill.skillName }} Lv.{{ skill.proficiency }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, MagicStick } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { shiftApi } from '@/api/shift'
import { skillApi, userSkillApi } from '@/api/skill'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const skills = ref([])
const workerList = ref([])
const recommendList = ref([])
const shiftRiskList = ref([])
const shiftDetail = ref(null)

const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const workerDialogVisible = ref(false)
const dialogTitle = ref('新建排班')
const formRef = ref(null)
const workerTableRef = ref(null)

const searchForm = reactive({
  shiftName: '',
  status: '',
  dateRange: []
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const formData = reactive({
  id: null,
  shiftName: '',
  shiftDate: '',
  shiftType: 'day',
  startTime: '',
  endTime: '',
  remark: ''
})

const formRules = {
  shiftName: [
    { required: true, message: '请输入班次名称', trigger: 'blur' }
  ],
  shiftDate: [
    { required: true, message: '请选择排班日期', trigger: 'change' }
  ],
  shiftType: [
    { required: true, message: '请选择班次类型', trigger: 'change' }
  ]
}

const selectedSkills = ref([])
const selectedUserIds = ref([])
const tempSelectedUserIds = ref([])

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

const loadWorkers = async () => {
  try {
    const res = await userSkillApi.getMatrix({})
    if (res.code === 200) {
      workerList.value = res.data
    }
  } catch (error) {
    console.error(error)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      shiftName: searchForm.shiftName,
      status: searchForm.status
    }
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    const res = await shiftApi.page(params)
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

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  searchForm.shiftName = ''
  searchForm.status = ''
  searchForm.dateRange = []
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新建排班'
  Object.assign(formData, {
    id: null,
    shiftName: '',
    shiftDate: '',
    shiftType: 'day',
    startTime: '',
    endTime: '',
    remark: ''
  })
  selectedSkills.value = []
  selectedUserIds.value = []
  recommendList.value = []
  shiftRiskList.value = []
  dialogVisible.value = true
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑排班'
  Object.assign(formData, {
    id: row.id,
    shiftName: row.shiftName,
    shiftDate: row.shiftDate,
    shiftType: row.shiftType,
    startTime: row.startTime,
    endTime: row.endTime,
    remark: row.remark
  })
  selectedUserIds.value = []
  recommendList.value = []
  loadShiftDetail(row.id)
}

const loadShiftDetail = async (id) => {
  try {
    const res = await shiftApi.getDetail(id)
    if (res.code === 200) {
      selectedUserIds.value = (res.data.members || []).map(m => m.userId)
      dialogVisible.value = true
    }
  } catch (error) {
    console.error(error)
  }
}

const handleView = async (row) => {
  try {
    const res = await shiftApi.getDetail(row.id)
    if (res.code === 200) {
      shiftDetail.value = res.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取详情失败')
  }
}

const handleSkillChange = () => {
  recommendList.value = []
}

const handleRecommend = async () => {
  if (selectedSkills.value.length === 0) {
    ElMessage.warning('请先选择所需技能')
    return
  }
  try {
    const res = await shiftApi.recommendMembers(formData.id || 0, {
      skillIds: selectedSkills.value.join(','),
      memberCount: 10
    })
    if (res.code === 200) {
      recommendList.value = res.data
      if (res.data.length === 0) {
        ElMessage.warning('未找到同时具备所有技能的工人，请调整技能要求')
      }
      checkShiftRisk()
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('推荐失败')
  }
}

const checkShiftRisk = async () => {
  try {
    const res = await userSkillApi.getRiskWarning()
    if (res.code === 200) {
      const keySkills = skills.value.filter(s => s.skillType === 'key')
      const selectedSkillIds = selectedSkills.value
      shiftRiskList.value = res.data.filter(r => 
        r.isRisk && selectedSkillIds.includes(r.skillId)
      )
    }
  } catch (error) {
    console.error(error)
  }
}

const toggleRecommendWorker = (worker) => {
  const idx = selectedUserIds.value.indexOf(worker.userId)
  if (idx > -1) {
    selectedUserIds.value.splice(idx, 1)
  } else {
    selectedUserIds.value.push(worker.userId)
  }
}

const getUserName = (userId) => {
  const worker = workerList.value.find(w => w.userId === userId)
  return worker ? worker.nickname : `用户${userId}`
}

const removeMember = (userId) => {
  const idx = selectedUserIds.value.indexOf(userId)
  if (idx > -1) {
    selectedUserIds.value.splice(idx, 1)
  }
}

const handleWorkerSelectionChange = (selection) => {
  tempSelectedUserIds.value = selection.map(w => w.userId)
}

const confirmWorkerSelection = () => {
  const newIds = tempSelectedUserIds.value.filter(id => !selectedUserIds.value.includes(id))
  selectedUserIds.value = [...selectedUserIds.value, ...newIds]
  workerDialogVisible.value = false
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (selectedUserIds.value.length === 0) {
        ElMessage.warning('请至少选择一名排班成员')
        return
      }
      submitting.value = true
      try {
        const data = {
          ...formData,
          userIds: selectedUserIds.value,
          supervisorId: 1
        }
        let res
        if (formData.id) {
          await shiftApi.updateMembers(formData.id, { userIds: selectedUserIds.value })
          res = { code: 200 }
        } else {
          res = await shiftApi.create(data)
        }
        if (res.code === 200) {
          ElMessage.success(formData.id ? '更新成功' : '创建成功')
          dialogVisible.value = false
          loadData()
        }
      } catch (error) {
        console.error(error)
        ElMessage.error(formData.id ? '更新失败' : '创建失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

const handlePublish = async (row) => {
  try {
    await ElMessageBox.confirm('确定要发布该排班吗？', '提示', {
      type: 'warning'
    })
    const res = await shiftApi.publish(row.id, 1)
    if (res.code === 200) {
      ElMessage.success('发布成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('发布失败')
    }
  }
}

const handleEvaluate = (row) => {
  router.push(`/pc/shift-evaluation/${row.id}`)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该排班吗？', '提示', {
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    })
    const res = await shiftApi.delete(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('删除失败')
    }
  }
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
  loadWorkers()
  loadData()
})
</script>

<style scoped>
.shift-page {
  max-width: 1400px;
  margin: 0 auto;
}

.skill-recommend-section {
  border-top: 1px solid #ebeef5;
  padding-top: 20px;
}

.recommend-card {
  padding: 12px 16px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  min-width: 200px;
  background: white;
}

.recommend-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}

.recommend-card.selected {
  border-color: #409eff;
  background: #ecf5ff;
}

.risk-skills {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.risk-skill {
  display: inline-block;
}

.selected-members {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  min-height: 50px;
}

.shift-detail {
  padding: 10px 0;
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
