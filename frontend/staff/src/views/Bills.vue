<template>
  <div class="page">
    <div class="page-header">
      <h2>账单管理</h2>
    </div>
    
    <el-table :data="bills" border>
      <el-table-column prop="id" label="账单ID" width="80" />
      <el-table-column prop="guestName" label="客人" />
      <el-table-column prop="roomNumber" label="房间号" />
      <el-table-column prop="billStatus" label="状态">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.billStatus)">{{ getStatusLabel(row.billStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="总金额">
        <template #default="{ row }">¥{{ row.totalAmount || 0 }}</template>
      </el-table-column>
      <el-table-column prop="paidAmount" label="已付金额">
        <template #default="{ row }">¥{{ row.paidAmount || 0 }}</template>
      </el-table-column>
      <el-table-column prop="depositAmount" label="押金">
        <template #default="{ row }">¥{{ row.depositAmount || 0 }}</template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" />
      <el-table-column prop="closedAt" label="结算时间" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="viewBill(row)">查看详情</el-button>
          <el-button size="small" type="primary" @click="closeBill(row)" v-if="row.billStatus === 'open'">结算</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <el-dialog v-model="showDetailDialog" title="账单详情" width="600px">
      <div v-if="currentBill">
        <el-form :model="currentBill" label-width="100px">
          <el-form-item label="客人">
            <span>{{ currentBill.guestName }}</span>
          </el-form-item>
          <el-form-item label="房间">
            <span>{{ currentBill.roomNumber }}</span>
          </el-form-item>
          <el-form-item label="状态">
            <el-tag :type="getStatusType(currentBill.billStatus)">{{ getStatusLabel(currentBill.billStatus) }}</el-tag>
          </el-form-item>
          <el-form-item label="押金">
            <span>¥{{ currentBill.depositAmount || 0 }}</span>
          </el-form-item>
          <el-form-item label="总金额">
            <span>¥{{ currentBill.totalAmount || 0 }}</span>
          </el-form-item>
          <el-form-item label="已付金额">
            <span>¥{{ currentBill.paidAmount || 0 }}</span>
          </el-form-item>
          <el-form-item label="待付金额">
            <span>¥{{ (currentBill.totalAmount - currentBill.paidAmount) || 0 }}</span>
          </el-form-item>
        </el-form>
        
        <h4 style="margin-top: 20px">账单明细</h4>
        <el-table :data="billItems" border>
          <el-table-column prop="itemType" label="类型">
            <template #default="{ row }">{{ getItemTypeLabel(row.itemType) }}</template>
          </el-table-column>
          <el-table-column prop="description" label="描述" />
          <el-table-column prop="quantity" label="数量" />
          <el-table-column prop="unitPrice" label="单价">
            <template #default="{ row }">¥{{ row.unitPrice }}</template>
          </el-table-column>
          <el-table-column prop="amount" label="小计">
            <template #default="{ row }">¥{{ row.amount }}</template>
          </el-table-column>
          <el-table-column prop="chargeDate" label="日期" />
        </el-table>
      </div>
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getBills, updateBill } from '../api/bill'
import { getBillItems } from '../api/billItem'

const bills = ref([])
const billItems = ref([])
const showDetailDialog = ref(false)
const currentBill = ref(null)

const getStatusType = (status) => {
  const types = {
    open: 'warning',
    closed: 'success',
    void: 'danger'
  }
  return types[status] || 'info'
}

const getStatusLabel = (status) => {
  const labels = {
    open: '未结算',
    closed: '已结算',
    void: '作废'
  }
  return labels[status] || status
}

const getItemTypeLabel = (type) => {
  const labels = {
    room_charge: '房费',
    food: '餐饮',
    beverage: '酒水',
    laundry: '洗衣',
    damage: '损坏赔偿',
    other: '其他'
  }
  return labels[type] || type
}

const loadBills = async () => {
  try {
    const response = await getBills()
    bills.value = (response.data || []).map(b => ({
      ...b,
      guestName: b.checkIn?.guest?.firstName ? `${b.checkIn.guest.firstName} ${b.checkIn.guest.lastName}` : '',
      roomNumber: b.checkIn?.room?.roomNumber
    }))
  } catch (error) {
    ElMessage.error('加载账单列表失败')
  }
}

const viewBill = async (row) => {
  currentBill.value = row
  try {
    const response = await getBillItems(row.id)
    billItems.value = response.data || []
  } catch (error) {
    billItems.value = []
  }
  showDetailDialog.value = true
}

const closeBill = async (row) => {
  try {
    await updateBill(row.id, { ...row, billStatus: 'closed', closedAt: new Date().toISOString() })
    ElMessage.success('结算成功')
    loadBills()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadBills()
})
</script>

<style scoped>
.page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}
</style>
