<template>
  <div class="consumable-order-page">
    <h2 class="page-title">消费下单</h2>

    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="房间号">
          <el-input v-model="searchForm.roomNumber" placeholder="请输入房间号" style="width:200px" @keyup.enter="searchCheckIn" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchCheckIn">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="checkInInfo" class="info-card">
      <template #header>
        <div class="card-header">
          <span>在住信息</span>
          <el-tag type="success">已入住</el-tag>
        </div>
      </template>
      <el-descriptions :column="4" border>
        <el-descriptions-item label="房间号">{{ checkInInfo.roomNumber }}</el-descriptions-item>
        <el-descriptions-item label="客人姓名">{{ checkInInfo.guestName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="入住ID">{{ checkInInfo.checkInId }}</el-descriptions-item>
        <el-descriptions-item label="账单ID">{{ checkInInfo.billId }}</el-descriptions-item>
        <el-descriptions-item label="已消费金额">
          <span class="amount">¥{{ Number(checkInInfo.totalAmount || 0).toFixed(2) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="押金">
          <span class="amount">¥{{ Number(checkInInfo.depositAmount || 0).toFixed(2) }}</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="order-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span>本酒店消费项目</span>
            <el-tag type="info">{{ filteredConsumableList.length }} 项</el-tag>
          </div>
          <el-select v-model="selectedCategory" placeholder="分类筛选" clearable size="small" style="width:140px">
            <el-option label="餐饮" value="food" />
            <el-option label="饮品" value="beverage" />
            <el-option label="洗衣" value="laundry" />
            <el-option label="其他" value="other" />
          </el-select>
        </div>
      </template>

      <div class="consumable-list">
        <div v-for="item in filteredConsumableList" :key="item.id" class="consumable-item">
          <div class="item-info">
            <div class="item-name">
              {{ item.name }}
              <el-tag :type="getCategoryTagType(item.category)" size="small">{{ getCategoryLabel(item.category) }}</el-tag>
            </div>
            <div class="item-price">¥{{ Number(item.price).toFixed(2) }} / 份</div>
          </div>
          <div class="item-action">
            <el-input-number v-model="item.quantity" :min="0" :max="99" size="small" />
            <el-button type="primary" size="small" @click="addToCart(item)">添加</el-button>
          </div>
        </div>
        <el-empty v-if="filteredConsumableList.length === 0" description="本酒店暂无可用的消费项目" />
      </div>
    </el-card>

    <el-card v-if="cartItems.length > 0" class="cart-card">
      <template #header>
        <div class="card-header">
          <span>购物车</span>
          <el-tag type="warning">{{ cartItems.length }} 项</el-tag>
        </div>
      </template>

      <el-table :data="cartItems" border>
        <el-table-column prop="name" label="项目名称" />
        <el-table-column label="分类" width="100">
          <template #default="{ row }">
            <el-tag :type="getCategoryTagType(row.category)" size="small">{{ getCategoryLabel(row.category) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="100">
          <template #default="{ row }">¥{{ Number(row.price).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="数量" width="80" align="center">
          <template #default="{ row }">
            <span>{{ row.quantity }}</span>
          </template>
        </el-table-column>
        <el-table-column label="小计" width="120" align="right">
          <template #default="{ row }">
            <span class="amount">¥{{ (Number(row.price) * Number(row.quantity)).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ $index }">
            <el-button type="danger" size="small" link @click="removeFromCart($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="cart-footer">
        <div class="total">
          合计：<span class="amount">¥{{ cartTotal.toFixed(2) }}</span>
        </div>
        <el-button type="primary" size="large" @click="submitOrder" :loading="submitting">确认下单</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCheckInInfoByRoomNumber, addConsumableToBill, getActiveConsumableItems } from '../api/consumableOrder'

const searchForm = reactive({
  roomNumber: ''
})

const checkInInfo = ref(null)
const consumableList = ref([])
const cartItems = ref([])
const submitting = ref(false)
const selectedCategory = ref('')

const filteredConsumableList = computed(() => {
  if (!selectedCategory.value) {
    return consumableList.value
  }
  return consumableList.value.filter(item => item.category === selectedCategory.value)
})

const cartTotal = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + Number(item.price) * Number(item.quantity), 0)
})

const getCategoryLabel = (cat) => {
  const map = { food: '餐饮', beverage: '饮品', laundry: '洗衣', other: '其他' }
  return map[cat] || cat
}

const getCategoryTagType = (cat) => {
  const map = { food: 'success', beverage: 'warning', laundry: 'info', other: '' }
  return map[cat] || ''
}

const loadConsumableList = async () => {
  try {
    const res = await getActiveConsumableItems()
    if (res.code === 200) {
      consumableList.value = res.data || []
      consumableList.value.forEach(item => {
        item.quantity = 0
      })
    }
  } catch (e) {
    console.error('加载消费项目失败:', e)
  }
}

const searchCheckIn = async () => {
  if (!searchForm.roomNumber) {
    ElMessage.warning('请输入房间号')
    return
  }

  try {
    const res = await getCheckInInfoByRoomNumber(searchForm.roomNumber)
    if (res.code === 200) {
      checkInInfo.value = res.data
      cartItems.value = []
      ElMessage.success(`查询成功：${res.data.guestName || '客人'} 正在入住`)
    } else {
      ElMessage.error(res.message)
      checkInInfo.value = null
      cartItems.value = []
    }
  } catch (e) {
    ElMessage.error('查询失败')
    checkInInfo.value = null
  }
}

const addToCart = (item) => {
  if (!checkInInfo.value) {
    ElMessage.warning('请先输入房间号查询在住信息')
    return
  }
  const qty = Number(item.quantity)
  if (qty <= 0) {
    ElMessage.warning('请先选择数量')
    return
  }

  const existing = cartItems.value.find(i => i.id === item.id)
  if (existing) {
    existing.quantity = qty
  } else {
    cartItems.value.push({
      id: item.id,
      name: item.name,
      category: item.category,
      price: Number(item.price),
      quantity: qty
    })
  }

  item.quantity = 0
  ElMessage.success(`已添加 ${item.name}`)
}

const removeFromCart = (index) => {
  cartItems.value.splice(index, 1)
}

const submitOrder = async () => {
  if (cartItems.value.length === 0) {
    ElMessage.warning('购物车为空')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认下单 ${cartItems.value.length} 项消费项目，合计 ¥${cartTotal.value.toFixed(2)}？`,
      '确认下单',
      { type: 'warning' }
    )
  } catch {
    return
  }

  submitting.value = true
  let successCount = 0
  let failCount = 0

  for (const item of cartItems.value) {
    try {
      const res = await addConsumableToBill({
        checkInId: checkInInfo.value.checkInId,
        consumableId: item.id,
        quantity: Number(item.quantity),
        description: item.name
      })
      if (res.code === 200) {
        successCount++
      } else {
        failCount++
        ElMessage.error(`${item.name}: ${res.message}`)
      }
    } catch (e) {
      failCount++
      console.error('添加失败:', item.name, e)
    }
  }

  submitting.value = false

  if (successCount > 0) {
    ElMessage.success(`成功下单 ${successCount} 项${failCount > 0 ? `，失败 ${failCount} 项` : ''}`)
    cartItems.value = []
    searchCheckIn()
  } else {
    ElMessage.error('下单失败')
  }
}

onMounted(() => {
  loadConsumableList()
})
</script>
