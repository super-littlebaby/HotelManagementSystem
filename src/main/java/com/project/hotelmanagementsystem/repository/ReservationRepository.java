package com.project.hotelmanagementsystem.repository;

import com.project.hotelmanagementsystem.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 预订Repository接口
 */
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

    /**
     * 根据客人手机号查询预订列表
     *
     * @param phone 客人手机号
     * @return 预订列表
     */
    @Query("SELECT r FROM Reservation r JOIN Guest g ON r.guestId = g.id WHERE g.phone = :phone")
    List<Reservation> findByGuestPhone(@Param("phone") String phone);

    /**
     * 根据客人邮箱查询预订列表
     *
     * @param email 客人邮箱
     * @return 预订列表
     */
    @Query("SELECT r FROM Reservation r JOIN Guest g ON r.guestId = g.id WHERE g.email = :email")
    List<Reservation> findByGuestEmail(@Param("email") String email);

    /**
     * 根据客人姓名查询预订列表（模糊匹配姓或名）
     *
     * @param name 客人姓名
     * @return 预订列表
     */
    @Query("SELECT r FROM Reservation r JOIN Guest g ON r.guestId = g.id WHERE g.firstName LIKE %:name% OR g.lastName LIKE %:name% OR CONCAT(g.firstName, g.lastName) LIKE %:name%")
    List<Reservation> findByGuestName(@Param("name") String name);

    /**
     * 根据酒店ID查询预订列表
     *
     * @param hotelId 酒店ID
     * @return 预订列表
     */
    List<Reservation> findByHotelId(Integer hotelId);

    /**
     * 查询所有需要自动更新为"未到场"的预订
     * 条件：状态不是 checked_in 或 cancelled，且入住日期早于当前日期
     *
     * @param today 当前日期
     * @return 需要更新的预订列表
     */
    @Query("SELECT r FROM Reservation r WHERE r.status NOT IN ('checked_in', 'cancelled', 'no_show') AND r.checkInDate < :today")
    List<Reservation> findExpiredReservations(@Param("today") LocalDate today);

    /**
     * 批量更新过期预订状态为"未到场"
     *
     * @param today 当前日期
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE Reservation r SET r.status = 'no_show' WHERE r.status NOT IN ('checked_in', 'cancelled', 'no_show') AND r.checkInDate < :today")
    int updateExpiredReservationsToNoShow(@Param("today") LocalDate today);
}
