package com.project.hotelmanagementsystem.repository;

import com.project.hotelmanagementsystem.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 退款记录Repository接口
 */
public interface RefundRepository extends JpaRepository<Refund, Integer> {

    /**
     * 根据账单ID查询退款记录列表
     *
     * @param billId 账单ID
     * @return 退款记录列表
     */
    List<Refund> findByBillId(Integer billId);

    /**
     * 根据退款方式查询退款记录列表
     *
     * @param refundMethod 退款方式
     * @return 退款记录列表
     */
    List<Refund> findByRefundMethod(String refundMethod);

    /**
     * 根据酒店ID查询退款记录列表（通过bill → checkIn → room关联）
     *
     * @param hotelId 酒店ID
     * @return 退款记录列表
     */
    @Query("SELECT rf FROM Refund rf WHERE rf.billId IN (SELECT b.id FROM Bill b WHERE b.checkInId IN (SELECT c.id FROM CheckIn c WHERE c.roomId IN (SELECT r.id FROM Room r WHERE r.hotelId = :hotelId)))")
    List<Refund> findAllByHotelId(@Param("hotelId") Integer hotelId);

    /**
     * 根据退款记录ID查询关联的酒店ID
     *
     * @param refundId 退款记录ID
     * @return 酒店ID
     */
    @Query("SELECT r.hotelId FROM Room r WHERE r.id = (SELECT c.roomId FROM CheckIn c WHERE c.id = (SELECT b.checkInId FROM Bill b WHERE b.id = (SELECT rf.billId FROM Refund rf WHERE rf.id = :refundId)))")
    Optional<Integer> findHotelIdByRefundId(@Param("refundId") Integer refundId);

    /**
     * 根据账单ID和酒店ID查询退款记录列表
     *
     * @param billId 账单ID
     * @param hotelId 酒店ID
     * @return 退款记录列表
     */
    @Query("SELECT rf FROM Refund rf WHERE rf.billId = :billId AND rf.billId IN (SELECT b.id FROM Bill b WHERE b.checkInId IN (SELECT c.id FROM CheckIn c WHERE c.roomId IN (SELECT r.id FROM Room r WHERE r.hotelId = :hotelId)))")
    List<Refund> findByBillIdAndHotelId(@Param("billId") Integer billId, @Param("hotelId") Integer hotelId);

    /**
     * 根据退款方式和酒店ID查询退款记录列表
     *
     * @param refundMethod 退款方式
     * @param hotelId 酒店ID
     * @return 退款记录列表
     */
    @Query("SELECT rf FROM Refund rf WHERE rf.refundMethod = :refundMethod AND rf.billId IN (SELECT b.id FROM Bill b WHERE b.checkInId IN (SELECT c.id FROM CheckIn c WHERE c.roomId IN (SELECT r.id FROM Room r WHERE r.hotelId = :hotelId)))")
    List<Refund> findByRefundMethodAndHotelId(@Param("refundMethod") String refundMethod, @Param("hotelId") Integer hotelId);
}
