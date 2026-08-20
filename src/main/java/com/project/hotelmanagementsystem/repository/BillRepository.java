package com.project.hotelmanagementsystem.repository;

import com.project.hotelmanagementsystem.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 账单Repository接口
 */
@Repository
public interface BillRepository extends JpaRepository<Bill, Integer> {

    /**
     * 根据入住ID查询账单
     *
     * @param checkInId 入住ID
     * @return 账单列表
     */
    List<Bill> findByCheckInId(Integer checkInId);

    /**
     * 根据账单状态查询账单列表
     *
     * @param billStatus 账单状态
     * @return 账单列表
     */
    List<Bill> findByBillStatus(String billStatus);

    /**
     * 根据入住ID查询最新账单
     *
     * @param checkInId 入住ID
     * @return 账单信息
     */
    Optional<Bill> findFirstByCheckInIdOrderByIdDesc(Integer checkInId);

    /**
     * 根据酒店ID查询账单列表（通过checkIn → room关联）
     *
     * @param hotelId 酒店ID
     * @return 账单列表
     */
    @Query("SELECT b FROM Bill b WHERE b.checkInId IN (SELECT c.id FROM CheckIn c WHERE c.roomId IN (SELECT r.id FROM Room r WHERE r.hotelId = :hotelId))")
    List<Bill> findAllByHotelId(@Param("hotelId") Integer hotelId);

    /**
     * 根据账单ID查询关联的酒店ID
     *
     * @param billId 账单ID
     * @return 酒店ID
     */
    @Query("SELECT r.hotelId FROM Room r WHERE r.id = (SELECT c.roomId FROM CheckIn c WHERE c.id = (SELECT b.checkInId FROM Bill b WHERE b.id = :billId))")
    Optional<Integer> findHotelIdByBillId(@Param("billId") Integer billId);

    /**
     * 根据入住ID和酒店ID查询账单列表
     *
     * @param checkInId 入住ID
     * @param hotelId 酒店ID
     * @return 账单列表
     */
    @Query("SELECT b FROM Bill b WHERE b.checkInId = :checkInId AND b.checkInId IN (SELECT c.id FROM CheckIn c WHERE c.roomId IN (SELECT r.id FROM Room r WHERE r.hotelId = :hotelId))")
    List<Bill> findByCheckInIdAndHotelId(@Param("checkInId") Integer checkInId, @Param("hotelId") Integer hotelId);

    /**
     * 根据账单状态和酒店ID查询账单列表
     *
     * @param billStatus 账单状态
     * @param hotelId 酒店ID
     * @return 账单列表
     */
    @Query("SELECT b FROM Bill b WHERE b.billStatus = :billStatus AND b.checkInId IN (SELECT c.id FROM CheckIn c WHERE c.roomId IN (SELECT r.id FROM Room r WHERE r.hotelId = :hotelId))")
    List<Bill> findByBillStatusAndHotelId(@Param("billStatus") String billStatus, @Param("hotelId") Integer hotelId);

    /**
     * 根据入住ID和酒店ID查询最新账单
     *
     * @param checkInId 入住ID
     * @param hotelId 酒店ID
     * @return 账单信息
     */
    @Query("SELECT b FROM Bill b WHERE b.checkInId = :checkInId AND b.checkInId IN (SELECT c.id FROM CheckIn c WHERE c.roomId IN (SELECT r.id FROM Room r WHERE r.hotelId = :hotelId)) ORDER BY b.id DESC")
    List<Bill> findByCheckInIdAndHotelIdOrderByIdDesc(@Param("checkInId") Integer checkInId, @Param("hotelId") Integer hotelId);

    /**
     * 根据入住ID和账单状态查询账单列表
     *
     * @param checkInId 入住ID
     * @param billStatus 账单状态
     * @return 账单列表
     */
    List<Bill> findByCheckInIdAndBillStatus(Integer checkInId, String billStatus);
}
