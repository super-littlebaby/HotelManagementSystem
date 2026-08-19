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
     * 客人ID（可选，登录用户自动填充）
     */
    private Integer guestId;

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
