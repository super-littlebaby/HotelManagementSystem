package com.project.hotelmanagementsystem.repository;

import com.project.hotelmanagementsystem.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}
