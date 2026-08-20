<template>
  <el-form-item :label="label" :required="required">
    <el-autocomplete
      v-if="isAdmin"
      v-model="inputName"
      :fetch-suggestions="querySearch"
      :placeholder="placeholder"
      @select="selectHotel"
      clearable
      style="width: 100%"
    />
    <el-input
      v-else-if="hasHotel"
      v-model="displayName"
      :disabled="true"
      placeholder="加载中..."
    />
    <el-input
      v-else
      v-model="displayName"
      :disabled="true"
      placeholder="暂无关联酒店"
    />
  </el-form-item>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { getHotels } from '../api/hotel'
import { state as authState } from '../stores/auth'

const props = defineProps({
  modelValue: {
    type: [Number, null],
    default: null
  },
  label: {
    type: String,
    default: '所属酒店'
  },
  required: {
    type: Boolean,
    default: true
  },
  placeholder: {
    type: String,
    default: '输入酒店名称搜索'
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const inputName = ref('')
const hotels = ref([])
const displayName = ref('')

const currentRole = computed(() => authState.staff?.role || '')
const currentHotelId = computed(() => authState.staff?.hotelId)

const isAdmin = computed(() => currentRole.value === 'admin')
const hasHotel = computed(() => currentHotelId.value !== null && currentHotelId.value !== undefined)

const initHotelList = async () => {
  try {
    const res = await getHotels()
    if (res.code === 200) {
      hotels.value = res.data || []
    }
  } catch (error) {
    console.error('加载酒店列表失败', error)
  }
}

const querySearch = (queryString, cb) => {
  const keyword = (queryString || '').trim().toLowerCase()
  const matched = hotels.value.filter(h =>
    !keyword || h.name.toLowerCase().includes(keyword)
  )
  cb(matched.map(h => ({ value: h.name, hotel: h })))
}

const selectHotel = (item) => {
  if (item && item.hotel) {
    emit('update:modelValue', item.hotel.id)
    emit('change', item.hotel)
  }
}

watch(currentHotelId, async (newHotelId) => {
  if (!isAdmin.value && newHotelId !== null && newHotelId !== undefined) {
    if (hotels.value.length === 0) {
      await initHotelList()
    }
    const hotel = hotels.value.find(h => h.id === Number(newHotelId))
    displayName.value = hotel ? hotel.name : ''
    emit('update:modelValue', Number(newHotelId))
  }
}, { immediate: true })

watch(() => props.modelValue, async (newVal) => {
  if (isAdmin.value) {
    if (newVal !== null && newVal !== undefined) {
      if (hotels.value.length === 0) {
        await initHotelList()
      }
      const hotel = hotels.value.find(h => h.id === newVal)
      if (hotel) {
        inputName.value = hotel.name
      }
    }
  }
}, { immediate: true })

onMounted(async () => {
  await initHotelList()
})
</script>


