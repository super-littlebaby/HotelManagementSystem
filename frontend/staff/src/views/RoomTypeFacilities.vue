<template>
  <div class="page">
    <div class="page-header">
      <h2>房型-设施关联管理</h2>
      <el-button type="primary" @click="showRoomTypeSelect = true" :disabled="!canModify" title="无权修改">
        分配设施
      </el-button>
    </div>

    <el-table :data="roomTypesWithFacilities" border v-loading="loading">
      <el-table-column prop="id" label="房型ID" width="80" />
      <el-table-column prop="hotelName" label="所属酒店" width="150" />
      <el-table-column prop="name" label="房型名称" width="150" />
      <el-table-column prop="basePrice" label="基础价格" width="120">
        <template #default="{ row }">
          ¥{{ row.basePrice }}
        </template>
      </el-table-column>
      <el-table-column prop="bedType" label="床型" width="100" />
      <el-table-column prop="facilities" label="关联设施">
        <template #default="{ row }">
          <el-tag
            v-for="facility in row.facilities"
            :key="facility.id"
            class="facility-tag"
            size="small"
          >
            {{ facility.name }}
          </el-tag>
          <span v-if="row.facilities.length === 0" style="color: #999;">无</span>
        </template>
      </el-table-column>
      <el-table-column prop="facilityCount" label="设施数量" width="100" align="center" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="editRoomTypeFacilities(row)" :disabled="!canModify">
            编辑设施
          </el-button>
          <el-button
            size="small"
            type="danger"
            @click="clearRoomTypeFacilities(row)"
            :disabled="!canModify || row.facilities.length === 0"
          >
            清空
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 选择房型对话框 -->
    <el-dialog v-model="showRoomTypeSelect" title="选择房型" width="500px">
      <el-form :model="selectForm" label-width="80px">
        <el-form-item label="选择房型" required>
          <el-select v-model="selectForm.roomTypeId" placeholder="请选择房型" style="width: 100%;">
            <el-option
              v-for="rt in roomTypes"
              :key="rt.id"
              :label="rt.name + ' (' + rt.hotelName + ')'"
              :value="rt.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRoomTypeSelect = false">取消</el-button>
        <el-button type="primary" @click="confirmRoomTypeSelect">下一步</el-button>
      </template>
    </el-dialog>

    <!-- 设施分配对话框 -->
    <el-dialog
      v-model="showEditDialog"
      :title="currentRoomTypeName + ' - 设施分配'"
      width="700px"
      :close-on-click-modal="false"
    >
      <div class="facility-select-container">
        <div class="facility-group">
          <div class="group-header">
            <h4>可选设施</h4>
            <el-checkbox
              v-model="selectAllAvailable"
              :indeterminate="isIndeterminate"
              @change="handleSelectAllAvailable"
            >
              全选
            </el-checkbox>
          </div>
          <div class="facility-list">
            <el-checkbox-group v-model="selectedFacilityIds">
              <el-checkbox
                v-for="facility in availableFacilities"
                :key="facility.id"
                :label="facility.id"
                class="facility-item"
              >
                <span class="facility-name">{{ facility.name }}</span>
                <span class="facility-price">¥{{ facility.price }}</span>
              </el-checkbox>
            </el-checkbox-group>
          </div>
        </div>
        <div class="facility-group">
          <div class="group-header">
            <h4>已选设施 ({{ selectedFacilities.length }})</h4>
            <el-button
              link
              type="danger"
              size="small"
              @click="clearAllSelected"
              :disabled="selectedFacilities.length === 0"
            >
              清空已选
            </el-button>
          </div>
          <div class="selected-list">
            <el-tag
              v-for="facility in selectedFacilities"
              :key="facility.id"
              closable
              @close="removeSelectedFacility(facility.id)"
              class="selected-tag"
            >
              {{ facility.name }}
            </el-tag>
            <div v-if="selectedFacilities.length === 0" class="empty-tip">
              未选择任何设施
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="saveFacilities" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoomTypes } from '../api/roomType'
import { getFacilities } from '../api/facility'
import {
  getFacilitiesByRoomTypeId,
  replaceFacilitiesForRoomType,
  removeFacilitiesFromRoomType
} from '../api/roomTypeFacility'
import { state as authState } from '../stores/auth'

const roomTypes = ref([])
const facilities = ref([])
const roomTypesWithFacilities = ref([])
const loading = ref(false)
const saving = ref(false)
const showRoomTypeSelect = ref(false)
const showEditDialog = ref(false)

const currentRole = computed(() => authState.staff?.role || '')

