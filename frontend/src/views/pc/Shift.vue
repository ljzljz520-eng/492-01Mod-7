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
            <el-button 
              type="success" 
              size="small"
              @click="addPositionRequirement"
            >
              <el-icon><Plus /></el-icon>
              添加岗位
            </el-button>
            <el-button 
              type="primary" 
              size="small"
              @click="handleRecommendTeam"
              :disabled="positionRequirements.length === 0"
            >
              <el-icon><MagicStick /></el-icon>
              智能推荐组合
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

        <div class="position-requirements mb-6">
          <div class="text-sm font-medium text-gray-700 mb-3">岗位要求配置：</div>
          <div 
            v-for="(req, index) in positionRequirements" 
            :key="index"
            class="position-requirement-card mb-4"
          >
            <div class="flex items-center justify-between mb-3">
              <div class="flex items-center gap-3">
                <el-input 
                  v-model="req.positionName" 
                  placeholder="岗位名称" 
                  size="small"
                  style="width: 150px"
                />
                <el-input-number 
                  v-model="req.count" 
                  :min="1" 
                  :max="10"
                  size="small"
                  controls-position="right"
                />
                <span class="text-sm text-gray-500">人</span>
                <el-select 
                  v-model="req.minProficiency" 
                  placeholder="最低熟练度"
                  size="small"
                  style="width: 120px"
                >
                  <el-option label="不限" :value="1" />
                  <el-option label="初级以上" :value="2" />
                  <el-option label="中级以上" :value="3" />
                  <el-option label="高级以上" :value="4" />
                </el-select>
              </div>
              <el-button 
                type="danger" 
                size="small"
                link
                @click="removePositionRequirement(index)"
              >
                删除
              </el-button>
            </div>
            <div class="position-skills">
              <span class="text-sm text-gray-500 mr-3">所需技能：</span>
              <el-select 
                v-model="req.requiredSkillIds" 
                multiple 
                placeholder="请选择该岗位所需技能"
                size="small"
                style="width: calc(100% - 100px)"
              >
                <el-option 
                  v-for="skill in skills" 
                  :key="skill.id" 
                  :label="skill.skillName + (skill.skillType === 'key' ? ' (关键)' : '')" 
                  :value="skill.id"
                />
              </el-select>
            </div>
          </div>
          <el-empty 
            v-if="positionRequirements.length === 0" 
            description="点击上方'添加岗位'按钮配置岗位要求"
            :image-size="100"
          />
        </div>

        <div class="recommend-results mb-4" v-if="recommendResults.length > 0">
          <div class="text-sm font-medium text-gray-700 mb-3">
            推荐组合：
          </div>
          <div 
            v-for="(result, index) in recommendResults" 
            :key="index"
            class="recommend-result-card mb-4"
          >
            <div class="flex items-center justify-between mb-3">
              <div class="flex items-center gap-2">
                <span class="font-medium text-gray-800">{{ result.positionName }}</span>
                <el-tag size="small" v-if="result.message" type="danger">
                  {{ result.message }}
                </el-tag>
              </div>
              <el-button 
                type="primary" 
                size="small"
                @click="addRecommendedToSelected(result)"
                :disabled="!result.selected"
              >
                添加推荐人员
              </el-button>
            </div>
            <div v-if="result.candidates && result.candidates.length > 0" class="flex flex-wrap gap-2">
              <div 
                v-for="(worker, wIndex) in result.candidates" 
                :key="worker.userId"
                class="recommend-card"
                :class="{ 
                  'selected': selectedUserIds.includes(worker.userId),
                  'best-match': result.selected && result.selected.userId === worker.userId 
                }"
                @click="toggleRecommendWorker(worker)"
              >
                <div class="flex items-center justify-between">
                  <span class="font-medium">{{ worker.nickname }}</span>
                  <el-tag 
                    size="small" 
                    :type="result.selected && result.selected.userId === worker.userId ? 'success' : 'info'"
                  >
                    {{ result.selected && result.selected.userId === worker.userId ? '推荐' : '候选' }}: {{ worker.matchScore }}
                  </el-tag>
                </div>
                <div class="text-xs text-gray-500 mt-1">
                  <span v-for="skill in worker.skills" :key="skill.skillId" class="mr-2">
                    {{ skill.skillName }}: Lv.{{ skill.proficiency }}
                  </span>
                </div>
              </div>
            </div>
          </div>
          <div class="mt-4 text-center">
            <el-button 
              type="success" 
              size="large"
              @click="addAllRecommendedToSelected"
              :disabled="!hasValidRecommendations"
            >
              <el-icon><Check /></el-icon>
              一键添加所有推荐人员
            </el-button>
          </div>
        </div>

        <div class="selected-members">
          <div class="flex items-center justify-between mb-2">
            <span class="text-sm text-gray-600">
              已选成员 ({{ selectedUserIds.length }}人):
            </span>
            <el-button 
              type="primary" 
              link
              size="small"
              @click="workerDialogVisible = true"
            >
              手动添加成员
            </el-button>
          </div>
          <div v-if="selectedUserIds.length > 0">
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
          </div>
          <el-empty 
            v-else
            description="暂无成员，通过上方智能推荐或手动添加"
            :image-size="80"
          />
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
import { Plus, MagicStick, Check } from '@element-plus/icons-vue'
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
const recommendResults = ref([])
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

