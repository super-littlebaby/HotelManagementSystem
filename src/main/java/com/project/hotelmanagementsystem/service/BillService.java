package com.project.hotelmanagementsystem.service;

import com.project.hotelmanagementsystem.entity.Bill;

import java.util.List;
import java.util.Optional;

/**
 * 账单Service接口
 */
public interface BillService {

    /**
     * 根据ID查询账单
     *
     * @param id 账单ID
     * @return 账单信息
     */
    Optional<Bill> findById(Integer id);

    /**
     * 查询所有账单
     *
     * @return 账单列表
     */
    List<Bill> findAll();

    /**
     * 保存/更新账单
     *
     * @param bill 账单信息
     * @return 保存后的账单信息
     */
    Bill save(Bill bill);

    /**
     * 根据ID删除账单
     *
     * @param id 账单ID
     */
    void deleteById(Integer id);

    /**
     * 根据入住ID查询账单列表
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
}