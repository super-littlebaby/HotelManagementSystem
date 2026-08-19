package com.project.hotelmanagementsystem.dto.reservation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 预订房间请求DTO
 */
public class ReservationRoomRequest {

    @NotNull(message = "房型ID不能为空")
    private Integer roomTypeId;

    @Min(value = 1, message = "成人数量至少为1")
    private Integer adults;

    @Min(value = 0, message = "儿童数量不能为负数")
    private Integer children;

    public Integer getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(Integer roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public Integer getAdults() {
        return adults;
    }

    public void setAdults(Integer adults) {
        this.adults = adults;
    }

    public Integer getChildren() {
        return children;
    }

    public void setChildren(Integer children) {
        this.children = children;
    }
}
