package com.project.hotelmanagementsystem.dto.reservation;

import com.project.hotelmanagementsystem.dto.checkin.CreateCheckInRequest;

import java.util.List;

/**
 * 办理有预约入住请求DTO
 * <p>
 * 用于员工为已确认的预订办理入住时，携带同住客人信息。
 * </p>
 */
public class CheckInReservationRequest {

    /**
     * 同住客人信息列表（不含主登记人，主登记人信息从客人档案自动填充）
     */
    private List<CreateCheckInRequest.StayGuestRequest> stayGuests;

    public List<CreateCheckInRequest.StayGuestRequest> getStayGuests() {
        return stayGuests;
    }

    public void setStayGuests(List<CreateCheckInRequest.StayGuestRequest> stayGuests) {
        this.stayGuests = stayGuests;
    }
}
