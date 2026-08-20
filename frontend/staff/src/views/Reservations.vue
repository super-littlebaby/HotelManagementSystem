<template>
  <div class="page">
    <div class="page-header">
      <h2>预订管理</h2>
    </div>

    <!-- 搜索和筛选 -->
    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="搜索类型">
        <el-select v-model="searchForm.type" style="width: 120px">
          <el-option label="全部" value="all" />
          <el-option label="手机号" value="phone" />
          <el-option label="邮箱" value="email" />
          <el-option label="客人姓名" value="name" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词" v-if="searchForm.type !== 'all'">
        <el-input
          v-model="searchForm.keyword"
          :placeholder="getSearchPlaceholder()"
          style="width: 200px"
          clearable
          @keyup.enter="handleSearch"
        />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="全部状态" style="width: 140px" clearable>
          <el-option label="待确认" value="pending" />
          <el-option label="已确认" value="confirmed" />
          <el-option label="已入住" value="checked_in" />
          <el-option label="已退房" value="checked_out" />
          <el-option label="已取消" value="cancelled" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
        <el-button @click="loadReservations">刷新</el-button>
      </el-form-item>
    </el-form>

    <!-- 预订列表 -->
    <el-table :data="reservations" border stripe v-loading="loading">
      <el-table-column prop="id" label="预订ID" width="80" />
      <el-table-column prop="hotelName" label="酒店" show-overflow-tooltip />
      <el-table-column prop="guestName" label="客人" />
      <el-table-column label="房型">
        <template #default="{ row }">
          <span v-if="row.rooms && row.rooms.length > 0">
            <span v-for="(item, idx) in getRoomTypeSummary(row.rooms)" :key="idx" style="margin-right: 4px;">
              {{ item.count }}间{{ item.name }}{{ idx < getRoomTypeSummary(row.rooms).length - 1 ? '、' : '' }}
            </span>
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="入住日期">
        <template #default="{ row }">{{ formatDate(row.checkInDate) }}</template>
      </el-table-column>
      <el-table-column label="退房日期">
        <template #default="{ row }">{{ formatDate(row.checkOutDate) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="金额" align="right">
        <template #default="{ row }">¥{{ Number(row.totalAmount).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="渠道">
        <template #default="{ row }">{{ getChannelLabel(row.channel) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row)">详情</el-button>
          <el-button
            size="small"
            type="primary"
            @click="handleConfirm(row)"
            v-if="row.status === 'pending'"
          >
            确认
          </el-button>
          <el-button
            size="small"
            type="danger"
            @click="handleCancel(row)"
            v-if="row.status === 'pending' || row.status === 'confirmed'"
          >
            取消
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 预订详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="预订详情" width="700px">
      <div v-if="currentReservation" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="预订ID">{{ currentReservation.id }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentReservation.status)">
              {{ getStatusLabel(currentReservation.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="酒店">{{ currentReservation.hotelName }}</el-descriptions-item>
          <el-descriptions-item label="客人">{{ currentReservation.guestName }}</el-descriptions-item>
          <el-descriptions-item label="入住日期">{{ currentReservation.checkInDate }}</el-descriptions-item>
          <el-descriptions-item label="退房日期">{{ currentReservation.checkOutDate }}</el-descriptions-item>
          <el-descriptions-item label="预订渠道">{{ getChannelLabel(currentReservation.channel) }}</el-descriptions-item>
          <el-descriptions-item label="总金额">¥{{ Number(currentReservation.totalAmount).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="预订时间" :span="2">
            {{ formatDateTime(currentReservation.bookingDate) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentReservation.specialRequests" label="特殊要求" :span="2">
            {{ currentReservation.specialRequests }}
          </el-descriptions-item>
        </el-descriptions>

        <h4 style="margin: 20px 0 10px">房间明细</h4>
        <el-table :data="currentReservation.rooms" border size="small">
          <el-table-column prop="roomTypeName" label="房型" />
          <el-table-column prop="roomNumber" label="房间号" width="100">
            <template #default="{ row }">
              {{ row.roomNumber || '未分配' }}
            </template>
          </el-table-column>
          <el-table-column prop="adults" label="成人" width="80" />
          <el-table-column prop="children" label="儿童" width="80" />
          <el-table-column label="房价/晚" width="120">
            <template #default="{ row }">¥{{ Number(row.ratePerNight).toFixed(2) }}</template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          type="primary"
          @click="handleConfirm(currentReservation)"
          v-if="currentReservation && currentReservation.status === 'pending'"
        >
          确认预订
        </el-button>
        <el-button
          type="warning"
          @click="handleAssignRoom(currentReservation)"
          v-if="currentReservation && currentReservation.status === 'confirmed' && hasUnassignedRoom(currentReservation)"
        >
          分配房间
        </el-button>
        <el-button
          type="success"
          @click="handleCheckIn(currentReservation)"
          v-if="currentReservation && currentReservation.status === 'confirmed'"
        >
          办理入住
        </el-button>
        <el-button
          type="danger"
          @click="handleCancel(currentReservation)"
          v-if="currentReservation && (currentReservation.status === 'pending' || currentReservation.status === 'confirmed')"
        >
          取消预订
        </el-button>
      </template>
    </el-dialog>

    <!-- 分配房间对话框 -->
    <el-dialog v-model="assignRoomDialogVisible" title="分配房间" width="600px">
      <el-form label-width="100px">
        <el-form-item label="选择房型">
          <el-select v-model="selectedRoomTypeId" placeholder="请选择房型" style="width: 100%" @change="handleRoomTypeChange">
            <el-option
              v-for="rt in availableRoomTypes"
              :key="rt.id"
              :label="rt.name + ' (¥' + Number(rt.basePrice).toFixed(2) + '/晚)'"
              :value="rt.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="选择房间">
          <el-select v-model="selectedRoomId" placeholder="请选择房间" style="width: 100%">
            <el-option
              v-for="room in availableRooms"
              :key="room.id"
              :label="`${room.roomNumber}`"
              :value="room.id"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <!-- 房型信息和费用计算 -->
      <el-divider v-if="selectedRoomTypeInfo" />
      <div v-if="selectedRoomTypeInfo" class="price-info">
        <h4 style="margin: 0 0 10px; color: #409eff">房型信息</h4>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="房型名称">{{ selectedRoomTypeInfo.name }}</el-descriptions-item>
          <el-descriptions-item label="基础房价">¥{{ Number(selectedRoomTypeInfo.basePrice).toFixed(2) }}/晚</el-descriptions-item>
          <el-descriptions-item label="可住成人">{{ selectedRoomTypeInfo.maxAdults }} 人</el-descriptions-item>
          <el-descriptions-item label="可住儿童">{{ selectedRoomTypeInfo.maxChildren }} 人</el-descriptions-item>
        </el-descriptions>
        <div class="total-calc">
          <el-descriptions :column="3" border size="small">
            <el-descriptions-item label="入住天数">{{ stayNights }} 天</el-descriptions-item>
            <el-descriptions-item label="房费小计">¥{{ Number(selectedRoomTypeInfo.basePrice * stayNights).toFixed(2) }}</el-descriptions-item>
            <el-descriptions-item label="应付总金额" content-class="total-label">
              <span style="color: #f56c6c; font-weight: bold; font-size: 16px">¥{{ Number(selectedRoomTypeInfo.basePrice * stayNights).toFixed(2) }}</span>
            </el-descriptions-item>
          </el-descriptions>
          <div class="price-change-hint" v-if="originalRoomType && originalRoomType.id !== selectedRoomTypeId">
            <el-alert type="warning" :closable="false" show-icon>
              <template #title>
                已更换房型！原房型：{{ originalRoomType.name }} (¥{{ Number(originalRoomType.basePrice).toFixed(2) }}/晚) → 新房型：{{ selectedRoomTypeInfo.name }} (¥{{ Number(selectedRoomTypeInfo.basePrice).toFixed(2) }}/晚)
              </template>
            </el-alert>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="assignRoomDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAssignRoom">确认分配</el-button>
      </template>
    </el-dialog>

    <!-- 办理入住（按房间录入实际入住人信息）对话框 -->
    <el-dialog v-model="checkInDialogVisible" title="办理入住 - 实际入住人信息" width="900px" top="5vh">
      <div v-if="currentReservation" class="check-in-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="预订ID">{{ currentReservation.id }}</el-descriptions-item>
          <el-descriptions-item label="预订人">{{ currentReservation.guestName }}</el-descriptions-item>
          <el-descriptions-item label="入住日期">{{ currentReservation.checkInDate }}</el-descriptions-item>
          <el-descriptions-item label="退房日期">{{ currentReservation.checkOutDate }}</el-descriptions-item>
        </el-descriptions>

        <el-alert
          title="请为每个房间填写实际入住人信息（可能是帮别人预约，所以不要直接使用预订账号信息）"
          type="warning"
          :closable="false"
          show-icon
          style="margin: 15px 0"
        />

        <div v-for="(roomCheckIn, rIndex) in roomCheckInForms" :key="rIndex" class="room-checkin-block">
          <div class="room-checkin-header">
            房间 {{ rIndex + 1 }}：
            <span class="room-info">{{ roomCheckIn.roomNumber || '未分配' }} - {{ roomCheckIn.roomTypeName || '' }}</span>
            <span class="room-capacity">(成人 {{ roomCheckIn.adults }} / 儿童 {{ roomCheckIn.children }}，共 {{ roomCheckIn.totalGuests }} 人)</span>
          </div>

          <el-divider content-position="left">主登记人信息</el-divider>
          <el-form label-width="100px">
            <el-form-item label="姓名" required>
              <el-input v-model="roomCheckIn.primaryGuestName" placeholder="请输入实际入住的主登记人姓名" />
            </el-form-item>
            <el-form-item label="证件类型" required>
              <el-select v-model="roomCheckIn.primaryIdType" placeholder="请选择证件类型">
                <el-option label="身份证" value="id_card" />
                <el-option label="护照" value="passport" />
                <el-option label="驾驶证" value="drivers_license" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
            <el-form-item label="证件号" required>
              <el-input v-model="roomCheckIn.primaryIdNumber" placeholder="请输入主登记人证件号码" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="roomCheckIn.primaryPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-form>

          <el-divider content-position="left">
            同住客人信息（共需 {{ Math.max(0, roomCheckIn.totalGuests - 1) }} 人，已添加 {{ roomCheckIn.stayGuests.length }} 人）
          </el-divider>
          <div v-if="roomCheckIn.totalGuests <= 1" style="color: #999; padding: 5px 0 10px;">
            单人入住，无需填写同住客人信息
          </div>
          <div v-for="(guest, gIndex) in roomCheckIn.stayGuests" :key="gIndex" class="stay-guest-item">
            <div class="stay-guest-header">同住客人 {{ gIndex + 1 }}</div>
            <el-form label-width="100px">
              <el-form-item label="姓名" required>
                <el-input v-model="guest.name" placeholder="请输入姓名" />
              </el-form-item>
              <el-form-item label="证件类型">
                <el-select v-model="guest.idType" placeholder="请选择证件类型">
                  <el-option label="身份证" value="id_card" />
                  <el-option label="护照" value="passport" />
                  <el-option label="驾驶证" value="drivers_license" />
                  <el-option label="其他" value="other" />
                </el-select>
              </el-form-item>
              <el-form-item label="证件号" required>
                <el-input v-model="guest.idNumber" placeholder="请输入证件号码" />
              </el-form-item>
              <el-form-item>
                <el-button type="danger" size="small" @click="removeStayGuest(rIndex, gIndex)">删除</el-button>
              </el-form-item>
            </el-form>
          </div>
          <el-button
            v-if="roomCheckIn.stayGuests.length < roomCheckIn.totalGuests - 1"
            type="primary"
            plain
            size="small"
            @click="addStayGuest(rIndex)"
            style="margin-top: 5px"
          >
            + 添加同住客人
          </el-button>
        </div>
      </div>
      <template #footer>
        <el-button @click="checkInDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCheckIn">确认办理入住</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDate } from '../utils/date'
import {
  getReservations,
  getReservationById,
  confirmReservation,
  cancelReservation,
  checkInReservation,
  checkOutReservation,
  assignRoom,
  searchByGuestPhone,
  searchByGuestEmail,
  searchByGuestName
} from '../api/reservation'
import { getRoomsByType } from '../api/room'
import { getRoomTypes } from '../api/roomType'
import { state as authState } from '../stores/auth'

const reservations = ref([])
const loading = ref(false)
const detailDialogVisible = ref(false)
const assignRoomDialogVisible = ref(false)
const checkInDialogVisible = ref(false)
const currentReservation = ref(null)
const selectedRoomId = ref(null)
const selectedRoomTypeId = ref(null)
const availableRooms = ref([])
const roomTypes = ref([])
const roomCheckInForms = ref([])
const originalRoomType = ref(null)

const availableRoomTypes = computed(() => {
  const hotelId = authState.staff?.hotelId
  if (hotelId) {
    return roomTypes.value.filter(rt => rt.hotelId === hotelId || rt.hotelId == null)
  }
  return roomTypes.value
})

const selectedRoomTypeInfo = computed(() => {
  if (!selectedRoomTypeId.value) return null
  return roomTypes.value.find(rt => rt.id === selectedRoomTypeId.value)
})

const stayNights = computed(() => {
  if (!currentReservation.value) return 1
  const checkIn = currentReservation.value.checkInDate
  const checkOut = currentReservation.value.checkOutDate
  if (!checkIn || !checkOut) return 1
  const d1 = new Date(checkIn)
  const d2 = new Date(checkOut)
  const diff = Math.round((d2 - d1) / (1000 * 60 * 60 * 24))
  return Math.max(diff, 1)
})

const searchForm = reactive({
  type: 'all',
  keyword: '',
  status: ''
})

const getStatusType = (status) => {
  const types = {
    pending: 'warning',
    confirmed: 'success',
    checked_in: 'primary',
    checked_out: 'info',
    cancelled: 'danger',
    no_show: 'info'
  }
  return types[status] || 'info'
}

const getStatusLabel = (status) => {
  const labels = {
    pending: '待确认',
    confirmed: '已确认',
    checked_in: '已入住',
    checked_out: '已退房',
    cancelled: '已取消',
    no_show: '未入住'
  }
  return labels[status] || status
}

const getChannelLabel = (channel) => {
  const labels = {
    online: '在线',
    phone: '电话',
    walk_in: '到店',
    ota: 'OTA'
  }
  return labels[channel] || channel
}

const getSearchPlaceholder = () => {
  const map = {
    phone: '请输入手机号',
    email: '请输入邮箱',
    name: '请输入客人姓名'
  }
  return map[searchForm.type] || '请输入关键词'
}

const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  return dateTimeStr.replace('T', ' ').substring(0, 16)
}

const getRoomTypeSummary = (rooms) => {
  if (!rooms || rooms.length === 0) return []
  const map = {}
  for (const room of rooms) {
    const name = room.roomTypeName || '未知房型'
    if (!map[name]) {
      map[name] = { name, count: 0 }
    }
    map[name].count++
  }
  return Object.values(map)
}

const hasUnassignedRoom = (reservation) => {
  if (!reservation || !reservation.rooms || reservation.rooms.length === 0) {
    return false
  }
  return reservation.rooms.some(room => !room.roomNumber || room.roomNumber === '未分配')
}

const loadReservations = async () => {
  loading.value = true
  try {
    const hotelId = authState.staff?.hotelId || null
    const res = await getReservations(hotelId)
    if (res.code === 200) {
      let data = res.data || []
      data = data.filter(r => r.status === 'pending' || r.status === 'confirmed')
      if (searchForm.status) {
        data = data.filter(r => r.status === searchForm.status)
      }
      reservations.value = data
    } else {
      ElMessage.error(res.message || '加载预订列表失败')
    }
  } catch (error) {
    ElMessage.error('加载预订列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  if (searchForm.type === 'all') {
    loadReservations()
    return
  }

  if (!searchForm.keyword.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  loading.value = true
  try {
    let res
    if (searchForm.type === 'phone') {
      res = await searchByGuestPhone(searchForm.keyword.trim())
    } else if (searchForm.type === 'email') {
      res = await searchByGuestEmail(searchForm.keyword.trim())
    } else if (searchForm.type === 'name') {
      res = await searchByGuestName(searchForm.keyword.trim())
    }

    if (res.code === 200) {
      let data = res.data || []
      const hotelId = authState.staff?.hotelId
      if (hotelId) {
        data = data.filter(r => r.hotelId === hotelId)
      }
      data = data.filter(r => r.status === 'pending' || r.status === 'confirmed')
      if (searchForm.status) {
        data = data.filter(r => r.status === searchForm.status)
      }
      reservations.value = data
      if (data.length === 0) {
        ElMessage.info('未找到相关预订记录')
      }
    } else {
      ElMessage.error(res.message || '搜索失败')
    }
  } catch (error) {
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.type = 'all'
  searchForm.keyword = ''
  searchForm.status = ''
  loadReservations()
}

const viewDetail = async (row) => {
  try {
    const res = await getReservationById(row.id)
    if (res.code === 200) {
      currentReservation.value = res.data
      detailDialogVisible.value = true
    } else {
      ElMessage.error(res.message || '获取详情失败')
    }
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

const handleConfirm = async (row) => {
  if (!row) return

  try {
    doConfirm(row.id, null)
  } catch (error) {
    ElMessage.error('确认失败')
  }
}

const handleRoomTypeChange = async (roomTypeId) => {
  if (!roomTypeId) {
    availableRooms.value = []
    selectedRoomId.value = null
    return
  }

  try {
    const res = await getRoomsByType(roomTypeId)
    if (res.code === 200) {
      availableRooms.value = res.data.filter(r => r.status === 'vacant')
      selectedRoomId.value = null
      if (availableRooms.value.length === 0) {
        ElMessage.warning('该房型当前没有空闲房间')
      }
    } else {
      ElMessage.error(res.message || '获取房间列表失败')
    }
  } catch (error) {
    console.error('获取房间列表错误:', error)
    ElMessage.error('获取房间列表失败')
  }
}

const handleAssignRoom = async (row) => {
  if (!row) return

  try {
    const [roomTypesRes] = await Promise.all([
      getRoomTypes()
    ])

    if (roomTypesRes.code === 200) {
      roomTypes.value = roomTypesRes.data
    } else {
      ElMessage.error('获取房型列表失败')
    }

    const roomTypeId = row.rooms?.[0]?.roomTypeId
    selectedRoomTypeId.value = roomTypeId

    if (roomTypeId) {
      await handleRoomTypeChange(roomTypeId)
    }

    // 保存原始房型信息用于对比
    originalRoomType.value = roomTypes.value.find(rt => rt.id === roomTypeId)

    currentReservation.value = row
    selectedRoomId.value = null
    assignRoomDialogVisible.value = true
  } catch (error) {
    console.error('获取房间列表错误:', error)
    ElMessage.error('获取房间列表失败: ' + (error.message || error))
  }
}

const confirmAssignRoom = async () => {
  if (!currentReservation.value) return
  
  if (!selectedRoomId.value) {
    ElMessage.warning('请选择房间')
    return
  }

  const roomTypeChanged = originalRoomType.value && originalRoomType.value.id !== selectedRoomTypeId.value

  assignRoomDialogVisible.value = false

  if (currentReservation.value.status === 'pending') {
    doConfirm(currentReservation.value.id, selectedRoomId.value)
  } else if (currentReservation.value.status === 'confirmed') {
    try {
      const res = await assignRoom(currentReservation.value.id, selectedRoomId.value, selectedRoomTypeId.value)
      if (res.code === 200) {
        let msg = '分配房间成功'
        if (roomTypeChanged) {
          msg += `（已更换房型为 ${selectedRoomTypeInfo.value?.name}）`
        }
        ElMessage.success(msg)
        loadReservations()
        if (detailDialogVisible.value) {
          currentReservation.value = res.data
        }
      } else {
        ElMessage.error(res.message || '分配房间失败')
      }
    } catch (error) {
      ElMessage.error('分配房间失败')
    }
  }
}

const doConfirm = async (id, roomId) => {
  try {
    const res = await confirmReservation(id, roomId)
    if (res.code === 200) {
      ElMessage.success('预订已确认')
      loadReservations()
      if (detailDialogVisible.value) {
        currentReservation.value = res.data
      }
    } else {
      ElMessage.error(res.message || '确认失败')
    }
  } catch (error) {
    ElMessage.error('确认失败')
  }
}

const handleCancel = async (row) => {
  if (!row) return

  try {
    await ElMessageBox.confirm(
      `确定要取消预订ID为 ${row.id} 的订单吗？`,
      '取消预订',
      {
        confirmButtonText: '确定取消',
        cancelButtonText: '再想想',
        type: 'warning'
      }
    )

    const res = await cancelReservation(row.id)
    if (res.code === 200) {
      ElMessage.success('预订已取消')
      loadReservations()
      if (detailDialogVisible.value) {
        currentReservation.value = res.data
      }
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

const handleCheckIn = async (row) => {
  if (!row) return

  try {
    const res = await getReservationById(row.id)
    if (res.code === 200) {
      currentReservation.value = res.data
      // 按房间初始化入住人信息表单
      const rooms = res.data.rooms || []
      roomCheckInForms.value = rooms.map(r => {
        const adults = r.adults || 1
        const children = r.children || 0
        return {
          reservationRoomId: r.id,
          roomNumber: r.roomNumber || '未分配',
          roomTypeName: r.roomTypeName || '',
          adults,
          children,
          totalGuests: adults + children,
          primaryGuestName: '',
          primaryIdType: 'id_card',
          primaryIdNumber: '',
          primaryPhone: '',
          stayGuests: []
        }
      })
      checkInDialogVisible.value = true
    } else {
      ElMessage.error(res.message || '获取预订详情失败')
    }
  } catch (error) {
    ElMessage.error('获取预订详情失败')
  }
}

const addStayGuest = (roomIndex) => {
  const room = roomCheckInForms.value[roomIndex]
  if (!room) return
  if (room.stayGuests.length >= room.totalGuests - 1) {
    ElMessage.warning(`同住客人最多 ${Math.max(0, room.totalGuests - 1)} 人`)
    return
  }
  room.stayGuests.push({
    name: '',
    idType: 'id_card',
    idNumber: ''
  })
}

const removeStayGuest = (roomIndex, guestIndex) => {
  roomCheckInForms.value[roomIndex].stayGuests.splice(guestIndex, 1)
}

const confirmCheckIn = async () => {
  if (!currentReservation.value) return

  // 校验每个房间的入住人信息
  for (let r = 0; r < roomCheckInForms.value.length; r++) {
    const rc = roomCheckInForms.value[r]
    if (!rc.primaryGuestName || !rc.primaryGuestName.trim()) {
      ElMessage.warning(`房间 ${r + 1}：请填写主登记人姓名`)
      return
    }
    if (!rc.primaryIdType) {
      ElMessage.warning(`房间 ${r + 1}：请选择主登记人证件类型`)
      return
    }
    if (!rc.primaryIdNumber || !rc.primaryIdNumber.trim()) {
      ElMessage.warning(`房间 ${r + 1}：请填写主登记人证件号`)
      return
    }
    for (let g = 0; g < rc.stayGuests.length; g++) {
      const guest = rc.stayGuests[g]
      if (!guest.name || !guest.name.trim()) {
        ElMessage.warning(`房间 ${r + 1}：请填写同住客人 ${g + 1} 的姓名`)
        return
      }
      if (!guest.idNumber || !guest.idNumber.trim()) {
        ElMessage.warning(`房间 ${r + 1}：请填写同住客人 ${g + 1} 的证件号`)
        return
      }
    }
  }

  try {
    await ElMessageBox.confirm(
      `确定为预订ID为 ${currentReservation.value.id} 的订单办理入住吗？`,
      '办理入住',
      {
        confirmButtonText: '确认办理',
        cancelButtonText: '取消',
        type: 'success'
      }
    )

    // 按房间分组提交入住人信息
    const submitData = {
      rooms: roomCheckInForms.value.map(rc => ({
        reservationRoomId: rc.reservationRoomId,
        primaryGuestName: rc.primaryGuestName,
        primaryIdType: rc.primaryIdType,
        primaryIdNumber: rc.primaryIdNumber,
        primaryPhone: rc.primaryPhone,
        stayGuests: rc.stayGuests.map(g => ({
          name: g.name,
          idType: g.idType || 'id_card',
          idNumber: g.idNumber,
          isPrimary: false
        }))
      }))
    }

    const res = await checkInReservation(currentReservation.value.id, submitData)
    if (res.code === 200) {
      ElMessage.success('办理入住成功')
      checkInDialogVisible.value = false
      loadReservations()
      if (detailDialogVisible.value) {
        currentReservation.value = res.data
      }
    } else {
      ElMessage.error(res.message || '办理入住失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || '办理入住失败')
    }
  }
}

const handleCheckOut = async (row) => {
  if (!row) return

  try {
    await ElMessageBox.confirm(
      `确定为预订ID为 ${row.id} 的订单办理退房吗？`,
      '办理退房',
      {
        confirmButtonText: '确认退房',
        cancelButtonText: '取消',
        type: 'danger'
      }
    )

    const res = await checkOutReservation(row.id)
    if (res.code === 200) {
      ElMessage.success('办理退房成功')
      loadReservations()
      if (detailDialogVisible.value) {
        currentReservation.value = res.data
      }
    } else {
      ElMessage.error(res.message || '办理退房失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('办理退房失败')
    }
  }
}

onMounted(() => {
  loadReservations()
})
</script>


