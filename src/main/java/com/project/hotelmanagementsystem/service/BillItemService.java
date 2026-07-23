package com.project.hotelmanagementsystem.service;

import com.project.hotelmanagementsystem.entity.BillItem;

import java.util.List;
import java.util.Optional;

/**
 * 账单明细Service接口
 */
public interface BillItemService {

    /**
     * 根据ID查询账单明细
     *
     * @param id 账单明细ID
     * @return 账单明细信息
     */
    Optional<BillItem> findById(Integer id);

    /**
     * 查询所有账单明细
     *
     * @return 账单明细列表
     */
    List<BillItem> findAll();

    /**
     * 保存/更新账单明细
     *
     * @param billItem 账单明细信息
     * @return 保存后的账单明细信息
     */
    BillItem save(BillItem billItem);

    /**
     * 根据ID删除账单明细
     *
     * @param id 账单明细ID
     */
    void deleteById(Integer id);

    /**
     * 根据账单ID查询账单明细列表
     *
     * @param billId 账单ID
     * @return 账单明细列表
     */
    List<BillItem> findByBillId(Integer billId);

    /**
     * 根据项目类型查询账单明细列表
     *
     * @param itemType 项目类型
     * @return 账单明细列表
     */
    List<BillItem> findByItemType(String itemType);

    /**
     * 根据账单ID和项目类型查询账单明细列表
     *
     * @param billId   账单ID
     * @param itemType 项目类型
     * @return 账单明细列表
     */
    List<BillItem> findByBillIdAndItemType(Integer billId, String itemType);
}