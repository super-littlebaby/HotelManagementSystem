<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElLoading } from 'element-plus'
import { getHotels, searchHotelsByName } from '../api/hotel'

const router = useRouter()
const hotels = ref([])
const searchKeyword = ref('')
const loading = ref(false)

onMounted(() => {
  loadHotels()
})

const loadHotels = async () => {
  loading.value = true
  try {
    const res = await getHotels()
    if (res.code === 200) {
      hotels.value = res.data
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('加载酒店列表失败')
  } finally {
    loading.value = false
  }
}

const search = async () => {
  if (!searchKeyword.value.trim()) {
    loadHotels()
    return
  }
  loading.value = true
  try {
    const res = await searchHotelsByName(searchKeyword.value)
    if (res.code === 200) {
      hotels.value = res.data
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

const goToHotelDetail = (id) => {
  router.push(`/hotel/${id}`)
}
</script>

<template>
  <div class="home-page">
    <div class="hero-section">
      <div class="container">
        <h2>欢迎来到酒店管理系统</h2>
        <p>为您提供优质的酒店预订服务</p>
        <div class="search-box">
          <input 
            v-model="searchKeyword" 
            type="text" 
            placeholder="搜索酒店名称..." 
            @keyup.enter="search"
          />
          <button @click="search">搜索</button>
        </div>
      </div>
    </div>
    
    <div class="container">
      <div class="section-title">
        <h3>推荐酒店</h3>
      </div>
      
      <div class="hotel-grid" v-loading="loading">
        <div 
          class="hotel-card" 
          v-for="hotel in hotels" 
          :key="hotel.id"
          @click="goToHotelDetail(hotel.id)"
        >
          <div class="hotel-image">
            <img :src="`https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=luxury%20hotel%20building%20exterior%20modern%20architecture&image_size=landscape_4_3`" alt="酒店图片" />
          </div>
          <div class="hotel-info">
            <h4>{{ hotel.name }}</h4>
            <p class="address">📍 {{ hotel.address }}</p>
            <p class="description">{{ hotel.description }}</p>
            <div class="price">
              <span class="currency">¥</span>
              <span class="amount">{{ hotel.minPrice ? hotel.minPrice.toFixed(0) : '暂无' }}</span>
              <span class="unit">起/晚</span>
            </div>
          </div>
        </div>
      </div>
      
      <div v-if="hotels.length === 0 && !loading" class="empty-state">
        <p>暂无酒店信息</p>
      </div>
    </div>
  </div>
</template>


