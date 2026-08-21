<template>
  <div class="page">
    <div class="page-header">
      <h2>账单管理</h2>
    </div>

    <el-form :inline="true" :model="filterForm" class="search-form">
      <el-form-item label="账单状态">
        <el-select v-model="filterForm.status" placeholder="全部状态" clearable style="width: 150px">
          <el-option label="未结算" value="open" />
          <el-option label="已结算" value="closed" />
          <el-option label="作废" value="void" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词">
        <el-input v-model="filterForm.keyword" placeholder="客人姓名或房间号" clearable style="width: 200px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadBills">查询</el-button>
        <el-button @click="resetFilter">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="filteredBills" border stripe>
      <el-table-column prop="id" label="ID" width="60" align="center" />
      <el-table-column prop="guestName" label="客人姓名">
        <template #default="{ row }">{{ row.guestName || '-' }}</template>
      </el-table-column>
      <el-table-column prop="roomNumber" label="房间号" width="80" align="center" />
      <el-table-column prop="billStatus" label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.billStatus)" size="small">{{ getStatusLabel(row.billStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="总金额" align="right">
        <template #default="{ row }">¥{{ Number(row.totalAmount || 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="paidAmount" label="已付" align="right">
        <template #default="{ row }">¥{{ Number(row.paidAmount || 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="depositAmount" label="押金" align="right">
        <template #default="{ row }">¥{{ Number(row.depositAmount || 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="退款" align="right">
        <template #default="{ row }">
          <span style="color: #409eff" v-if="Number(row.refundAmount || 0) > 0">¥{{ Number(row.refundAmount).toFixed(2) }}</span>
          <span style="color: #c0c4cc" v-else>¥0.00</span>
        </template>
      </el-table-column>
      <el-table-column label="补款" align="right">
        <template #default="{ row }">
          <span style="color: #e6a23c" v-if="Number(row.additionalPaymentAmount || 0) > 0">¥{{ Number(row.additionalPaymentAmount).toFixed(2) }}</span>
          <span style="color: #c0c4cc" v-else>¥0.00</span>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right" align="center">
        <template #default="{ row }">
          <el-button size="small" @click="viewBill(row)">详情</el-button>
          <el-button
            size="small"
            type="warning"
            v-if="row.billStatus === 'open' && row.hasDamageItem"
            @click="handleSettle(row)"
          >结算</el-button>
          <el-button
            size="small"
            type="danger"
            v-if="row.billStatus === 'closed'"
            @click="handleVoid(row)"
          >作废</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showDetailDialog" title="账单详情" width="900px" top="5vh">
      <div v-if="currentBill">
        <el-descriptions title="基本信息" :column="3" border>
          <el-descriptions-item label="账单ID">{{ currentBill.id }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentBill.billStatus)" effect="dark">{{ getStatusLabel(currentBill.billStatus) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(currentBill.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="客人姓名">{{ currentBill.guestName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="房间号">{{ currentBill.roomNumber || '-' }}</el-descriptions-item>
          <el-descriptions-item label="结算时间">{{ formatDateTime(currentBill.closedAt) }}</el-descriptions-item>
        </el-descriptions>

        <el-descriptions title="金额汇总" :column="3" border style="margin-top: 16px">
          <el-descriptions-item label="押金">
            ¥{{ Number(currentBill.depositAmount || 0).toFixed(2) }}
          </el-descriptions-item>
          <el-descriptions-item label="房费">
            ¥{{ getRoomCharge(currentBill) }}
          </el-descriptions-item>
          <el-descriptions-item label="消费">
            ¥{{ getOtherCharge(currentBill) }}
          </el-descriptions-item>
          <el-descriptions-item label="总金额">
            <span style="font-weight: bold; color: #f56c6c; font-size: 18px">¥{{ Number(currentBill.totalAmount || 0).toFixed(2) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="已付金额">
            ¥{{ Number(currentBill.paidAmount || 0).toFixed(2) }}
          </el-descriptions-item>
          <el-descriptions-item label="退款金额">
            <span style="color: #409eff">¥{{ Number(currentBill.refundAmount || 0).toFixed(2) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="补款金额">
            <span style="color: #e6a23c">¥{{ Number(currentBill.additionalPaymentAmount || 0).toFixed(2) }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <el-tabs v-model="activeTab" style="margin-top: 16px">
          <el-tab-pane label="账单明细" name="items">
            <el-table :data="billItems" border stripe size="small">
              <el-table-column prop="itemType" label="类型" width="100">
                <template #default="{ row }">
                  <el-tag size="small">{{ getItemTypeLabel(row.itemType) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="description" label="描述" min-width="150" />
              <el-table-column prop="quantity" label="数量" width="80" align="right" />
              <el-table-column prop="unitPrice" label="单价" width="100" align="right">
                <template #default="{ row }">¥{{ Number(row.unitPrice).toFixed(2) }}</template>
              </el-table-column>
              <el-table-column prop="amount" label="小计" width="100" align="right">
                <template #default="{ row }">¥{{ Number(row.amount).toFixed(2) }}</template>
              </el-table-column>
              <el-table-column prop="chargeDate" label="日期" width="120">
                <template #default="{ row }">{{ formatDate(row.chargeDate) }}</template>
              </el-table-column>
            </el-table>
            <div v-if="billItems.length === 0" class="empty-tip">暂无账单明细</div>
          </el-tab-pane>

          <el-tab-pane label="收款记录" name="payments">
            <el-table :data="payments" border stripe size="small">
              <el-table-column prop="paymentType" label="类型" width="120">
                <template #default="{ row }">
                  <el-tag :type="getPaymentTagType(row)" size="small">
                    {{ getPaymentTypeLabel(row) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="paymentMethod" label="方式" width="100">
                <template #default="{ row }">{{ getMethodLabel(row.paymentMethod) }}</template>
              </el-table-column>
              <el-table-column prop="amount" label="金额" width="120" align="right">
                <template #default="{ row }">¥{{ Number(row.amount).toFixed(2) }}</template>
              </el-table-column>
              <el-table-column prop="paymentDate" label="时间" width="160">
                <template #default="{ row }">{{ formatDateTime(row.paymentDate) }}</template>
              </el-table-column>
              <el-table-column prop="employeeId" label="操作员" width="80" align="center">
                <template #default="{ row }">{{ row.employeeId || '-' }}</template>
              </el-table-column>
              <el-table-column prop="transactionRef" label="备注" min-width="150">
                <template #default="{ row }">{{ row.transactionRef || '-' }}</template>
              </el-table-column>
            </el-table>
            <div v-if="payments.length === 0" class="empty-tip">暂无收款记录</div>
          </el-tab-pane>

          <el-tab-pane label="退款记录" name="refunds">
            <el-table :data="refunds" border stripe size="small">
              <el-table-column prop="refundMethod" label="方式" width="120">
                <template #default="{ row }">{{ getMethodLabel(row.refundMethod) }}</template>
              </el-table-column>
              <el-table-column prop="amount" label="金额" width="120" align="right">
                <template #default="{ row }">
                  <span style="color: #409eff">¥{{ Number(row.amount).toFixed(2) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="refundDate" label="时间" width="160">
                <template #default="{ row }">{{ formatDateTime(row.refundDate) }}</template>
              </el-table-column>
              <el-table-column prop="employeeId" label="操作员" width="80" align="center">
                <template #default="{ row }">{{ row.employeeId || '-' }}</template>
              </el-table-column>
              <el-table-column prop="notes" label="备注" min-width="100" />
            </el-table>
            <div v-if="refunds.length === 0" class="empty-tip">暂无退款记录</div>
          </el-tab-pane>
        </el-tabs>
      </div>

      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showSettleDialog" title="账单结算 - 选择支付方式" width="480px" top="15vh">
      <div v-if="settleBillData">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="账单ID">{{ settleBillData.id }}</el-descriptions-item>
          <el-descriptions-item label="房间号">{{ settleBillData.roomNumber || '-' }}</el-descriptions-item>
          <el-descriptions-item label="客人姓名">{{ settleBillData.guestName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="结算金额">
            <span style="color: #f56c6c; font-weight: bold; font-size: 16px">
              ¥{{ Number(settleBillData.totalAmount || 0).toFixed(2) }}
            </span>
          </el-descriptions-item>
        </el-descriptions>

        <el-form :model="settleForm" label-width="90px" style="margin-top: 20px">
          <el-form-item label="支付方式" required>
            <el-radio-group v-model="settleForm.paymentMethod" style="width: 100%">
              <el-radio value="cash" border style="margin-bottom: 8px">现金</el-radio>
              <el-radio value="wechat" border style="margin-bottom: 8px">微信</el-radio>
              <el-radio value="alipay" border style="margin-bottom: 8px">支付宝</el-radio>
              <el-radio value="credit_card" border style="margin-bottom: 8px">信用卡</el-radio>
              <el-radio value="debit_card" border style="margin-bottom: 8px">借记卡</el-radio>
              <el-radio value="bank_transfer" border>银行转账</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="cancelSettle">取消</el-button>
        <el-button type="warning" :disabled="!settleForm.paymentMethod" @click="confirmSettle">
          确认结算
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDateTime, formatDate } from '../utils/date'
import { getBills, settleBill, voidBill } from '../api/bill'
import { getBillItems } from '../api/billItem'
import { getPaymentsByBillId } from '../api/payment'
import { getRefundsByBillId } from '../api/refund'

const bills = ref([])
const billItems = ref([])
const payments = ref([])
const refunds = ref([])
const showDetailDialog = ref(false)
const currentBill = ref(null)
const activeTab = ref('items')
const showSettleDialog = ref(false)
const settleBillData = ref(null)
const settleForm = reactive({
  paymentMethod: ''
})

const filterForm = reactive({
  status: '',
  keyword: ''
})

const filteredBills = computed(() => {
  return bills.value.filter(b => {
    if (filterForm.status && b.billStatus !== filterForm.status) return false
    if (filterForm.keyword) {
      const kw = filterForm.keyword.toLowerCase()
      const name = (b.guestName || '').toLowerCase()
      const room = (b.roomNumber || '').toLowerCase()
      if (!name.includes(kw) && !room.includes(kw)) return false
    }
    return true
  })
})

const getStatusType = (status) => {
  const types = { open: 'warning', closed: 'success', void: 'danger' }
  return types[status] || 'info'
}

const getStatusLabel = (status) => {
  const labels = { open: '未结算', closed: '已结算', void: '作废' }
  return labels[status] || status
}

const getItemTypeLabel = (type) => {
  const labels = { room_charge: '房费', food: '餐饮', beverage: '酒水', laundry: '洗衣', damage: '损坏赔偿', other: '其他' }
  return labels[type] || type
}

const getMethodLabel = (method) => {
  const labels = { cash: '现金', credit_card: '信用卡', debit_card: '借记卡', wechat: '微信', alipay: '支付宝', bank_transfer: '银行转账' }
  return labels[method] || method
}

const getPaymentTypeLabel = (row) => {
  if (row.paymentType === 'deposit') return '押金'
  if (row.paymentType === 'charge') {
    if (row.transactionRef && row.transactionRef.includes('损坏赔偿')) return '损坏赔偿结算'
    return '补价'
  }
  return row.paymentType || ''
}

const getPaymentTagType = (row) => {
  if (row.paymentType === 'deposit') return 'info'
  if (row.paymentType === 'charge') {
    if (row.transactionRef && row.transactionRef.includes('损坏赔偿')) return 'warning'
    return 'primary'
  }
  return 'primary'
}

const getRoomCharge = (row) => {
  return Number(row.roomCharge || 0).toFixed(2)
}

const getOtherCharge = (row) => {
  return Number(row.additionalCharges || 0).toFixed(2)
}

const loadBills = async () => {
  try {
    const response = await getBills()
    bills.value = (response.data || []).sort((a, b) => {
      const timeA = a.createdAt ? new Date(a.createdAt).getTime() : 0
      const timeB = b.createdAt ? new Date(b.createdAt).getTime() : 0
      return timeB - timeA
    })
  } catch (error) {
    ElMessage.error('加载账单列表失败')
  }
}

const resetFilter = () => {
  filterForm.status = ''
  filterForm.keyword = ''
}

const viewBill = async (row) => {
  currentBill.value = row
  activeTab.value = 'items'
  try {
    const [itemsRes, paymentsRes, refundsRes] = await Promise.all([
      getBillItems(row.id).catch(() => ({ data: [] })),
      getPaymentsByBillId(row.id).catch(() => ({ data: [] })),
      getRefundsByBillId(row.id).catch(() => ({ data: [] }))
    ])
    billItems.value = itemsRes.data || []
    payments.value = paymentsRes.data || []
    refunds.value = refundsRes.data || []
  } catch (error) {
    billItems.value = []
    payments.value = []
    refunds.value = []
  }
  showDetailDialog.value = true
}

const handleSettle = (row) => {
  settleBillData.value = row
  settleForm.paymentMethod = ''
  showSettleDialog.value = true
}

const cancelSettle = () => {
  showSettleDialog.value = false
  settleBillData.value = null
}

const confirmSettle = async () => {
  if (!settleForm.paymentMethod) {
    ElMessage.warning('请选择支付方式')
    return
  }
  try {
    const res = await settleBill(
      settleBillData.value.id,
      settleForm.paymentMethod
    )
    if (res.code === 200) {
      ElMessage.success(`结算成功，支付方式：${getMethodLabel(settleForm.paymentMethod)}`)
      showSettleDialog.value = false
      settleBillData.value = null
      loadBills()
    } else {
      ElMessage.error(res.message || '结算失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '结算失败')
    }
  }
}

const handleVoid = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要作废账单 #${row.id} 吗？作废后账单将失效，此操作不可逆。`,
      '作废确认',
      { confirmButtonText: '确定作废', cancelButtonText: '取消', type: 'error' }
    )
    const res = await voidBill(row.id)
    if (res.code === 200) {
      ElMessage.success('作废成功')
      loadBills()
    } else {
      ElMessage.error(res.message || '作废失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '作废失败')
    }
  }
}

onMounted(() => {
  loadBills()
})
</script>
