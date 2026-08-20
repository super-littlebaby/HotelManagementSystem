<template>
  <div class="page consumable-page">
    <div class="page-header">
      <h2>可消费项目管理</h2>
      <el-button type="primary" @click="openAddDialog">添加消费项目</el-button>
    </div>

    <div class="search-form consumable-search">
      <el-form :inline="true">
        <el-form-item label="酒店">
          <el-select v-if="canSelectHotel" v-model="searchHotelId" placeholder="全部酒店" clearable @change="loadItems" style="width:180px">
            <el-option v-for="h in hotelList" :key="h.id" :label="h.name" :value="h.id" />
          </el-select>
          <div v-else class="hotel-tag-fixed">{{ currentHotelName }}</div>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchCategory" placeholder="全部分类" clearable @change="loadItems" style="width:140px">
            <el-option v-for="c in categoryOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchActive" placeholder="全部状态" clearable @change="loadItems" style="width:120px">
            <el-option label="启用" :value="true" />
            <el-option label="停用" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="items" border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="品名" />
      <el-table-column label="分类" width="120">
        <template #default="{ row }">
          <el-tag :type="getCategoryType(row.category)">{{ getCategoryLabel(row.category) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="价格" width="120">
        <template #default="{ row }">¥{{ Number(row.price).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.isActive ? 'success' : 'info'">{{ row.isActive ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="editItem(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteItem(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showDialog" :title="form.id ? '编辑消费项目' : '添加消费项目'" width="450px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="品名" required>
          <el-input v-model="form.name" placeholder="如：自助早餐、干洗西装" />
        </el-form-item>
        <el-form-item label="分类" required>
          <el-select v-model="form.category" placeholder="请选择分类" style="width:100%">
            <el-option v-for="c in categoryOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" required>
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="10" />
        </el-form-item>
        <el-form-item label="酒店" v-if="canSelectHotel">
          <el-select v-model="form.hotelId" placeholder="请选择酒店" style="width:100%">
            <el-option v-for="h in hotelList" :key="h.id" :label="h.name" :value="h.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="酒店" v-else>
          <div class="hotel-tag-fixed">{{ currentHotelName }}</div>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.isActive" :active-value="true" :inactive-value="false" active-text="启用" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="saveItem">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getConsumableItems,
  getConsumableItemsByHotelId,
  getConsumableItemsByCategory,
  getConsumableItemsByIsActive,
  getConsumableItemsByHotelAndCategory,
  getConsumableItemsByHotelAndIsActive,
  createConsumableItem,
  updateConsumableItem,
  deleteConsumableItem
} from '../api/consumable'
import { getHotels } from '../api/hotel'
import { state } from '../stores/auth'

const items = ref([])
const hotelList = ref([])
const showDialog = ref(false)
const searchHotelId = ref(null)
const searchCategory = ref('')
const searchActive = ref(null)

const form = reactive({
  id: null,
  name: '',
  category: '',
  price: 0,
  hotelId: null,
  isActive: true
})

const categoryOptions = [
  { value: 'food', label: '餐饮' },
  { value: 'beverage', label: '饮品' },
  { value: 'laundry', label: '洗衣' },
  { value: 'other', label: '其他' }
]

const canSelectHotel = computed(() => state.staff?.role === 'admin' && state.staff?.hotelId == null)
const isHotelLocked = computed(() => !canSelectHotel.value)
const currentHotelName = computed(() => {
  const hid = state.staff?.hotelId
  const h = hotelList.value.find(x => x.id === hid)
  return h ? h.name : ''
})

const getCurrentHotelId = () => state.staff?.hotelId || null

const getCategoryLabel = (v) => categoryOptions.find(c => c.value === v)?.label || v
const getCategoryType = (v) => {
  const map = { food: '', beverage: 'warning', laundry: 'success', other: 'info' }
  return map[v] || ''
}

const resetForm = () => {
  Object.assign(form, { id: null, name: '', category: '', price: 0, hotelId: state.staff?.hotelId || null, isActive: true })
}

const resetSearch = () => {
  if (canSelectHotel.value) {
    searchHotelId.value = null
  } else {
    searchHotelId.value = getCurrentHotelId()
  }
  searchCategory.value = ''
  searchActive.value = null
  loadItems()
}

const loadHotels = async () => {
  try {
    const res = await getHotels()
    hotelList.value = res.data || []
  } catch (e) {}
}

const loadItems = async () => {
  try {
    let res
    const hotelId = isHotelLocked.value ? getCurrentHotelId() : (searchHotelId.value || getCurrentHotelId())
    if (hotelId && searchCategory.value) {
      res = await getConsumableItemsByHotelAndCategory(hotelId, searchCategory.value)
    } else if (hotelId && searchActive.value !== null && searchActive.value !== '') {
      res = await getConsumableItemsByHotelAndIsActive(hotelId, searchActive.value)
    } else if (searchCategory.value && canSelectHotel.value) {
      res = await getConsumableItemsByCategory(searchCategory.value)
    } else if (searchActive.value !== null && searchActive.value !== '' && canSelectHotel.value) {
      res = await getConsumableItemsByIsActive(searchActive.value)
    } else if (hotelId) {
      res = await getConsumableItemsByHotelId(hotelId)
    } else {
      res = await getConsumableItems()
    }
    items.value = res.data || []
  } catch (error) {
    ElMessage.error('加载失败')
  }
}

const openAddDialog = () => {
  resetForm()
  showDialog.value = true
}

const editItem = (row) => {
  Object.assign(form, {
    id: row.id,
    name: row.name,
    category: row.category,
    price: row.price,
    hotelId: row.hotelId,
    isActive: row.isActive
  })
  showDialog.value = true
}

const saveItem = async () => {
  if (!form.name) { ElMessage.warning('请填写品名'); return }
  if (!form.category) { ElMessage.warning('请选择分类'); return }
  if (form.price == null || form.price < 0) { ElMessage.warning('请输入有效价格'); return }

  if (isHotelLocked.value) {
    form.hotelId = getCurrentHotelId()
  }

  try {
    if (form.id) {
      await updateConsumableItem(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createConsumableItem(form)
      ElMessage.success('创建成功')
    }
    showDialog.value = false
    loadItems()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const deleteItem = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除「${row.name}」吗？`, '提示', { type: 'warning' })
    await deleteConsumableItem(row.id)
    ElMessage.success('删除成功')
    loadItems()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(async () => {
  await loadHotels()
  if (isHotelLocked.value) {
    searchHotelId.value = getCurrentHotelId()
  }
  loadItems()
})
</script>
