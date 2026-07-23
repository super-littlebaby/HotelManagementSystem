package com.project.hotelmanagementsystem.repository;

import com.project.hotelmanagementsystem.entity.BillItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 账单明细Repository接口
 */
@Repository
public interface BillItemRepository extends JpaRepository<BillItem, Integer> {

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
