package com.project.hotelmanagementsystem.entity;

import jakarta.persistence.*;

/**
 * 同住客人实体类
 * <p>
 * 记录入住登记中的所有在住人员信息，包括主登记人和同住人员。
 * </p>
 */
@Entity
@Table(name = "stay_guests")
public class StayGuest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "check_in_id", nullable = false)
    private Integer checkInId;

    @Column(name = "guest_id")
    private Integer guestId;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "id_type", length = 20)
    private String idType;

    @Column(name = "id_number", length = 200)
    private String idNumber;

    @Column(name = "is_primary")
    private Boolean isPrimary = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "check_in_id", insertable = false, updatable = false)
    private CheckIn checkIn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id", insertable = false, updatable = false)
    private Guest guest;

    public StayGuest() {
    }

    public StayGuest(Integer checkInId, Integer guestId, Boolean isPrimary) {
        this.checkInId = checkInId;
        this.guestId = guestId;
        this.isPrimary = isPrimary;
    }

    public StayGuest(Integer checkInId, Integer guestId, String name, String idType, String idNumber, Boolean isPrimary) {
        this.checkInId = checkInId;
        this.guestId = guestId;
        this.name = name;
        this.idType = idType;
        this.idNumber = idNumber;
        this.isPrimary = isPrimary;
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Boolean getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    public CheckIn getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(CheckIn checkIn) {
        this.checkIn = checkIn;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }
}