const canModify = computed(() => {
  return currentRole.value === 'admin' || currentRole.value === 'manager'
})

const selectForm = reactive({
  roomTypeId: null
})

const selectedFacilityIds = ref([])
const currentRoomTypeId = ref(null)
const currentRoomTypeName = ref('')

const availableFacilities = computed(() => {
  return facilities.value
})

const selectedFacilities = computed(() => {
  return facilities.value.filter(f => selectedFacilityIds.value.includes(f.id))
})

const selectAllAvailable = computed(() => {
  return availableFacilities.value.length > 0 &&
    selectedFacilityIds.value.length === availableFacilities.value.length
})

const isIndeterminate = computed(() => {
  return selectedFacilityIds.value.length > 0 &&
    selectedFacilityIds.value.length < availableFacilities.value.length
})

const handleSelectAllAvailable = (val) => {
  if (val) {
    selectedFacilityIds.value = availableFacilities.value.map(f => f.id)
  } else {
    selectedFacilityIds.value = []
  }
}

const loadRoomTypes = async () => {
  loading.value = true
  try {
    const response = await getRoomTypes()
    roomTypes.value = response.data || []
    await loadAllRoomTypeFacilities()
  } catch (error) {
    ElMessage.error('加载房型列表失败')
  } finally {
    loading.value = false
  }
}

const loadFacilities = async () => {
  try {
    const response = await getFacilities()
    facilities.value = response.data || []
  } catch (error) {
    ElMessage.error('加载设施列表失败')
  }
}

const loadAllRoomTypeFacilities = async () => {
  const list = []
  for (const rt of roomTypes.value) {
    try {
      const response = await getFacilitiesByRoomTypeId(rt.id)
      const facilityList = response.data || []
      list.push({
        ...rt,
        facilities: facilityList,
        facilityCount: facilityList.length
      })
    } catch (error) {
      list.push({
        ...rt,
        facilities: [],
        facilityCount: 0
      })
    }
  }
  roomTypesWithFacilities.value = list
}

const loadRoomTypeFacilitiesById = async (roomTypeId) => {
  try {
    const response = await getFacilitiesByRoomTypeId(roomTypeId)
    return response.data || []
  } catch (error) {
    ElMessage.error('加载房型设施失败')
    return []
  }
}

const confirmRoomTypeSelect = async () => {
  if (!selectForm.roomTypeId) {
    ElMessage.warning('请选择房型')
    return
  }

  currentRoomTypeId.value = selectForm.roomTypeId
  const roomType = roomTypes.value.find(rt => rt.id === selectForm.roomTypeId)
  if (roomType) {
    currentRoomTypeName.value = roomType.name
  }

  const currentFacilities = await loadRoomTypeFacilitiesById(selectForm.roomTypeId)
  selectedFacilityIds.value = currentFacilities.map(f => f.id)

  showRoomTypeSelect.value = false
  showEditDialog.value = true
}

const editRoomTypeFacilities = async (row) => {
  currentRoomTypeId.value = row.id
  currentRoomTypeName.value = row.name
  selectForm.roomTypeId = row.id

  const currentFacilities = await loadRoomTypeFacilitiesById(row.id)
  selectedFacilityIds.value = currentFacilities.map(f => f.id)

  showEditDialog.value = true
}

const removeSelectedFacility = (facilityId) => {
  const index = selectedFacilityIds.value.indexOf(facilityId)
  if (index > -1) {
    selectedFacilityIds.value.splice(index, 1)
  }
}

const clearAllSelected = () => {
  selectedFacilityIds.value = []
}

const saveFacilities = async () => {
  if (!currentRoomTypeId.value) {
    ElMessage.warning('请选择房型')
    return
  }

  saving.value = true
  try {
    await replaceFacilitiesForRoomType(currentRoomTypeId.value, selectedFacilityIds.value)
    ElMessage.success('设施分配成功')
    showEditDialog.value = false
    await loadRoomTypes()
  } catch (error) {
    const message = error.response?.data?.message || '操作失败'
    ElMessage.error(message)
  } finally {
    saving.value = false
  }
}

const clearRoomTypeFacilities = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要清空房型「${row.name}」的所有设施吗？`,
      '确认清空',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    const facilityIds = row.facilities.map(f => f.id)
    await removeFacilitiesFromRoomType(row.id, facilityIds)
    ElMessage.success('清空成功')
    await loadRoomTypes()
  } catch (error) {
    if (error !== 'cancel') {
      const message = error.response?.data?.message || '操作失败'
      ElMessage.error(message)
    }
  }
}

onMounted(() => {
  loadRoomTypes()
  loadFacilities()
})
</script>


