package com.project.hotelmanagementsystem.dto.reservation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * 创建预订请求DTO
 */
public class CreateReservationRequest {

    /**
     * 客人ID（可选，登录用户自动填充；线下/电话预订无账号时为空）
     */
    private Integer guestId;

    /**
     * 客人姓名（线下/电话预订无账号时必填）
     */
    private String guestName;

    /**
     * 证件类型（线下客人）
     */
    private String idType;

    /**
     * 证件号码（线下客人，明文传入由后端加密）
     */
    private String idNumber;

    /**
     * 联系电话（线下客人）
     */
    private String phone;

    /**
     * 入住日期
     */
    @NotNull(message = "入住日期不能为空")
    private LocalDate checkInDate;

    /**
     * 退房日期
     */
    @NotNull(message = "退房日期不能为空")
    private LocalDate checkOutDate;

    /**
     * 特殊要求
     */
    private String specialRequests;

    /**
     * 预订渠道：online/phone/walk_in/ota
     */
    @NotBlank(message = "预订渠道不能为空")
    private String channel;

    /**
     * 房间列表
     */
    @NotEmpty(message = "至少需要选择一间房")
    @Valid
    private List<ReservationRoomRequest> rooms;

    public Integer getGuestId() {
        return guestId;
    }

    public void setGuestId(Integer guestId) {
        this.guestId = guestId;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public List<ReservationRoomRequest> getRooms() {
        return rooms;
    }

    public void setRooms(List<ReservationRoomRequest> rooms) {
        this.rooms = rooms;
    }
}
