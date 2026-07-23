package com.project.hotelmanagementsystem.repository;

import com.project.hotelmanagementsystem.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 预订Repository接口
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

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
