<template>
  <div class="page reservation-page">
    <div class="page-header">
      <h2>预订管理</h2>
      <el-button type="primary" @click="openAddDialog">添加预订</el-button>
    </div>

    <!-- 搜索和筛选 -->
    <el-form :inline="true" :model="searchForm" class="search-form">
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
          <el-option label="未到场" value="no_show" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
        <el-button @click="loadReservations">刷新</el-button>
      </el-form-item>
    </el-form>

    <!-- 预订列表 -->
    <el-table :data="reservations" border stripe v-loading="loading">
      <el-table-column prop="id" label="预订ID" width="80" />
      <el-table-column prop="hotelName" label="酒店" show-overflow-tooltip />
      <el-table-column prop="guestName" label="客人" />
      <el-table-column label="房型">
        <template #default="{ row }">
          <span v-if="row.rooms && row.rooms.length > 0">
            <template v-for="(item, idx) in getRoomTypeSummary(row.rooms)" :key="idx">
              <span>{{ item.count }}间{{ item.name }}</span><template v-if="idx < getRoomTypeSummary(row.rooms).length - 1">、<br/></template>
            </template>
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="入住日期">
        <template #default="{ row }">{{ formatDate(row.checkInDate) }}</template>
      </el-table-column>
      <el-table-column label="退房日期">
        <template #default="{ row }">{{ formatDate(row.checkOutDate) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="金额" align="right">
        <template #default="{ row }">¥{{ Number(row.totalAmount).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="渠道">
        <template #default="{ row }">{{ getChannelLabel(row.channel) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row)">详情</el-button>
          <template v-if="row.status !== 'no_show'">
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
              type="danger"
              @click="handleCancel(row)"
              v-if="row.status === 'pending' || row.status === 'confirmed'"
            >
              取消
            </el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

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
        <template v-if="currentReservation && currentReservation.status !== 'no_show'">
          <el-button
            type="primary"
            @click="handleConfirm(currentReservation)"
            v-if="currentReservation.status === 'pending'"
          >
            确认预订
          </el-button>
          <el-button
            type="warning"
            @click="handleAssignRoom(currentReservation)"
            v-if="currentReservation.status === 'confirmed' && hasUnassignedRoom(currentReservation)"
          >
            分配房间
          </el-button>
          <el-button
            type="success"
            @click="handleCheckIn(currentReservation)"
            v-if="currentReservation.status === 'confirmed'"
          >
            办理入住
          </el-button>
          <el-button
            type="danger"
            @click="handleCancel(currentReservation)"
            v-if="currentReservation.status === 'pending' || currentReservation.status === 'confirmed'"
          >
            取消预订
          </el-button>
        </template>
      </template>
    </el-dialog>

    <!-- 分配房间对话框 -->
    <el-dialog v-model="assignRoomDialogVisible" title="分配房间" width="600px">
      <el-form label-width="100px">
        <el-form-item label="选择房型">
          <el-select v-model="selectedRoomTypeId" placeholder="请选择房型" style="width: 100%" @change="handleRoomTypeChange">
            <el-option
              v-for="rt in availableRoomTypes"
              :key="rt.id"
              :label="rt.name + ' (¥' + Number(rt.basePrice).toFixed(2) + '/晚)'"
              :value="rt.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="选择房间">
          <el-select v-model="selectedRoomId" placeholder="请选择房间" style="width: 100%">
            <el-option
              v-for="room in availableRooms"
              :key="room.id"
              :label="`${room.roomNumber}`"
              :value="room.id"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <!-- 房型信息和费用计算 -->
      <el-divider v-if="selectedRoomTypeInfo" />
      <div v-if="selectedRoomTypeInfo" class="price-info">
        <h4 style="margin: 0 0 10px; color: #409eff">房型信息</h4>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="房型名称">{{ selectedRoomTypeInfo.name }}</el-descriptions-item>
          <el-descriptions-item label="基础房价">¥{{ Number(selectedRoomTypeInfo.basePrice).toFixed(2) }}/晚</el-descriptions-item>
          <el-descriptions-item label="可住成人">{{ selectedRoomTypeInfo.maxAdults }} 人</el-descriptions-item>
          <el-descriptions-item label="可住儿童">{{ selectedRoomTypeInfo.maxChildren }} 人</el-descriptions-item>
        </el-descriptions>
        <div class="total-calc">
          <el-descriptions :column="3" border size="small">
            <el-descriptions-item label="入住天数">{{ stayNights }} 天</el-descriptions-item>
            <el-descriptions-item label="房费小计">¥{{ Number(selectedRoomTypeInfo.basePrice * stayNights).toFixed(2) }}</el-descriptions-item>
            <el-descriptions-item label="应付总金额" content-class="total-label">
              <span style="color: #f56c6c; font-weight: bold; font-size: 16px">¥{{ Number(selectedRoomTypeInfo.basePrice * stayNights).toFixed(2) }}</span>
            </el-descriptions-item>
          </el-descriptions>
          <div class="price-change-hint" v-if="originalRoomType && originalRoomType.id !== selectedRoomTypeId">
            <el-alert type="warning" :closable="false" show-icon>
              <template #title>
                已更换房型！原房型：{{ originalRoomType.name }} (¥{{ Number(originalRoomType.basePrice).toFixed(2) }}/晚) → 新房型：{{ selectedRoomTypeInfo.name }} (¥{{ Number(selectedRoomTypeInfo.basePrice).toFixed(2) }}/晚)
              </template>
            </el-alert>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="assignRoomDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAssignRoom">确认分配</el-button>
      </template>
    </el-dialog>

    <!-- 办理入住（按房间录入实际入住人信息）对话框 -->
    <el-dialog v-model="checkInDialogVisible" title="办理入住 - 实际入住人信息" width="900px" top="5vh">
      <div v-if="currentReservation" class="check-in-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="预订ID">{{ currentReservation.id }}</el-descriptions-item>
          <el-descriptions-item label="预订人">{{ currentReservation.guestName }}</el-descriptions-item>
          <el-descriptions-item label="入住日期">{{ currentReservation.checkInDate }}</el-descriptions-item>
          <el-descriptions-item label="退房日期">{{ currentReservation.checkOutDate }}</el-descriptions-item>
        </el-descriptions>

        <el-alert
          title="请为每个房间填写实际入住人信息（可能是帮别人预约，所以不要直接使用预订账号信息）"
          type="warning"
          :closable="false"
          show-icon
          style="margin: 15px 0"
        />

        <div v-for="(roomCheckIn, rIndex) in roomCheckInForms" :key="rIndex" class="room-checkin-block">
          <div class="room-checkin-header">
            房间 {{ rIndex + 1 }}：
            <span class="room-info">{{ roomCheckIn.roomNumber || '未分配' }} - {{ roomCheckIn.roomTypeName || '' }}</span>
            <span class="room-capacity">(成人 {{ roomCheckIn.adults }} / 儿童 {{ roomCheckIn.children }}，共 {{ roomCheckIn.totalGuests }} 人)</span>
            <el-checkbox
              v-if="currentReservation && currentReservation.guestName"
              v-model="roomCheckIn.selfCheckIn"
              @change="toggleSelfCheckIn(rIndex, roomCheckIn.selfCheckIn)"
              style="margin-left: 15px"
            >本人入住（预订人自动作为主登记人）</el-checkbox>
          </div>

          <el-divider content-position="left">主登记人信息</el-divider>
          <el-form label-width="120px">
            <el-form-item label="姓名" required>
              <el-input v-model="roomCheckIn.primaryGuestName" placeholder="请输入实际入住的主登记人姓名" />
            </el-form-item>
            <el-form-item label="证件类型" required>
              <el-select v-model="roomCheckIn.primaryIdType" placeholder="请选择证件类型">
                <el-option label="身份证" value="id_card" />
                <el-option label="护照" value="passport" />
                <el-option label="驾驶证" value="drivers_license" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
            <el-form-item label="证件号" required>
              <el-input v-model="roomCheckIn.primaryIdNumber" placeholder="请输入主登记人证件号码" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="roomCheckIn.primaryPhone" placeholder="请输入联系电话" />
            </el-form-item>
            <el-form-item label="押金支付方式" required>
              <el-select v-model="roomCheckIn.depositPaymentMethod" placeholder="请选择押金支付方式" style="width: 100%">
                <el-option label="现金" value="cash" />
                <el-option label="信用卡" value="credit_card" />
                <el-option label="借记卡" value="debit_card" />
                <el-option label="微信支付" value="wechat" />
                <el-option label="支付宝" value="alipay" />
                <el-option label="银行转账" value="bank_transfer" />
              </el-select>
            </el-form-item>
          </el-form>

          <el-divider content-position="left">
            同住客人信息（共需 {{ Math.max(0, roomCheckIn.totalGuests - 1) }} 人，已添加 {{ roomCheckIn.stayGuests.length }} 人）
          </el-divider>
          <div v-if="roomCheckIn.totalGuests <= 1" style="color: #999; padding: 5px 0 10px;">
            单人入住，无需填写同住客人信息
          </div>
          <div v-for="(guest, gIndex) in roomCheckIn.stayGuests" :key="gIndex" class="stay-guest-item">
            <div class="stay-guest-header">同住客人 {{ gIndex + 1 }}</div>
            <el-form label-width="120px">
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
                <el-button type="danger" size="small" @click="removeStayGuest(rIndex, gIndex)">删除</el-button>
              </el-form-item>
            </el-form>
          </div>
          <el-button
            v-if="roomCheckIn.stayGuests.length < roomCheckIn.totalGuests - 1"
            type="primary"
            plain
            size="small"
            @click="addStayGuest(rIndex)"
            style="margin-top: 5px"
          >
            + 添加同住客人
          </el-button>
        </div>
      </div>
      <template #footer>
        <el-button @click="checkInDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCheckIn">确认办理入住</el-button>
      </template>
    </el-dialog>

    <!-- 添加预订对话框（线下预订场景） -->
    <el-dialog v-model="addDialogVisible" title="添加预订（线下预订）" width="900px" top="5vh" :close-on-click-modal="false">
      <el-alert
        title="线下预订流程：先按手机号查找客人，找到则直接选用；未找到时在下方表单填写客人信息并保存。然后选择酒店、房型、日期、人数后提交预订。"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 15px"
      />

      <!-- 第一步：客人查找/录入 -->
      <el-divider content-position="left">第一步：选择客人</el-divider>

      <!-- 场景一：客人有账户 -->
      <el-alert
        title="客人有账户？输入手机号或邮箱查找并选择"
        type="success"
        :closable="false"
        show-icon
        style="margin-bottom: 10px"
      />
      <el-form :inline="true" style="margin-bottom: 10px">
        <el-form-item label="查询">
          <el-select v-model="guestSearchType" style="width: 100px; margin-right: 10px">
            <el-option label="手机号" value="phone" />
            <el-option label="邮箱" value="email" />
          </el-select>
          <el-input
            v-model="guestSearchKeyword"
            :placeholder="guestSearchType === 'phone' ? '请输入客人手机号' : '请输入客人邮箱'"
            style="width: 200px"
            clearable
            @keyup.enter="searchGuest"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchGuest">查找</el-button>
        </el-form-item>
      </el-form>

      <!-- 查找到的客人选择列表 -->
      <div v-if="guestSearchResults.length > 0" style="margin-bottom: 15px">
        <el-table :data="guestSearchResults" border size="small" max-height="200" highlight-current-row @current-change="handleSelectGuest">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column label="姓名">
            <template #default="{ row }">
              {{ (row.firstName || '') + (row.lastName || '') }}
            </template>
          </el-table-column>
          <el-table-column prop="phone" label="手机号" width="140" />
          <el-table-column prop="email" label="邮箱" width="180" />
          <el-table-column label="操作" width="100" align="center">
            <template #default="{ row }">
              <el-button v-if="selectedGuest && selectedGuest.id === row.id" type="success" size="small" disabled>已选择</el-button>
              <el-button v-else type="primary" size="small" @click="selectGuest(row)">选择</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div v-else-if="guestSearched && guestSearchResults.length === 0 && !selectedGuest" style="margin-bottom: 15px">
        <el-alert type="warning" :closable="false" show-icon title="未找到该手机号对应的客人，请在下方「客人无账户」区域填写信息" />
      </div>

      <!-- 当前已选客人 -->
      <div v-if="selectedGuest" class="selected-guest-info" style="margin-bottom: 15px">
        <el-tag type="success" size="large">
          已选客人：{{ (selectedGuest.firstName || '') + (selectedGuest.lastName || '') }}（{{ selectedGuest.phone }}）
        </el-tag>
        <el-button type="text" @click="clearSelectedGuest">取消选择</el-button>
      </div>

      <!-- 场景二：客人无账户 -->
      <el-alert
        v-if="!selectedGuest"
        title="客人无账户？直接填写下方信息提交预订"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 10px"
      />
      <el-form v-if="!selectedGuest" :model="newGuestForm" label-width="80px" size="small" style="margin-top: 5px">
        <el-form-item label="姓名" required>
          <el-input v-model="offlineGuestName" placeholder="如：张三" style="width: 200px" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="newGuestForm.phone" placeholder="可选，便于联系" style="width: 200px" />
        </el-form-item>
        <el-form-item label="证件类型">
          <el-select v-model="newGuestForm.idType" style="width: 140px">
            <el-option label="身份证" value="id_card" />
            <el-option label="护照" value="passport" />
            <el-option label="驾驶证" value="drivers_license" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="证件号">
          <el-input v-model="newGuestForm.idNumber" placeholder="可选，便于办理入住" style="width: 240px" />
        </el-form-item>
      </el-form>

      <!-- 第二步：预订信息 -->
      <el-divider content-position="left">第二步：填写预订信息</el-divider>

      <el-form :model="addReservationForm" label-width="80px">
        <el-form-item label="酒店" required>
          <el-select v-model="addReservationForm.hotelId" placeholder="请选择酒店" style="width: 100%" @change="handleAddHotelChange">
            <el-option v-for="h in hotels" :key="h.id" :label="h.name" :value="h.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="入住日期" required>
          <el-date-picker
            v-model="addReservationForm.checkInDate"
            type="date"
            placeholder="选择入住日期"
            value-format="YYYY-MM-DD"
            :disabled-date="disablePast"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="退房日期" required>
          <el-date-picker
            v-model="addReservationForm.checkOutDate"
            type="date"
            placeholder="选择退房日期"
            value-format="YYYY-MM-DD"
            :disabled-date="disableBeforeCheckIn"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item v-if="addNights > 0" label="入住天数">
          <el-tag>共 {{ addNights }} 晚</el-tag>
        </el-form-item>

        <el-form-item label="预订渠道" required>
          <el-radio-group v-model="addReservationForm.channel">
            <el-radio label="walk_in">到店预订</el-radio>
            <el-radio label="phone">电话预订</el-radio>
          </el-radio-group>
        </el-form-item>

        <div v-for="(room, idx) in addReservationForm.rooms" :key="idx" class="add-room-block">
          <div class="add-room-header">
            <span>房间 {{ idx + 1 }}</span>
            <el-button v-if="addReservationForm.rooms.length > 1" type="danger" size="small" link @click="removeAddRoom(idx)">删除</el-button>
          </div>
          <el-form-item label="房型" required>
            <el-select v-model="room.roomTypeId" placeholder="请选择房型" style="width: 100%" @change="handleAddRoomTypeChange(idx)">
              <el-option
                v-for="rt in addAvailableRoomTypes"
                :key="rt.id"
                :label="`${rt.name} - ¥${Number(rt.basePrice).toFixed(2)}/晚 (成人${rt.maxAdults||0}/儿童${rt.maxChildren||0})`"
                :value="rt.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="成人数" required>
            <el-input-number v-model="room.adults" :min="1" :max="getRoomTypeMaxAdults(room.roomTypeId)" />
          </el-form-item>
          <el-form-item label="儿童数">
            <el-input-number v-model="room.children" :min="0" :max="getRoomTypeMaxChildren(room.roomTypeId)" />
          </el-form-item>
        </div>

        <el-form-item>
          <el-button type="primary" plain @click="addAddRoom" :disabled="!addReservationForm.hotelId || addReservationForm.rooms.length >= 5">+ 添加房间</el-button>
        </el-form-item>

        <el-form-item label="特殊要求">
          <el-input v-model="addReservationForm.specialRequests" type="textarea" :rows="2" placeholder="可选" />
        </el-form-item>

        <el-form-item v-if="addTotalAmount > 0" label="预估总价">
          <span style="color: #f56c6c; font-weight: bold; font-size: 16px">¥{{ addTotalAmount.toFixed(2) }}</span>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddReservation" :loading="submittingAdd">提交预订</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDate } from '../utils/date'
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
  searchByGuestName,
  createReservation
} from '../api/reservation'
import { getAvailableRoomsByTypeAndStatus } from '../api/room'
import { getRoomTypes } from '../api/roomType'
import { getHotels } from '../api/hotel'
import { findGuestsByPhone, findGuestByEmail } from '../api/guest'
import { state as authState } from '../stores/auth'
import { validateIdNumber } from '../utils/validate'

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
const roomCheckInForms = ref([])
const originalRoomType = ref(null)

