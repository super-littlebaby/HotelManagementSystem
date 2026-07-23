package com.project.hotelmanagementsystem.service;

import com.project.hotelmanagementsystem.entity.Payment;

import java.util.List;
import java.util.Optional;

/**
 * 收款记录Service接口
 */
public interface PaymentService {

    /**
     * 根据ID查询收款记录
     *
     * @param id 收款记录ID
     * @return 收款记录信息
     */
    Optional<Payment> findById(Integer id);

    /**
     * 查询所有收款记录
     *
     * @return 收款记录列表
     */
    List<Payment> findAll();

    /**
     * 保存/更新收款记录
     *
     * @param payment 收款记录信息
     * @return 保存后的收款记录信息
     */
    Payment save(Payment payment);

    /**
     * 根据ID删除收款记录
     *
     * @param id 收款记录ID
     */
    void deleteById(Integer id);

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