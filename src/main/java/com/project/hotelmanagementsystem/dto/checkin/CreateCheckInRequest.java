package com.project.hotelmanagementsystem.dto.checkin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.hotelmanagementsystem.entity.CheckIn;
import com.project.hotelmanagementsystem.entity.StayGuest;

import java.time.LocalDateTime;
import java.util.List;

public class CreateCheckInRequest {

    private Integer reservationId;
    private Integer guestId;
    private String guestName;
    private String idType;
    private String idNumber;
    private String phone;
    private Integer roomId;
    private Integer adults = 1;
    private Integer children = 0;
    private LocalDateTime checkInTime;
    private LocalDateTime expectedCheckOutTime;
    private String status = "in_house";
    private Double ratePerNight;
    private Double totalCharge;
    private String notes;
    @JsonProperty("depositPaymentMethod")
    private String depositPaymentMethod;
    private List<StayGuestRequest> stayGuests;

    public Integer getReservationId() {
        return reservationId;
    }

    public void setReservationId(Integer reservationId) {
        this.reservationId = reservationId;
    }

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

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
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

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalDateTime getExpectedCheckOutTime() {
        return expectedCheckOutTime;
    }

    public void setExpectedCheckOutTime(LocalDateTime expectedCheckOutTime) {
        this.expectedCheckOutTime = expectedCheckOutTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getRatePerNight() {
        return ratePerNight;
    }

    public void setRatePerNight(Double ratePerNight) {
        this.ratePerNight = ratePerNight;
    }

    public Double getTotalCharge() {
        return totalCharge;
    }

    public void setTotalCharge(Double totalCharge) {
        this.totalCharge = totalCharge;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDepositPaymentMethod() {
        return depositPaymentMethod;
    }

    public void setDepositPaymentMethod(String depositPaymentMethod) {
        this.depositPaymentMethod = depositPaymentMethod;
    }

    public List<StayGuestRequest> getStayGuests() {
        return stayGuests;
    }

    public void setStayGuests(List<StayGuestRequest> stayGuests) {
        this.stayGuests = stayGuests;
    }

    public CheckIn toCheckIn() {
        CheckIn checkIn = new CheckIn();
        checkIn.setReservationId(this.reservationId);
        checkIn.setGuestId(this.guestId);
        checkIn.setGuestName(this.guestName);
        checkIn.setIdType(this.idType);
        checkIn.setIdNumber(this.idNumber);
        checkIn.setPhone(this.phone);
        checkIn.setRoomId(this.roomId);
        checkIn.setAdults(this.adults);
        checkIn.setChildren(this.children);
        checkIn.setCheckInTime(this.checkInTime != null ? this.checkInTime : LocalDateTime.now());
        checkIn.setExpectedCheckOutTime(this.expectedCheckOutTime);
        checkIn.setStatus(this.status);
        checkIn.setRatePerNight(this.ratePerNight != null ? java.math.BigDecimal.valueOf(this.ratePerNight) : null);
        checkIn.setTotalCharge(this.totalCharge != null ? java.math.BigDecimal.valueOf(this.totalCharge) : null);
        checkIn.setNotes(this.notes);
        return checkIn;
    }

    public List<StayGuest> toStayGuests(Integer checkInId) {
        if (this.stayGuests == null || this.stayGuests.isEmpty()) {
            return List.of();
        }
        return this.stayGuests.stream()
                .map(req -> new StayGuest(checkInId, req.getGuestId(), req.getName(), req.getIdType(), req.getIdNumber(), req.getIsPrimary()))
                .toList();
    }

    public static class StayGuestRequest {
        private Integer guestId;
        private String name;
        private String idType;
        private String idNumber;
        private Boolean isPrimary;

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
    }
}