// ===== 添加预订（线下预订）相关状态 =====
const addDialogVisible = ref(false)
const submittingAdd = ref(false)
const hotels = ref([])

// 客人查找
const guestSearchType = ref('phone')
const guestSearchKeyword = ref('')
const guestSearchResults = ref([])
const guestSearched = ref(false)
const selectedGuest = ref(null)

// 新建客人表单（用于保存客人档案）
const newGuestForm = reactive({
  firstName: '',
  lastName: '',
  phone: '',
  email: '',
  idType: 'id_card',
  idNumber: ''
})

// 线下客人姓名（无账号，直接提交预订）
const offlineGuestName = ref('')

// 预订表单
const addReservationForm = reactive({
  hotelId: null,
  checkInDate: '',
  checkOutDate: '',
  channel: 'walk_in',
  specialRequests: '',
  rooms: [{ roomTypeId: null, adults: 1, children: 0 }]
})

const addAvailableRoomTypes = computed(() => {
  if (!addReservationForm.hotelId) return []
  return roomTypes.value.filter(rt => rt.hotelId === addReservationForm.hotelId)
})

const addNights = computed(() => {
  if (!addReservationForm.checkInDate || !addReservationForm.checkOutDate) return 0
  const d1 = new Date(addReservationForm.checkInDate)
  const d2 = new Date(addReservationForm.checkOutDate)
  const diff = Math.round((d2 - d1) / (1000 * 60 * 60 * 24))
  return diff > 0 ? diff : 0
})

