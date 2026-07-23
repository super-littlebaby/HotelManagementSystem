package com.project.hotelmanagementsystem.service;

import com.project.hotelmanagementsystem.entity.Reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 预订Service接口
 */
public interface ReservationService {

    /**
     * 根据ID查询预订
     *
     * @param id 预订ID
     * @return 预订信息
     */
    Optional<Reservation> findById(Integer id);

    /**
     * 查询所有预订
     *
     * @return 预订列表
     */
    List<Reservation> findAll();

    /**
     * 保存/更新预订
     *
     * @param reservation 预订信息
     * @return 保存后的预订信息
     */
    Reservation save(Reservation reservation);

    /**
     * 根据ID删除预订
     *
     * @param id 预订ID
     */
    void deleteById(Integer id);

    /**
     * 根据客人ID查询预订列表
     *
     * @param guestId 客人ID
     * @return 预订列表
     */
    List<Reservation> findByGuestId(Integer guestId);

    /**
     * 根据状态查询预订列表
     *
     * @param status 预订状态
     * @return 预订列表
     */
    List<Reservation> findByStatus(String status);

    /**
     * 根据员工ID查询预订列表
     *
     * @param employeeId 员工ID
     * @return 预订列表
     */
    List<Reservation> findByEmployeeId(Integer employeeId);

    /**
     * 根据客人ID和状态查询预订列表
     *
     * @param guestId 客人ID
     * @param status  预订状态
     * @return 预订列表
     */
    List<Reservation> findByGuestIdAndStatus(Integer guestId, String status);

    /**
     * 根据入住日期范围查询预订列表
     *
     * @param checkInDate  入住日期
     * @param checkOutDate 退房日期
     * @return 预订列表
     */
    List<Reservation> findByCheckInDateBetween(LocalDate checkInDate, LocalDate checkOutDate);
}