<template>
  <div class="page">
    <div class="page-header">
      <h2>设施管理</h2>
      <el-button type="primary" @click="showAddDialog = true">添加设施</el-button>
    </div>
    
    <el-table :data="facilities" border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="设施名称" />
      <el-table-column prop="price" label="赔偿价格" width="120">
        <template #default="{ row }">
          ¥{{ row.price }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="editFacility(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteFacility(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <el-dialog v-model="showAddDialog" title="添加设施" width="400px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="设施名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="赔偿价格">
          <el-input v-model.number="form.price" type="number" step="0.01" placeholder="损坏赔偿单价" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveFacility">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getFacilities, createFacility, updateFacility, deleteFacility as deleteFacilityApi } from '../api/facility'

const facilities = ref([])
const showAddDialog = ref(false)

const form = reactive({
  id: null,
  name: '',
  price: 0
})

const loadFacilities = async () => {
  try {
    const response = await getFacilities()
    facilities.value = response.data || []
  } catch (error) {
    ElMessage.error('加载设施列表失败')
  }
}

const editFacility = (row) => {
  Object.assign(form, {
    id: row.id,
    name: row.name,
    price: row.price
  })
  showAddDialog.value = true
}

const saveFacility = async () => {
  if (!form.name) {
    ElMessage.warning('请填写设施名称')
    return
  }
  
  try {
    if (form.id) {
      await updateFacility(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createFacility(form)
      ElMessage.success('创建成功')
    }
    showAddDialog.value = false
    loadFacilities()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const deleteFacility = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该设施吗？', '提示', {
      type: 'warning'
    })
    await deleteFacilityApi(row.id)
    ElMessage.success('删除成功')
    loadFacilities()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadFacilities()
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