const positionRequirements = ref([])

const hasValidRecommendations = computed(() => {
  return recommendResults.value.some(r => r.selected)
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
  recommendResults.value = []
  shiftRiskList.value = []
  positionRequirements.value = []
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
  recommendResults.value = []
  positionRequirements.value = []
  loadShiftDetail(row.id)
}

const addPositionRequirement = () => {
  positionRequirements.value.push({
    positionName: '',
    requiredSkillIds: [],
    minProficiency: 1,
    count: 1
  })
}

const removePositionRequirement = (index) => {
  positionRequirements.value.splice(index, 1)
  recommendResults.value = []
}

const handleRecommendTeam = async () => {
  const invalidReq = positionRequirements.value.find(req => 
    !req.positionName || req.requiredSkillIds.length === 0
  )
  if (invalidReq) {
    ElMessage.warning('请填写完整的岗位名称和所需技能')
    return
  }

  try {
    const res = await shiftApi.recommendTeam(formData.id || 0, {
      requirements: positionRequirements.value
    })
    if (res.code === 200) {
      recommendResults.value = res.data
      checkShiftRisk()
      if (res.data.length > 0) {
        ElMessage.success('推荐完成，请查看下方推荐组合')
      }
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('推荐失败')
  }
}

const addRecommendedToSelected = (result) => {
  if (!result.selected) return
  if (!selectedUserIds.value.includes(result.selected.userId)) {
    selectedUserIds.value.push(result.selected.userId)
  }
  ElMessage.success(`已添加 ${result.selected.nickname} 到排班`)
}

const addAllRecommendedToSelected = () => {
  let addedCount = 0
  for (const result of recommendResults.value) {
    if (result.selected && !selectedUserIds.value.includes(result.selected.userId)) {
      selectedUserIds.value.push(result.selected.userId)
      addedCount++
    }
  }
  if (addedCount > 0) {
    ElMessage.success(`已添加 ${addedCount} 名推荐人员到排班`)
  } else {
    ElMessage.info('没有可添加的推荐人员')
  }
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
      const allSkillIds = new Set()
      for (const req of positionRequirements.value) {
        if (req.requiredSkillIds) {
          req.requiredSkillIds.forEach(id => allSkillIds.add(id))
        }
      }
      if (allSkillIds.size === 0) {
        allSkillIds.add(...selectedSkills.value)
      }
      shiftRiskList.value = res.data.filter(r => 
        r.isRisk && allSkillIds.has(r.skillId)
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

.position-requirement-card {
  padding: 16px;
  background: #fafafa;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.position-skills {
  display: flex;
  align-items: flex-start;
}

.recommend-result-card {
  padding: 16px;
  background: #f0f9eb;
  border: 1px solid #c2e7b0;
  border-radius: 8px;
}

.recommend-card.best-match {
  border-color: #67c23a;
  background: #f0f9eb;
  border-width: 2px;
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
