<template>
  <div class="page">
    <div class="page-header">
      <h2>入住管理</h2>
      <el-button type="primary" @click="showAddDialog = true">未预约入住</el-button>
    </div>
    
    <el-table :data="checkIns" border>
      <el-table-column prop="id" label="登记ID" width="80" />
      <el-table-column prop="guestName" label="客人" />
      <el-table-column prop="roomNumber" label="房间号" />
      <el-table-column prop="adults" label="成人" width="60" />
      <el-table-column prop="children" label="儿童" width="60" />
      <el-table-column prop="checkInTime" label="入住时间">
        <template #default="{ row }">{{ formatDateTime(row.checkInTime) }}</template>
      </el-table-column>
      <el-table-column prop="expectedCheckOutTime" label="预计退房">
        <template #default="{ row }">{{ formatDateTime(row.expectedCheckOutTime) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="totalCharge" label="总费用">
        <template #default="{ row }">¥{{ row.totalCharge || 0 }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="handleCheckOut(row)" v-if="row.status === 'in_house'">退房</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <el-dialog v-model="showAddDialog" title="未预约入住" width="800px">
      <el-form :model="form" label-width="100px">
        <el-divider content-position="left">主登记人信息</el-divider>
        <el-form-item label="姓名" required>
          <el-input v-model="form.guestName" placeholder="请输入主登记人姓名" />
        </el-form-item>
        <el-form-item label="证件类型" required>
          <el-select v-model="form.idType" placeholder="请选择证件类型">
            <el-option label="身份证" value="id_card" />
            <el-option label="护照" value="passport" />
            <el-option label="驾驶证" value="drivers_license" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="证件号码" required>
          <el-input v-model="form.idNumber" placeholder="请输入证件号码" />
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        
        <el-divider content-position="left">入住信息</el-divider>
        <el-form-item label="房型" required>
          <el-select v-model="form.roomTypeId" placeholder="请选择房型" @change="handleRoomTypeChange">
            <el-option v-for="rt in roomTypes" :key="rt.id" :label="rt.name" :value="rt.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="房间号" required>
          <el-select v-model="form.roomId" placeholder="请选择空闲房间" :disabled="!selectedRoomType">
            <el-option v-for="room in availableRooms" :key="room.id" :label="room.roomNumber" :value="room.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="成人" required>
          <el-input v-model.number="form.adults" type="number" :min="1" placeholder="成人数量" />
        </el-form-item>
        <el-form-item label="儿童">
          <el-input v-model.number="form.children" type="number" :min="0" placeholder="儿童数量" />
        </el-form-item>
        <el-form-item label="预计退房时间" required>
          <el-date-picker v-model="form.expectedCheckOutTime" type="datetime" style="width: 100%" placeholder="选择预计退房时间" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.notes" type="textarea" placeholder="入住备注" />
        </el-form-item>
        
        <el-divider content-position="left">同住人员</el-divider>
        <div v-if="form.stayGuests.length === 0" style="color: #999; padding: 10px;">
          单人入住，无需填写同住人员信息
        </div>
        <div v-for="(guest, index) in form.stayGuests" :key="index" class="stay-guest-item">
          <div class="stay-guest-header">同住人员 {{ index + 1 }}</div>
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
            <el-button type="danger" size="small" @click="removeStayGuest(index)">删除</el-button>
          </el-form-item>
        </div>
        <el-button type="primary" size="small" @click="addStayGuest" style="margin-top: 10px">添加同住人员</el-button>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveCheckIn">确认入住</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showCheckOutDialog" title="办理退房 - 费用结算" width="650px">
      <div v-if="checkOutResult">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="实际入住天数">
            {{ checkOutResult.actualDays }} 天
          </el-descriptions-item>
          <el-descriptions-item label="房费">
            ¥{{ Number(checkOutResult.roomCharge).toFixed(2) }}
          </el-descriptions-item>
          <el-descriptions-item label="消费记录">
            ¥{{ Number(checkOutResult.additionalCharges).toFixed(2) }}
          </el-descriptions-item>
          <el-descriptions-item label="总费用" content-class="total-charge">
            <span style="color: #f56c6c; font-weight: bold; font-size: 18px">
              ¥{{ Number(checkOutResult.totalCharge).toFixed(2) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="已收押金">
            ¥{{ Number(checkOutResult.depositAmount).toFixed(2) }}
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="checkOutResult.billItems && checkOutResult.billItems.length > 0" style="margin-top: 15px">
          <div style="font-weight: 600; margin-bottom: 8px">消费明细</div>
          <el-table :data="checkOutResult.billItems" border size="small" style="width: 100%">
            <el-table-column label="类型" width="90">
              <template #default="{ row }">
                <el-tag :type="itemTypeTag(row.itemType)" size="small">{{ itemTypeLabel(row.itemType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="说明" show-overflow-tooltip />
            <el-table-column prop="quantity" label="数量" width="70" align="center" />
            <el-table-column label="单价" width="90" align="right">
              <template #default="{ row }">¥{{ Number(row.unitPrice).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column label="金额" width="90" align="right">
              <template #default="{ row }">¥{{ Number(row.amount).toFixed(2) }}</template>
            </el-table-column>
          </el-table>
        </div>

        <el-divider />

        <div v-if="checkOutResult.needPay" class="diff-section">
          <el-alert type="warning" :closable="false" show-icon>
            <template #title>
              <span>押金不足，需补差价：<b>¥{{ Number(checkOutResult.payAmount).toFixed(2) }}</b></span>
            </template>
          </el-alert>
          <el-form :model="checkOutForm" label-width="100px" style="margin-top: 15px">
            <el-form-item label="补价方式">
              <el-select v-model="checkOutForm.method" style="width: 100%">
                <el-option label="现金" value="cash" />
                <el-option label="信用卡" value="credit_card" />
                <el-option label="借记卡" value="debit_card" />
                <el-option label="微信支付" value="wechat" />
                <el-option label="支付宝" value="alipay" />
                <el-option label="银行转账" value="bank_transfer" />
              </el-select>
            </el-form-item>
          </el-form>
        </div>

        <div v-else-if="checkOutResult.needRefund" class="diff-section">
          <el-alert type="success" :closable="false" show-icon>
            <template #title>
              <span>押金多余，需退款：<b>¥{{ Number(checkOutResult.refundAmount).toFixed(2) }}</b></span>
            </template>
          </el-alert>
          <el-form :model="checkOutForm" label-width="100px" style="margin-top: 15px">
            <el-form-item label="退款方式">
              <el-select v-model="checkOutForm.method" style="width: 100%">
                <el-option label="现金" value="cash" />
                <el-option label="信用卡原路退回" value="credit_card" />
                <el-option label="借记卡原路退回" value="debit_card" />
                <el-option label="微信退款" value="wechat" />
                <el-option label="支付宝退款" value="alipay" />
                <el-option label="银行转账" value="bank_transfer" />
              </el-select>
            </el-form-item>
          </el-form>
        </div>

        <div v-else class="diff-section">
          <el-alert type="info" :closable="false" show-icon>
            <template #title>押金刚好等于总费用，无需补价或退款</template>
          </el-alert>
        </div>
      </div>

      <template #footer>
        <el-button @click="showCheckOutDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmCheckOut">确认退房</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCheckIns, createCheckIn, preCheckOut as preCheckOutApi, checkOut as checkOutApi } from '../api/checkin'
import { getRooms, getRoomsByType } from '../api/room'
import { formatDateTime } from '../utils/date'
import { getRoomTypes } from '../api/roomType'
import { state as authState } from '../stores/auth'

const checkIns = ref([])
const availableRooms = ref([])
const roomTypes = ref([])
const showAddDialog = ref(false)
const showCheckOutDialog = ref(false)
const currentCheckOutId = ref(null)
const checkOutResult = ref(null)

const checkOutForm = reactive({
  method: 'cash'
})

const itemTypeLabel = (t) => ({
  room_charge: '房费',
  food: '餐饮',
  beverage: '饮品',
  laundry: '洗衣',
  damage: '损坏赔偿',
  other: '其他'
}[t] || t)

const itemTypeTag = (t) => ({
  room_charge: '',
  food: 'success',
  beverage: 'warning',
  laundry: 'info',
  damage: 'danger',
  other: 'info'
}[t] || '')

const form = reactive({
  guestName: '',
  idType: 'id_card',
  idNumber: '',
  phone: '',
  roomTypeId: null,
  roomId: null,
  adults: 1,
  children: 0,
  expectedCheckOutTime: '',
  notes: '',
  stayGuests: []
})

const currentHotelId = computed(() => {
  return authState.staff?.hotelId || null
})

const selectedRoomType = computed(() => {
  return form.roomTypeId !== null && form.roomTypeId !== undefined
})

const getStatusType = (status) => {
  const types = {
    in_house: 'danger',
    checked_out: 'success',
    early_check_out: 'warning'
  }
  return types[status] || 'info'
}

const getStatusLabel = (status) => {
  const labels = {
    in_house: '在住',
    checked_out: '已退房',
    early_check_out: '提前退房'
  }
  return labels[status] || status
}

const loadCheckIns = async () => {
  try {
    const response = await getCheckIns()
    checkIns.value = (response.data || [])
      .map(c => ({
        ...c,
        guestName: c.guestName || '未登记',
        roomNumber: c.room?.roomNumber || '未分配'
      }))
      .sort((a, b) => {
        const timeA = a.checkInTime ? new Date(a.checkInTime).getTime() : 0
        const timeB = b.checkInTime ? new Date(b.checkInTime).getTime() : 0
        return timeB - timeA
      })
  } catch (error) {
    ElMessage.error('加载入住列表失败')
  }
}

const loadRoomTypes = async () => {
  try {
    const response = await getRoomTypes()
    let allRoomTypes = response.data || []
    if (currentHotelId.value) {
      allRoomTypes = allRoomTypes.filter(rt => rt.hotelId === currentHotelId.value)
    }
    roomTypes.value = allRoomTypes
  } catch (error) {
    console.error('加载房型失败', error)
  }
}

const loadAvailableRooms = async () => {
  try {
    const response = await getRooms()
    availableRooms.value = (response.data || []).filter(r => r.status === 'vacant')
  } catch (error) {
    console.error('加载房间失败', error)
  }
}

const handleRoomTypeChange = async (roomTypeId) => {
  form.roomId = null
  if (!roomTypeId) {
    availableRooms.value = []
    return
  }
  try {
    const response = await getRoomsByType(roomTypeId)
    let rooms = response.data || []
    if (currentHotelId.value) {
      rooms = rooms.filter(r => r.hotelId === currentHotelId.value)
    }
    availableRooms.value = rooms.filter(r => r.status === 'vacant')
  } catch (error) {
    console.error('加载房间失败', error)
    availableRooms.value = []
  }
}

const addStayGuest = () => {
  form.stayGuests.push({ name: '', idType: 'id_card', idNumber: '', isPrimary: false })
}

const removeStayGuest = (index) => {
  form.stayGuests.splice(index, 1)
}

const saveCheckIn = async () => {
  if (!form.guestName) {
    ElMessage.warning('请填写主登记人姓名')
    return
  }
  if (!form.idType) {
    ElMessage.warning('请选择证件类型')
    return
  }
  if (!form.idNumber) {
    ElMessage.warning('请填写证件号码')
    return
  }
  if (!form.phone) {
    ElMessage.warning('请填写联系电话')
    return
  }
  if (!form.roomId) {
    ElMessage.warning('请选择房间号')
    return
  }
  if (!form.adults || form.adults < 1) {
    ElMessage.warning('请填写至少1位成人')
    return
  }
  if (!form.expectedCheckOutTime) {
    ElMessage.warning('请选择预计退房时间')
    return
  }
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const checkOut = new Date(form.expectedCheckOutTime)
  const checkOutDate = new Date(checkOut.getFullYear(), checkOut.getMonth(), checkOut.getDate())
  if (checkOutDate <= today) {
    ElMessage.warning('预计退房日期必须晚于今天')
    return
  }

  const validStayGuests = form.stayGuests.filter(g => g.idNumber && g.name)

  // 校验人数一致性：填写人数（主登记人+同住人员）必须等于 成人+儿童 总数
  const totalRegistered = 1 + validStayGuests.length
  const totalDeclared = (form.adults || 0) + (form.children || 0)
  if (totalRegistered > totalDeclared) {
    ElMessage.warning(`登记人数(${totalRegistered})超过了填写的人数(${totalDeclared})，请减少同住人员或增加成人/儿童数`)
    return
  }
  if (totalRegistered < totalDeclared) {
    ElMessage.warning(`登记人数(${totalRegistered})少于填写的人数(${totalDeclared})，请补充同住人员信息`)
    return
  }
  
  const submitData = {
    guestName: form.guestName,
    idType: form.idType,
    idNumber: form.idNumber,
    phone: form.phone,
    roomId: form.roomId,
    adults: form.adults,
    children: form.children || 0,
    expectedCheckOutTime: form.expectedCheckOutTime,
    notes: form.notes,
    stayGuests: validStayGuests.length > 0 ? validStayGuests : undefined
  }
  
  try {
    await createCheckIn(submitData)
    ElMessage.success('入住成功')
    showAddDialog.value = false
    form.guestName = ''
    form.idType = 'id_card'
    form.idNumber = ''
    form.phone = ''
    form.roomTypeId = null
    form.roomId = null
    form.adults = 1
    form.children = 0
    form.expectedCheckOutTime = ''
    form.notes = ''
    form.stayGuests = []
    availableRooms.value = []
    loadCheckIns()
    loadRoomTypes()
  } catch (error) {
    const errMsg = error?.response?.data?.message || error?.message || '操作失败'
    ElMessage.error(errMsg)
  }
}

const handleCheckOut = async (row) => {
  try {
    const res = await preCheckOutApi(row.id)
    if (res.code === 200) {
      currentCheckOutId.value = row.id
      checkOutResult.value = res.data
      checkOutForm.method = 'cash'
      showCheckOutDialog.value = true
    } else {
      ElMessage.error(res.message || '获取费用信息失败')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '获取费用信息失败')
  }
}

const confirmCheckOut = async () => {
  if (!checkOutResult.value) return

  const hasDiff = checkOutResult.value.needRefund || checkOutResult.value.needPay
  const data = {
    paymentMethod: checkOutResult.value.needPay ? checkOutForm.method : null,
    refundMethod: checkOutResult.value.needRefund ? checkOutForm.method : null
  }

  try {
    const res = await checkOutApi(currentCheckOutId.value, data)
    if (res.code === 200) {
      ElMessage.success('退房成功')
      showCheckOutDialog.value = false
      checkOutResult.value = null
      loadCheckIns()
      loadRoomTypes()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '操作失败')
  }
}

onMounted(() => {
  loadCheckIns()
  loadRoomTypes()
})
</script>

