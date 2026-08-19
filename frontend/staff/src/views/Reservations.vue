<template>
  <div class="page">
    <div class="page-header">
      <h2>预订管理</h2>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm">
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
    </el-card>

    <!-- 预订列表 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="reservations" border v-loading="loading">
        <el-table-column prop="id" label="预订ID" width="80" />
        <el-table-column prop="hotelName" label="酒店" width="150" show-overflow-tooltip />
        <el-table-column prop="guestName" label="客人" width="120" />
        <el-table-column label="房型" width="140">
          <template #default="{ row }">
            <span v-if="row.rooms && row.rooms.length > 0">
              {{ row.rooms.length }}间 · {{ row.rooms[0].roomTypeName }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="checkInDate" label="入住日期" width="120" />
        <el-table-column prop="checkOutDate" label="退房日期" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="110">
          <template #default="{ row }">¥{{ Number(row.totalAmount).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="渠道" width="100">
          <template #default="{ row }">{{ getChannelLabel(row.channel) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
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
              type="success"
              @click="handleCheckIn(row)"
              v-if="row.status === 'confirmed'"
            >
              办理入住
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

      <div v-if="reservations.length === 0 && !loading" class="empty-state">
        暂无预订记录
      </div>
    </el-card>

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
          @click="handleCheckOut(currentReservation)"
          v-if="currentReservation && currentReservation.status === 'checked_in'"
        >
          办理退房
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
    <el-dialog v-model="assignRoomDialogVisible" title="分配房间" width="550px">
      <el-form label-width="100px">
        <el-form-item label="选择房型">
          <el-select v-model="selectedRoomTypeId" placeholder="请选择房型" style="width: 100%" @change="handleRoomTypeChange">
            <el-option
              v-for="rt in roomTypes"
              :key="rt.id"
              :label="rt.name"
              :value="rt.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="选择房间">
          <el-select v-model="selectedRoomId" placeholder="请选择房间" style="width: 100%">
            <el-option
              v-for="room in availableRooms"
              :key="room.id"
              :label="`${room.roomNumber} - ${room.roomTypeName || ''}`"
              :value="room.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignRoomDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAssignRoom">确认分配</el-button>
      </template>
    </el-dialog>

    <!-- 办理入住（同住客人信息录入）对话框 -->
    <el-dialog v-model="checkInDialogVisible" title="办理入住 - 同住客人信息" width="800px">
      <div v-if="currentReservation" class="check-in-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="预订ID">{{ currentReservation.id }}</el-descriptions-item>
          <el-descriptions-item label="客人">{{ currentReservation.guestName }}</el-descriptions-item>
          <el-descriptions-item label="入住日期">{{ currentReservation.checkInDate }}</el-descriptions-item>
          <el-descriptions-item label="退房日期">{{ currentReservation.checkOutDate }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">主登记人信息（自动填充）</el-divider>
        <el-alert
          title="主登记人信息将自动从客人档案填充到同住客人表中"
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 15px"
        />

        <el-divider content-position="left">
          同住客人信息（预约 {{ totalStayGuests }} 人，已添加 {{ stayGuestForm.length }} 人）
        </el-divider>
        <div v-if="stayGuestForm.length === 0" style="margin-bottom: 15px">
          <el-alert
            title="仅主登记人入住时无需添加同住客人；如有同行人员请点击下方按钮添加"
            type="success"
            :closable="false"
            show-icon
          />
        </div>
        <div v-for="(guest, index) in stayGuestForm" :key="index" class="stay-guest-item">
          <el-form label-width="100px">
            <el-form-item :label="`姓名${index + 1}`" required>
              <el-input v-model="guest.name" :placeholder="`同住客人${index + 1}姓名`" />
            </el-form-item>
            <el-form-item :label="`证件类型${index + 1}`">
              <el-select v-model="guest.idType" placeholder="请选择证件类型">
                <el-option label="身份证" value="id_card" />
                <el-option label="护照" value="passport" />
                <el-option label="驾驶证" value="drivers_license" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
            <el-form-item :label="`证件号${index + 1}`" required>
              <el-input v-model="guest.idNumber" :placeholder="`同住客人${index + 1}证件号`" />
            </el-form-item>
            <el-form-item>
              <el-button type="danger" size="small" @click="removeStayGuest(index)">删除</el-button>
            </el-form-item>
          </el-form>
        </div>
        <el-button
          type="primary"
          plain
          size="small"
          :disabled="stayGuestForm.length >= totalStayGuests - 1"
          @click="addStayGuest"
          style="margin-top: 5px"
        >
          + 添加同住客人（最多 {{ Math.max(0, totalStayGuests - 1) }} 人）
        </el-button>
      </div>
      <template #footer>
        <el-button @click="checkInDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCheckIn">确认办理入住</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
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
const stayGuestForm = ref([])

// 计算当前入住所需的总人数（成人 + 儿童，取第一个房间的数据）
const totalStayGuests = computed(() => {
  if (!currentReservation.value || !currentReservation.value.rooms || currentReservation.value.rooms.length === 0) {
    return 1
  }
  const room = currentReservation.value.rooms[0]
  const adults = room.adults || 1
  const children = room.children || 0
  return adults + children
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
    const [roomTypesRes] = await Promise.all([
      getRoomTypes()
    ])

    if (roomTypesRes.code === 200) {
      roomTypes.value = roomTypesRes.data
    }

    const roomTypeId = row.rooms?.[0]?.roomTypeId
    selectedRoomTypeId.value = roomTypeId

    if (roomTypeId) {
      await handleRoomTypeChange(roomTypeId)
    }

    currentReservation.value = row
    selectedRoomId.value = null
    assignRoomDialogVisible.value = true
  } catch (error) {
    doConfirm(row.id, null)
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
  
  assignRoomDialogVisible.value = false
  
  if (!selectedRoomId.value) {
    ElMessage.warning('请选择房间')
    return
  }

  if (currentReservation.value.status === 'pending') {
    doConfirm(currentReservation.value.id, selectedRoomId.value)
  } else if (currentReservation.value.status === 'confirmed') {
    try {
      const res = await assignRoom(currentReservation.value.id, selectedRoomId.value)
      if (res.code === 200) {
        ElMessage.success('分配房间成功')
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
    // 加载预订详情，获取成人数和儿童数
    const res = await getReservationById(row.id)
    if (res.code === 200) {
      currentReservation.value = res.data
      // 初始化为空列表，由用户按实际入住人数动态添加同住客人
      stayGuestForm.value = []
      checkInDialogVisible.value = true
    } else {
      ElMessage.error(res.message || '获取预订详情失败')
    }
  } catch (error) {
    ElMessage.error('获取预订详情失败')
  }
}

const addStayGuest = () => {
  if (stayGuestForm.value.length >= totalStayGuests.value - 1) {
    ElMessage.warning(`同住客人最多 ${Math.max(0, totalStayGuests.value - 1)} 人`)
    return
  }
  stayGuestForm.value.push({
    name: '',
    idType: 'id_card',
    idNumber: '',
    isPrimary: false
  })
}

const removeStayGuest = (index) => {
  stayGuestForm.value.splice(index, 1)
}

const confirmCheckIn = async () => {
  if (!currentReservation.value) return

  // 校验已添加的同住客人信息
  for (let i = 0; i < stayGuestForm.value.length; i++) {
    const g = stayGuestForm.value[i]
    if (!g.name || !g.name.trim()) {
      ElMessage.warning(`请填写同住客人${i + 1}的姓名`)
      return
    }
    if (!g.idNumber || !g.idNumber.trim()) {
      ElMessage.warning(`请填写同住客人${i + 1}的证件号`)
      return
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

    // 只提交实际填写的同住客人信息（不含主登记人，主登记人由后端自动填充）
    const stayGuests = stayGuestForm.value.map(g => ({
      name: g.name,
      idType: g.idType || 'id_card',
      idNumber: g.idNumber,
      isPrimary: false
    }))

    const res = await checkInReservation(currentReservation.value.id, stayGuests)
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

<style scoped>
.page {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
  color: #999;
}

.detail-content {
  padding: 10px 0;
}

.detail-content h4 {
  margin: 20px 0 10px;
  font-size: 16px;
  color: #333;
}

.check-in-content {
  padding: 10px 0;
}

.stay-guest-item {
  padding: 10px;
  margin-bottom: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}
</style>
