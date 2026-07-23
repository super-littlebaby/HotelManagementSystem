package com.project.hotelmanagementsystem.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * 房型-设施关联复合主键类
 */
public class RoomTypeFacilityId implements Serializable {

    private Integer roomTypeId;
    private Integer facilityId;

    public RoomTypeFacilityId() {
    }

    public RoomTypeFacilityId(Integer roomTypeId, Integer facilityId) {
        this.roomTypeId = roomTypeId;
        this.facilityId = facilityId;
    }

    public Integer getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(Integer roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public Integer getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(Integer facilityId) {
        this.facilityId = facilityId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoomTypeFacilityId that = (RoomTypeFacilityId) o;
        return Objects.equals(roomTypeId, that.roomTypeId) && Objects.equals(facilityId, that.facilityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomTypeId, facilityId);
    }
}