const addTotalAmount = computed(() => {
  if (addNights.value <= 0) return 0
  let total = 0
  for (const room of addReservationForm.rooms) {
    if (!room.roomTypeId) continue
    const rt = roomTypes.value.find(t => t.id === room.roomTypeId)
    if (rt) {
      total += Number(rt.basePrice) * addNights.value
    }
  }
  return total
})

// 日期禁用
const disablePast = (date) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date < today
}

const disableBeforeCheckIn = (date) => {
  if (!addReservationForm.checkInDate) return disablePast(date)
  const checkIn = new Date(addReservationForm.checkInDate)
  return date <= checkIn
}

// 房型人数上限
const getRoomTypeMaxAdults = (roomTypeId) => {
  const rt = roomTypes.value.find(t => t.id === roomTypeId)
  return rt?.maxAdults || 10
}

const getRoomTypeMaxChildren = (roomTypeId) => {
  const rt = roomTypes.value.find(t => t.id === roomTypeId)
  return rt?.maxChildren || 10
}

// 打开添加预订对话框
const openAddDialog = async () => {
  // 重置表单
  guestSearchType.value = 'phone'
  guestSearchKeyword.value = ''
  guestSearchResults.value = []
  guestSearched.value = false
  selectedGuest.value = null

  newGuestForm.firstName = ''
  newGuestForm.lastName = ''
  newGuestForm.phone = ''
  newGuestForm.email = ''
  newGuestForm.idType = 'id_card'
  newGuestForm.idNumber = ''
  offlineGuestName.value = ''

  addReservationForm.hotelId = null
  addReservationForm.checkInDate = ''
  addReservationForm.checkOutDate = ''
  addReservationForm.channel = 'walk_in'
  addReservationForm.specialRequests = ''
  addReservationForm.rooms = [{ roomTypeId: null, adults: 1, children: 0 }]

  // 默认填员工所属酒店
  const staffHotelId = authState.staff?.hotelId
  if (staffHotelId) {
    addReservationForm.hotelId = staffHotelId
  }

  try {
    const [hotelsRes, roomTypesRes] = await Promise.all([
      getHotels(),
      getRoomTypes()
    ])
    if (hotelsRes.code === 200) {
      // 集团管理员可看全部酒店，普通员工只能选自己酒店
      if (staffHotelId) {
        hotels.value = (hotelsRes.data || []).filter(h => h.id === staffHotelId)
      } else {
        hotels.value = hotelsRes.data || []
      }
    }
    if (roomTypesRes.code === 200) {
      roomTypes.value = roomTypesRes.data
    }
  } catch (e) {
    ElMessage.error('初始化数据失败')
  }

  addDialogVisible.value = true
}

