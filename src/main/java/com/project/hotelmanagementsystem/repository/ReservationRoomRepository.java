package com.project.hotelmanagementsystem.repository;

import com.project.hotelmanagementsystem.entity.ReservationRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * 预订房间明细Repository接口
 */
public interface ReservationRoomRepository extends JpaRepository<ReservationRoom, Integer> {

    /**
     * 根据预订ID查询预订房间明细列表
     *
     * @param reservationId 预订ID
     * @return 预订房间明细列表
     */
    List<ReservationRoom> findByReservationId(Integer reservationId);

    /**
     * 根据房间ID查询预订房间明细列表
     *
     * @param roomId 房间ID
     * @return 预订房间明细列表
     */
    List<ReservationRoom> findByRoomId(Integer roomId);

    /**
     * 根据房型ID查询预订房间明细列表
     *
     * @param roomTypeId 房型ID
     * @return 预订房间明细列表
     */
    List<ReservationRoom> findByRoomTypeId(Integer roomTypeId);

    /**
     * 查询已被确认/入住预订占用的房间ID列表
     * 这些房间虽然状态可能仍是 vacant，但业务上已被锁定
     */
    @Query("SELECT DISTINCT rr.roomId FROM ReservationRoom rr WHERE rr.roomId IS NOT NULL " +
            "AND rr.reservationId IN (SELECT r.id FROM Reservation r WHERE r.status IN ('confirmed', 'checked_in'))")
    List<Integer> findLockedRoomIds();
}
