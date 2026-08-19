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

<style scoped>
.home-page {
  min-height: calc(100vh - 140px);
}

.hero-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  padding: 60px 0;
  text-align: center;
}

.hero-section h2 {
  font-size: 36px;
  margin-bottom: 10px;
}

.hero-section p {
  font-size: 18px;
  margin-bottom: 30px;
}

.search-box {
  display: flex;
  max-width: 500px;
  margin: 0 auto;
}

.search-box input {
  flex: 1;
  padding: 15px 20px;
  border: none;
  border-radius: 25px 0 0 25px;
  font-size: 16px;
}

.search-box button {
  padding: 15px 30px;
  border: none;
  border-radius: 0 25px 25px 0;
  background: #fff;
  color: #667eea;
  font-size: 16px;
  cursor: pointer;
}

.search-box button:hover {
  background: #f0f0f0;
}

.section-title {
  margin: 30px 0;
}

.section-title h3 {
  font-size: 24px;
  color: #333;
}

.hotel-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.hotel-card {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.hotel-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.15);
}

.hotel-image img {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.hotel-info {
  padding: 20px;
}

.hotel-info h4 {
  font-size: 20px;
  margin-bottom: 10px;
  color: #333;
}

.address {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.description {
  font-size: 14px;
  color: #888;
  margin-bottom: 15px;
  line-height: 1.5;
}

.price {
  font-size: 24px;
  color: #e74c3c;
  font-weight: bold;
}

.currency {
  font-size: 16px;
}

.amount {
  font-size: 28px;
}

.unit {
  font-size: 14px;
  font-weight: normal;
  color: #888;
}

.empty-state {
  text-align: center;
  padding: 50px 0;
  color: #999;
}
</style>
