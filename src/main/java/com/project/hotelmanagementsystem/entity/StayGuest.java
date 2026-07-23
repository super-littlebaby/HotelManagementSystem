package com.project.hotelmanagementsystem.entity;

import jakarta.persistence.*;

/**
 * 同住客人实体类
 * 对应数据库表：stay_guests
 */
@Entity
@Table(name = "stay_guests")
public class StayGuest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "check_in_id", nullable = false)
    private Integer checkInId;

    @Column(name = "guest_id", nullable = false)
    private Integer guestId;

    @Column(name = "is_primary")
    private Boolean isPrimary;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCheckInId() {
        return checkInId;
    }

    public void setCheckInId(Integer checkInId) {
        this.checkInId = checkInId;
    }

    public Integer getGuestId() {
        return guestId;
    }

    public void setGuestId(Integer guestId) {
        this.guestId = guestId;
    }

    public Boolean getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }
}