package com.project.hotelmanagementsystem.service;

import com.project.hotelmanagementsystem.entity.ReservationRoom;

import java.util.List;
import java.util.Optional;

/**
 * 预订房间明细Service接口
 */
public interface ReservationRoomService {

    /**
     * 根据ID查询预订房间明细
     *
     * @param id 预订房间明细ID
     * @return 预订房间明细信息
     */
    Optional<ReservationRoom> findById(Integer id);

    /**
     * 查询所有预订房间明细
     *
     * @return 预订房间明细列表
     */
    List<ReservationRoom> findAll();

    /**
     * 保存/更新预订房间明细
     *
     * @param reservationRoom 预订房间明细信息
     * @return 保存后的预订房间明细信息
     */
    ReservationRoom save(ReservationRoom reservationRoom);

    /**
     * 根据ID删除预订房间明细
     *
     * @param id 预订房间明细ID
     */
    void deleteById(Integer id);

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