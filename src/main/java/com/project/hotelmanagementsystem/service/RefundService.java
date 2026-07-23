package com.project.hotelmanagementsystem.service;

import com.project.hotelmanagementsystem.entity.Refund;

import java.util.List;
import java.util.Optional;

/**
 * 退款记录Service接口
 */
public interface RefundService {

    /**
     * 根据ID查询退款记录
     *
     * @param id 退款记录ID
     * @return 退款记录信息
     */
    Optional<Refund> findById(Integer id);

    /**
     * 查询所有退款记录
     *
     * @return 退款记录列表
     */
    List<Refund> findAll();

    /**
     * 保存/更新退款记录
     *
     * @param refund 退款记录信息
     * @return 保存后的退款记录信息
     */
    Refund save(Refund refund);

    /**
     * 根据ID删除退款记录
     *
     * @param id 退款记录ID
     */
    void deleteById(Integer id);

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