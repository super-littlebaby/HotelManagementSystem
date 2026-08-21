package com.project.hotelmanagementsystem.dto.reservation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.hotelmanagementsystem.dto.checkin.CreateCheckInRequest;

import java.util.List;

/**
 * 办理有预约入住请求DTO
 * <p>
 * 用于员工为已确认的预订办理入住时，按房间携带实际入住人信息。
 * 支持一条预约对应多个房间，每个房间分别填写实际入住人。
 * </p>
 */
public class CheckInReservationRequest {

    /**
     * 按房间分组的入住人信息列表
     */
    private List<RoomCheckInRequest> rooms;

    public List<RoomCheckInRequest> getRooms() {
        return rooms;
    }

    public void setRooms(List<RoomCheckInRequest> rooms) {
        this.rooms = rooms;
    }

    /**
     * 单个房间的入住人信息
     */
    public static class RoomCheckInRequest {

        /**
         * 预订房间明细ID（reservation_rooms.id）
         */
        private Integer reservationRoomId;

        /**
         * 主登记人姓名
         */
        private String primaryGuestName;

        /**
         * 主登记人证件类型
         */
        private String primaryIdType;

        /**
         * 主登记人证件号码
         */
        private String primaryIdNumber;

        /**
         * 主登记人联系电话
         */
        private String primaryPhone;

        /**
         * 押金支付方式（cash/credit_card/debit_card/wechat/alipay/bank_transfer）
         */
        @JsonProperty("depositPaymentMethod")
        private String depositPaymentMethod;

        /**
         * 同住客人信息列表（不含主登记人）
         */
        private List<CreateCheckInRequest.StayGuestRequest> stayGuests;

        public Integer getReservationRoomId() {
            return reservationRoomId;
        }

        public void setReservationRoomId(Integer reservationRoomId) {
            this.reservationRoomId = reservationRoomId;
        }

        public String getPrimaryGuestName() {
            return primaryGuestName;
        }

        public void setPrimaryGuestName(String primaryGuestName) {
            this.primaryGuestName = primaryGuestName;
        }

        public String getPrimaryIdType() {
            return primaryIdType;
        }

        public void setPrimaryIdType(String primaryIdType) {
            this.primaryIdType = primaryIdType;
        }

        public String getPrimaryIdNumber() {
            return primaryIdNumber;
        }

        public void setPrimaryIdNumber(String primaryIdNumber) {
            this.primaryIdNumber = primaryIdNumber;
        }

        public String getPrimaryPhone() {
            return primaryPhone;
        }

        public void setPrimaryPhone(String primaryPhone) {
            this.primaryPhone = primaryPhone;
        }

        public String getDepositPaymentMethod() {
            return depositPaymentMethod;
        }

        public void setDepositPaymentMethod(String depositPaymentMethod) {
            this.depositPaymentMethod = depositPaymentMethod;
        }

        public List<CreateCheckInRequest.StayGuestRequest> getStayGuests() {
            return stayGuests;
        }

        public void setStayGuests(List<CreateCheckInRequest.StayGuestRequest> stayGuests) {
            this.stayGuests = stayGuests;
        }
    }
}
