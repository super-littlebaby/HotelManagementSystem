<template>
  <div class="page facility-damage-page">
    <div class="page-header">
      <h2>设施损坏追责</h2>
    </div>

    <el-card class="search-card">
      <el-form :inline="true">
        <el-form-item label="房间号">
          <el-input v-model="searchRoomNumber" placeholder="请输入房间号" @keyup.enter="searchRoom" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchRoom">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <template v-if="roomInfo">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="info-card">
            <template #header>
              <span>房间信息</span>
            </template>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="房间号">{{ roomInfo.roomNumber }}</el-descriptions-item>
              <el-descriptions-item label="所属酒店">{{ roomInfo.hotelName }}</el-descriptions-item>
              <el-descriptions-item label="房型">{{ roomInfo.roomTypeName }}</el-descriptions-item>
              <el-descriptions-item label="当前状态">
                <el-tag :type="statusTagType(roomInfo.status)">{{ statusLabel(roomInfo.status) }}</el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card class="info-card">
            <template #header>
              <span>客人信息</span>
            </template>
            <el-descriptions v-if="roomInfo.guest && roomInfo.guest.guestName" :column="1" border>
              <el-descriptions-item label="姓名">{{ roomInfo.guest.guestName }}</el-descriptions-item>
              <el-descriptions-item label="手机号">{{ roomInfo.guest.guestPhone || '无' }}</el-descriptions-item>
              <el-descriptions-item label="邮箱">{{ roomInfo.guest.guestEmail || '无' }}</el-descriptions-item>
              <el-descriptions-item label="入住状态">
                <el-tag v-if="roomInfo.guest.checkInStatus" :type="checkInStatusTag(roomInfo.guest.checkInStatus)">
                  {{ checkInStatusLabel(roomInfo.guest.checkInStatus) }}
                </el-tag>
                <span v-else>无入住记录</span>
              </el-descriptions-item>
            </el-descriptions>
            <el-empty v-else description="暂无入住记录" :image-size="60" />
          </el-card>
        </el-col>
      </el-row>

      <el-card class="damage-card">
        <template #header>
          <span>设施损坏上报</span>
        </template>

        <div class="section-title">房间设施列表（点击勾选损坏的设施）</div>
        <el-table :data="facilityList" border @selection-change="handleFacilitySelection">
          <el-table-column type="selection" width="50" />
          <el-table-column prop="name" label="设施名称" />
          <el-table-column prop="price" label="原价(元)" width="120">
            <template #default="{ row }">
              ¥{{ row.price }}
            </template>
          </el-table-column>
          <el-table-column label="赔偿比例(%)" width="200">
            <template #default="{ row }">
              <el-input-number v-model="row.compensationPercent" :min="0" :max="100" :step="1"
                :disabled="!isFacilitySelected(row)" />
            </template>
          </el-table-column>
          <el-table-column label="赔偿金额(元)" width="150">
            <template #default="{ row }">
              <span v-if="isFacilitySelected(row)" class="damage-amount">
                ¥{{ calcDamageAmount(row).toFixed(2) }}
              </span>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
        </el-table>

        <div class="total-row">
          <span>损坏赔偿合计：</span>
          <span class="total-amount">¥{{ totalDamageAmount.toFixed(2) }}</span>
        </div>

        <el-divider />

        <el-form :model="damageForm" label-width="140px">
          <el-form-item label="损坏备注" required>
            <el-input v-model="damageForm.notes" type="textarea" :rows="3"
              placeholder="请描述损坏情况，将作为维修说明" />
          </el-form-item>

          <el-form-item label="客人造成损坏">
            <el-switch v-model="damageForm.guestCausedDamage" />
            <span class="hint-text" v-if="damageForm.guestCausedDamage">
              确认后将在账单中生成损坏赔偿记录（已关闭账单将自动新建）
            </span>
            <span class="hint-text" v-else>
              仅设置房间为维修中，不生成账单赔偿
            </span>
          </el-form-item>
        </el-form>

        <div class="form-actions">
          <el-button @click="resetForm">重置</el-button>
          <el-button type="danger" @click="submitReport">提交上报</el-button>
        </div>
      </el-card>
    </template>

    <el-empty v-else-if="searched && !searching" description="未查询到房间信息" />
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoomDamageInfo, reportDamage } from '../api/facilityDamage'

