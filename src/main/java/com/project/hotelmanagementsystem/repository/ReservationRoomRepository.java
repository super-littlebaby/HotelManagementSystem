package com.project.hotelmanagementsystem.repository;

import com.project.hotelmanagementsystem.entity.ReservationRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 预订房间明细Repository接口
 */
@Repository
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
}
