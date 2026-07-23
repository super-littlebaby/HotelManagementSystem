package com.project.hotelmanagementsystem.repository;

import com.project.hotelmanagementsystem.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 退款记录Repository接口
 */
@Repository
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
}
