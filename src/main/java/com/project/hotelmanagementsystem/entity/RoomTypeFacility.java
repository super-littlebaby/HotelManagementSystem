package com.project.hotelmanagementsystem.entity;

import jakarta.persistence.*;

/**
 * 房型-设施关联实体类
 * 对应数据库表：room_type_facilities
 */
@Entity
@Table(name = "room_type_facilities")
@IdClass(RoomTypeFacilityId.class)
public class RoomTypeFacility {

    @Id
    @Column(name = "room_type_id", nullable = false)
    private Integer roomTypeId;

    @Id
    @Column(name = "facility_id", nullable = false)
    private Integer facilityId;

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
}