const searchRoomNumber = ref('')
const roomInfo = ref(null)
const searched = ref(false)
const searching = ref(false)

const damageForm = reactive({
  notes: '',
  guestCausedDamage: false
})

const facilityList = ref([])
const selectedFacilities = ref([])

const statusLabel = (s) => ({
  vacant: '空闲', occupied: '入住中', dirty: '待打扫', out_of_order: '维修中'
}[s] || s)

const statusTagType = (s) => ({
  vacant: 'success', occupied: 'primary', dirty: 'warning', out_of_order: 'danger'
}[s] || '')

const checkInStatusLabel = (s) => ({
  in_house: '在住', checked_out: '已退房', early_check_out: '提前退房'
}[s] || s)

const checkInStatusTag = (s) => ({
  in_house: 'success', checked_out: 'info', early_check_out: 'warning'
}[s] || '')

const isFacilitySelected = (row) => selectedFacilities.value.some(f => f.id === row.id)

const calcDamageAmount = (row) => {
  if (!isFacilitySelected(row)) return 0
  const price = Number(row.price || 0)
  const percent = Number(row.compensationPercent || 0)
  return +(price * percent / 100).toFixed(2)
}

const totalDamageAmount = computed(() => {
  let total = 0
  for (const f of selectedFacilities.value) {
    total += calcDamageAmount(f)
  }
  return +total.toFixed(2)
})

const handleFacilitySelection = (selection) => {
  selectedFacilities.value = selection
  for (const item of selection) {
    if (item.compensationPercent === undefined || item.compensationPercent === null) {
      item.compensationPercent = 20
    }
  }
  if (selection.length > 0) {
    const names = selection.map(f => f.name).join('、')
    damageForm.notes = names + '损坏'
  } else {
    damageForm.notes = ''
  }
}

const searchRoom = async () => {
  if (!searchRoomNumber.value.trim()) {
    ElMessage.warning('请输入房间号')
    return
  }
  searching.value = true
  searched.value = true
  roomInfo.value = null
  facilityList.value = []
  selectedFacilities.value = []

  try {
    const res = await getRoomDamageInfo(searchRoomNumber.value.trim())
    if (res.code === 200) {
      roomInfo.value = res.data
      if (res.data.facilities && res.data.facilities.length > 0) {
        facilityList.value = res.data.facilities.map(f => ({
          ...f,
          compensationPercent: 20
        }))
      }
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (error) {
    ElMessage.error('查询失败')
  } finally {
    searching.value = false
  }
}

const resetForm = () => {
  damageForm.notes = ''
  damageForm.guestCausedDamage = false
  selectedFacilities.value = []
  for (const f of facilityList.value) {
    f.compensationPercent = 20
  }
}

const submitReport = async () => {
  if (!roomInfo.value) {
    ElMessage.warning('请先查询房间')
    return
  }
  if (selectedFacilities.value.length === 0) {
    ElMessage.warning('请至少选择一个损坏的设施')
    return
  }
  if (!damageForm.notes.trim()) {
    ElMessage.warning('请填写损坏备注')
    return
  }
  if (damageForm.guestCausedDamage && totalDamageAmount.value <= 0) {
    ElMessage.warning('赔偿金额必须大于0')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认上报？房间将被设置为维修中，共选择 ${selectedFacilities.value.length} 项设施，${damageForm.guestCausedDamage ? `赔偿合计 ¥${totalDamageAmount.value.toFixed(2)}` : '损坏非客人原因，不生成账单'}`,
      '设施损坏上报确认',
      { type: 'warning' }
    )

    const damagedFacilities = selectedFacilities.value.map(f => ({
      facilityId: f.id,
      compensationPercent: f.compensationPercent
    }))

    const res = await reportDamage({
      roomNumber: roomInfo.value.roomNumber,
      notes: damageForm.notes,
      guestCausedDamage: damageForm.guestCausedDamage,
      damagedFacilities
    })

    if (res.code === 200) {
      ElMessage.success(res.data?.message || '上报成功')
      const currentRoomNumber = roomInfo.value.roomNumber
      resetForm()
      searchRoomNumber.value = currentRoomNumber
      searchRoom()
    } else {
      ElMessage.error(res.message || '上报失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('上报失败')
    }
  }
}
</script>
