package com.project.hotelmanagementsystem.repository;

import com.project.hotelmanagementsystem.entity.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 入住登记Repository接口
 */
@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Integer> {

    /**
     * 根据客人ID查询入住记录列表
     *
     * @param guestId 客人ID
     * @return 入住记录列表
     */
    List<CheckIn> findByGuestId(Integer guestId);

    /**
     * 根据房间ID查询入住记录列表
     *
     * @param roomId 房间ID
     * @return 入住记录列表
     */
    List<CheckIn> findByRoomId(Integer roomId);

    /**
     * 根据状态查询入住记录列表
     *
     * @param status 入住状态
     * @return 入住记录列表
     */
    List<CheckIn> findByStatus(String status);

    /**
     * 根据预订ID查询入住记录
     *
     * @param reservationId 预订ID
     * @return 入住记录列表
     */
    List<CheckIn> findByReservationId(Integer reservationId);

    /**
     * 根据房间ID和状态查询入住记录
     *
     * @param roomId 房间ID
     * @param status 入住状态
     * @return 入住记录列表
     */
    List<CheckIn> findByRoomIdAndStatus(Integer roomId, String status);
}
