package com.project.hotelmanagementsystem.repository;

import com.project.hotelmanagementsystem.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 收款记录Repository接口
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    /**
     * 根据账单ID查询收款记录列表
     *
     * @param billId 账单ID
     * @return 收款记录列表
     */
    List<Payment> findByBillId(Integer billId);

    /**
     * 根据支付方式查询收款记录列表
     *
     * @param paymentMethod 支付方式
     * @return 收款记录列表
     */
    List<Payment> findByPaymentMethod(String paymentMethod);

    /**
     * 根据支付类型查询收款记录列表
     *
     * @param paymentType 支付类型
     * @return 收款记录列表
     */
    List<Payment> findByPaymentType(String paymentType);

    /**
     * 根据账单ID和支付类型查询收款记录列表
     *
     * @param billId      账单ID
     * @param paymentType 支付类型
     * @return 收款记录列表
     */
    List<Payment> findByBillIdAndPaymentType(Integer billId, String paymentType);

    /**
     * 根据酒店ID查询收款记录列表（通过bill → checkIn → room关联）
     *
     * @param hotelId 酒店ID
     * @return 收款记录列表
     */
    @Query("SELECT p FROM Payment p WHERE p.billId IN (SELECT b.id FROM Bill b WHERE b.checkInId IN (SELECT c.id FROM CheckIn c WHERE c.roomId IN (SELECT r.id FROM Room r WHERE r.hotelId = :hotelId)))")
    List<Payment> findAllByHotelId(@Param("hotelId") Integer hotelId);

    /**
     * 根据收款记录ID查询关联的酒店ID
     *
     * @param paymentId 收款记录ID
     * @return 酒店ID
     */
    @Query("SELECT r.hotelId FROM Room r WHERE r.id = (SELECT c.roomId FROM CheckIn c WHERE c.id = (SELECT b.checkInId FROM Bill b WHERE b.id = (SELECT p.billId FROM Payment p WHERE p.id = :paymentId)))")
    Optional<Integer> findHotelIdByPaymentId(@Param("paymentId") Integer paymentId);

    /**
     * 根据账单ID和酒店ID查询收款记录列表
     *
     * @param billId 账单ID
     * @param hotelId 酒店ID
     * @return 收款记录列表
     */
    @Query("SELECT p FROM Payment p WHERE p.billId = :billId AND p.billId IN (SELECT b.id FROM Bill b WHERE b.checkInId IN (SELECT c.id FROM CheckIn c WHERE c.roomId IN (SELECT r.id FROM Room r WHERE r.hotelId = :hotelId)))")
    List<Payment> findByBillIdAndHotelId(@Param("billId") Integer billId, @Param("hotelId") Integer hotelId);

    /**
     * 根据支付方式和酒店ID查询收款记录列表
     *
     * @param paymentMethod 支付方式
     * @param hotelId 酒店ID
     * @return 收款记录列表
     */
    @Query("SELECT p FROM Payment p WHERE p.paymentMethod = :paymentMethod AND p.billId IN (SELECT b.id FROM Bill b WHERE b.checkInId IN (SELECT c.id FROM CheckIn c WHERE c.roomId IN (SELECT r.id FROM Room r WHERE r.hotelId = :hotelId)))")
    List<Payment> findByPaymentMethodAndHotelId(@Param("paymentMethod") String paymentMethod, @Param("hotelId") Integer hotelId);

    /**
     * 根据支付类型和酒店ID查询收款记录列表
     *
     * @param paymentType 支付类型
     * @param hotelId 酒店ID
     * @return 收款记录列表
     */
    @Query("SELECT p FROM Payment p WHERE p.paymentType = :paymentType AND p.billId IN (SELECT b.id FROM Bill b WHERE b.checkInId IN (SELECT c.id FROM CheckIn c WHERE c.roomId IN (SELECT r.id FROM Room r WHERE r.hotelId = :hotelId)))")
    List<Payment> findByPaymentTypeAndHotelId(@Param("paymentType") String paymentType, @Param("hotelId") Integer hotelId);

    /**
     * 根据账单ID、支付类型和酒店ID查询收款记录列表
     *
     * @param billId      账单ID
     * @param paymentType 支付类型
     * @param hotelId     酒店ID
     * @return 收款记录列表
     */
    @Query("SELECT p FROM Payment p WHERE p.billId = :billId AND p.paymentType = :paymentType AND p.billId IN (SELECT b.id FROM Bill b WHERE b.checkInId IN (SELECT c.id FROM CheckIn c WHERE c.roomId IN (SELECT r.id FROM Room r WHERE r.hotelId = :hotelId)))")
    List<Payment> findByBillIdAndPaymentTypeAndHotelId(@Param("billId") Integer billId, @Param("paymentType") String paymentType, @Param("hotelId") Integer hotelId);
}
