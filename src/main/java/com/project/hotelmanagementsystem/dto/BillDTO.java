package com.project.hotelmanagementsystem.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BillDTO {
    private Integer id;
    private Integer checkInId;
    private String billStatus;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal depositAmount;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
    private String guestName;
    private String roomNumber;
    private BigDecimal roomCharge;
    private BigDecimal additionalCharges;
    private BigDecimal refundAmount;
    private BigDecimal additionalPaymentAmount;
    private boolean hasDamageItem;

    public boolean isHasDamageItem() {
        return hasDamageItem;
    }

    public void setHasDamageItem(boolean hasDamageItem) {
        this.hasDamageItem = hasDamageItem;
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

    public String getBillStatus() {
        return billStatus;
    }

    public void setBillStatus(String billStatus) {
        this.billStatus = billStatus;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public BigDecimal getRoomCharge() {
        return roomCharge;
    }

    public void setRoomCharge(BigDecimal roomCharge) {
        this.roomCharge = roomCharge;
    }

    public BigDecimal getAdditionalCharges() {
        return additionalCharges;
    }

    public void setAdditionalCharges(BigDecimal additionalCharges) {
        this.additionalCharges = additionalCharges;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public BigDecimal getAdditionalPaymentAmount() {
        return additionalPaymentAmount;
    }

    public void setAdditionalPaymentAmount(BigDecimal additionalPaymentAmount) {
        this.additionalPaymentAmount = additionalPaymentAmount;
    }
}