// 按手机号或邮箱查找客人
const searchGuest = async () => {
  const keyword = guestSearchKeyword.value.trim()
  if (!keyword) {
    ElMessage.warning(`请输入${guestSearchType.value === 'phone' ? '手机号' : '邮箱'}`)
    return
  }
  try {
    let res
    if (guestSearchType.value === 'phone') {
      res = await findGuestsByPhone(keyword)
    } else {
      res = await findGuestByEmail(keyword)
    }
    if (res.code === 200) {
      // 邮箱搜索返回单个对象，转换为数组；手机号搜索返回数组
      if (guestSearchType.value === 'email') {
        guestSearchResults.value = res.data ? [res.data] : []
      } else {
        guestSearchResults.value = res.data || []
      }
      guestSearched.value = true
      selectedGuest.value = null
      if (guestSearchResults.value.length === 0 && guestSearchType.value === 'phone') {
        // 未找到，把手机号填入新建客人表单
        newGuestForm.phone = keyword
      }
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (e) {
    ElMessage.error('查询失败')
  }
}

// 选择客人
const selectGuest = (row) => {
  if (!row) return
  selectedGuest.value = row
  ElMessage.success(`已选择客人：${(row.firstName || '') + (row.lastName || '')}`)
}

const clearSelectedGuest = () => {
  selectedGuest.value = null
}

// 酒店切换
const handleAddHotelChange = () => {
  // 清空房型
  for (const room of addReservationForm.rooms) {
    room.roomTypeId = null
  }
}

const handleAddRoomTypeChange = (idx) => {
  const room = addReservationForm.rooms[idx]
  if (!room.roomTypeId) return
  // 修正人数上限
  const maxAdults = getRoomTypeMaxAdults(room.roomTypeId)
  const maxChildren = getRoomTypeMaxChildren(room.roomTypeId)
  if (room.adults > maxAdults) room.adults = maxAdults
  if (room.children > maxChildren) room.children = maxChildren
}

const addAddRoom = () => {
  if (addReservationForm.rooms.length >= 5) {
    ElMessage.warning('一次预订最多5间房')
    return
  }
  addReservationForm.rooms.push({ roomTypeId: null, adults: 1, children: 0 })
}

const removeAddRoom = (idx) => {
  if (addReservationForm.rooms.length <= 1) return
  addReservationForm.rooms.splice(idx, 1)
}

// 提交预订
const submitAddReservation = async () => {
  // 校验客人：必须有账号客人 或 线下客人姓名
  const hasAccountGuest = selectedGuest.value && selectedGuest.value.id
  const offlineName = (offlineGuestName.value || '').trim()

  if (!hasAccountGuest && !offlineName) {
    ElMessage.warning('请先查找并选择客人，或在上方填写客人姓名')
    return
  }

  // 验证线下客人的证件号码
  if (!hasAccountGuest && newGuestForm.idNumber) {
    const idValidation = validateIdNumber(newGuestForm.idType, newGuestForm.idNumber)
    if (!idValidation.valid) {
      ElMessage.warning(idValidation.message)
      return
    }
  }

  if (!addReservationForm.hotelId) {
    ElMessage.warning('请选择酒店')
    return
  }
  if (!addReservationForm.checkInDate || !addReservationForm.checkOutDate) {
    ElMessage.warning('请选择入住和退房日期')
    return
  }
  if (addNights.value <= 0) {
    ElMessage.warning('退房日期必须晚于入住日期')
    return
  }
  for (let i = 0; i < addReservationForm.rooms.length; i++) {
    const r = addReservationForm.rooms[i]
    if (!r.roomTypeId) {
      ElMessage.warning(`房间 ${i + 1}：请选择房型`)
      return
    }
    if (!r.adults || r.adults < 1) {
      ElMessage.warning(`房间 ${i + 1}：成人数至少为1`)
      return
    }
  }

  submittingAdd.value = true
  try {
    // 构建请求：有账号客人走 guestId，否则走线下客人字段
    const payload = {
      checkInDate: addReservationForm.checkInDate,
      checkOutDate: addReservationForm.checkOutDate,
      specialRequests: addReservationForm.specialRequests || null,
      channel: addReservationForm.channel,
      rooms: addReservationForm.rooms.map(r => ({
        roomTypeId: r.roomTypeId,
        adults: r.adults,
        children: r.children
      }))
    }
    if (hasAccountGuest) {
      payload.guestId = selectedGuest.value.id
    } else {
      payload.guestName = offlineName
      payload.idType = newGuestForm.idType
      payload.idNumber = newGuestForm.idNumber || null
      payload.phone = newGuestForm.phone || null
    }
    const res = await createReservation(payload)
    if (res.code === 200) {
      ElMessage.success(`预订成功！预订ID: ${res.data?.id || ''}`)
      addDialogVisible.value = false
      loadReservations()
    } else {
      ElMessage.error(res.message || '预订失败')
    }
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || '预订失败'
    ElMessage.error(msg)
  } finally {
    submittingAdd.value = false
  }
}

const availableRoomTypes = computed(() => {
  const hotelId = authState.staff?.hotelId
  if (hotelId) {
    return roomTypes.value.filter(rt => rt.hotelId === hotelId || rt.hotelId == null)
  }
  return roomTypes.value
})

const selectedRoomTypeInfo = computed(() => {
  if (!selectedRoomTypeId.value) return null
  return roomTypes.value.find(rt => rt.id === selectedRoomTypeId.value)
})

const stayNights = computed(() => {
  if (!currentReservation.value) return 1
  const checkIn = currentReservation.value.checkInDate
  const checkOut = currentReservation.value.checkOutDate
  if (!checkIn || !checkOut) return 1
  const d1 = new Date(checkIn)
  const d2 = new Date(checkOut)
  const diff = Math.round((d2 - d1) / (1000 * 60 * 60 * 24))
  return Math.max(diff, 1)
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
    no_show: 'danger'
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
    no_show: '未到场'
  }
  return labels[status] || status
}

const getChannelLabel = (channel) => {
  const labels = {
    online: '在线',
    phone: '电话',
    walk_in: '到店'
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

const getRoomTypeSummary = (rooms) => {
  if (!rooms || rooms.length === 0) return []
  const map = {}
  for (const room of rooms) {
    const name = room.roomTypeName || '未知房型'
    if (!map[name]) {
      map[name] = { name, count: 0 }
    }
    map[name].count++
  }
  return Object.values(map)
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
    doConfirm(row.id, null)
  } catch (error) {
    ElMessage.error('确认失败')
  }
}

const handleRoomTypeChange = async (roomTypeId) => {
  if (!roomTypeId) {
    availableRooms.value = []
    selectedRoomId.value = null
    return
  }

  try {
    const res = await getAvailableRoomsByTypeAndStatus(roomTypeId, 'vacant')
    if (res.code === 200) {
      let rooms = res.data
      // 排除已在本预订中分配的房间
      if (currentReservation.value?.rooms) {
        const assignedRoomNumbers = currentReservation.value.rooms
          .filter(r => r.roomNumber)
          .map(r => r.roomNumber)
        rooms = rooms.filter(r => !assignedRoomNumbers.includes(r.roomNumber))
      }
      availableRooms.value = rooms
      selectedRoomId.value = null
      if (availableRooms.value.length === 0) {
        ElMessage.warning('该房型当前没有可分配的空闲房间')
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

    // 找到预订中第一个未分配的房间，用它的房型作为默认选中与"原始房型"对比基准
    const unassignedRoom = (row.rooms || []).find(r => !r.roomNumber || r.roomNumber === '未分配')
    const roomTypeId = unassignedRoom?.roomTypeId ?? row.rooms?.[0]?.roomTypeId
    selectedRoomTypeId.value = roomTypeId

    if (roomTypeId) {
      await handleRoomTypeChange(roomTypeId)
    }

    // 保存原始房型信息用于对比：只有未分配房间的房型才算"原始房型"
    originalRoomType.value = roomTypes.value.find(rt => rt.id === roomTypeId)

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
  
  if (!selectedRoomId.value) {
    ElMessage.warning('请选择房间')
    return
  }

  const roomTypeChanged = originalRoomType.value && originalRoomType.value.id !== selectedRoomTypeId.value

  assignRoomDialogVisible.value = false

  if (currentReservation.value.status === 'pending') {
    doConfirm(currentReservation.value.id, selectedRoomId.value)
  } else if (currentReservation.value.status === 'confirmed') {
    try {
      const res = await assignRoom(currentReservation.value.id, selectedRoomId.value, selectedRoomTypeId.value)
      if (res.code === 200) {
        let msg = '分配房间成功'
        if (roomTypeChanged) {
          msg += `（已更换房型为 ${selectedRoomTypeInfo.value?.name}）`
        }
        ElMessage.success(msg)
        loadReservations()
        if (detailDialogVisible.value) {
          currentReservation.value = res.data
        }
      } else {
        ElMessage.error(res.message || '分配房间失败')
      }
    } catch (error) {
    const msg = error?.response?.data?.message || error?.message || '分配房间失败'
    ElMessage.error(msg)
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
    const res = await getReservationById(row.id)
    if (res.code === 200) {
      currentReservation.value = res.data
      // 按房间初始化入住人信息表单
      const rooms = res.data.rooms || []
      roomCheckInForms.value = rooms.map(r => {
        const adults = r.adults || 1
        const children = r.children || 0
        return {
          reservationRoomId: r.id,
          roomNumber: r.roomNumber || '未分配',
          roomTypeName: r.roomTypeName || '',
          adults,
          children,
          totalGuests: adults + children,
          primaryGuestName: '',
          primaryIdType: 'id_card',
          primaryIdNumber: '',
          primaryPhone: '',
          depositPaymentMethod: 'cash',
          selfCheckIn: false,
          stayGuests: []
        }
      })
      checkInDialogVisible.value = true
    } else {
      ElMessage.error(res.message || '获取预订详情失败')
    }
  } catch (error) {
    ElMessage.error('获取预订详情失败')
  }
}

const toggleSelfCheckIn = (roomIndex, checked) => {
  const room = roomCheckInForms.value[roomIndex]
  if (!room) return
  const reservation = currentReservation.value
  if (!reservation) return

  if (checked) {
    room.primaryGuestName = reservation.guestName || ''
    room.primaryIdType = reservation.guestIdType || 'id_card'
    room.primaryIdNumber = reservation.guestIdNumber || ''
    room.primaryPhone = reservation.guestPhone || ''
  } else {
    room.primaryGuestName = ''
    room.primaryIdType = 'id_card'
    room.primaryIdNumber = ''
    room.primaryPhone = ''
  }
}

const addStayGuest = (roomIndex) => {
  const room = roomCheckInForms.value[roomIndex]
  if (!room) return
  if (room.stayGuests.length >= room.totalGuests - 1) {
    ElMessage.warning(`同住客人最多 ${Math.max(0, room.totalGuests - 1)} 人`)
    return
  }
  room.stayGuests.push({
    name: '',
    idType: 'id_card',
    idNumber: ''
  })
}

const removeStayGuest = (roomIndex, guestIndex) => {
  roomCheckInForms.value[roomIndex].stayGuests.splice(guestIndex, 1)
}

const confirmCheckIn = async () => {
  if (!currentReservation.value) return

  // 校验每个房间的入住人信息
  for (let r = 0; r < roomCheckInForms.value.length; r++) {
    const rc = roomCheckInForms.value[r]
    if (!rc.primaryGuestName || !rc.primaryGuestName.trim()) {
      ElMessage.warning(`房间 ${r + 1}：请填写主登记人姓名`)
      return
    }
    if (!rc.primaryIdType) {
      ElMessage.warning(`房间 ${r + 1}：请选择主登记人证件类型`)
      return
    }
    if (!rc.primaryIdNumber || !rc.primaryIdNumber.trim()) {
      ElMessage.warning(`房间 ${r + 1}：请填写主登记人证件号`)
      return
    }
    if (!rc.depositPaymentMethod || !String(rc.depositPaymentMethod).trim()) {
      ElMessage.warning(`房间 ${r + 1}：请选择押金支付方式`)
      return
    }
    // 验证主登记人证件号码
    const primaryIdValidation = validateIdNumber(rc.primaryIdType, rc.primaryIdNumber)
    if (!primaryIdValidation.valid) {
      ElMessage.warning(`房间 ${r + 1}：${primaryIdValidation.message}`)
      return
    }
    for (let g = 0; g < rc.stayGuests.length; g++) {
      const guest = rc.stayGuests[g]
      if (!guest.name || !guest.name.trim()) {
        ElMessage.warning(`房间 ${r + 1}：请填写同住客人 ${g + 1} 的姓名`)
        return
      }
      if (!guest.idNumber || !guest.idNumber.trim()) {
        ElMessage.warning(`房间 ${r + 1}：请填写同住客人 ${g + 1} 的证件号`)
        return
      }
      // 验证同住客人证件号码
      const guestIdType = guest.idType || 'id_card'
      const guestIdValidation = validateIdNumber(guestIdType, guest.idNumber)
      if (!guestIdValidation.valid) {
        ElMessage.warning(`房间 ${r + 1} 同住客人 ${g + 1}：${guestIdValidation.message}`)
        return
      }
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

    // 按房间分组提交入住人信息
    const submitData = {
      rooms: roomCheckInForms.value.map(rc => ({
        reservationRoomId: rc.reservationRoomId,
        primaryGuestName: rc.primaryGuestName,
        primaryIdType: rc.primaryIdType,
        primaryIdNumber: rc.primaryIdNumber,
        primaryPhone: rc.primaryPhone,
        depositPaymentMethod: rc.depositPaymentMethod,
        stayGuests: rc.stayGuests.map(g => ({
          name: g.name,
          idType: g.idType || 'id_card',
          idNumber: g.idNumber,
          isPrimary: false
        }))
      }))
    }

    console.log('[DIAG-CHECKIN] 预约入住提交数据：', JSON.stringify(submitData, null, 2))

    const res = await checkInReservation(currentReservation.value.id, submitData)
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
      const errorMsg = error?.message || error?.response?.data?.message || '办理入住失败'
      ElMessage.error(errorMsg)
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


