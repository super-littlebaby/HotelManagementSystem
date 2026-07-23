package com.project.hotelmanagementsystem.service;

import com.project.hotelmanagementsystem.entity.CheckIn;

import java.util.List;
import java.util.Optional;

/**
 * 入住登记Service接口
 */
public interface CheckInService {

    /**
     * 根据ID查询入住记录
     *
     * @param id 入住记录ID
     * @return 入住记录信息
     */
    Optional<CheckIn> findById(Integer id);

    /**
     * 查询所有入住记录
     *
     * @return 入住记录列表
     */
    List<CheckIn> findAll();

    /**
     * 保存/更新入住记录
     *
     * @param checkIn 入住记录信息
     * @return 保存后的入住记录信息
     */
    CheckIn save(CheckIn checkIn);

    /**
     * 根据ID删除入住记录
     *
     * @param id 入住记录ID
     */
    void deleteById(Integer id);

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
     * 根据预订ID查询入住记录列表
     *
     * @param reservationId 预订ID
     * @return 入住记录列表
     */
    List<CheckIn> findByReservationId(Integer reservationId);

    /**
     * 根据房间ID和状态查询入住记录列表
     *
     * @param roomId 房间ID
     * @param status 入住状态
     * @return 入住记录列表
     */
    List<CheckIn> findByRoomIdAndStatus(Integer roomId, String status);
}