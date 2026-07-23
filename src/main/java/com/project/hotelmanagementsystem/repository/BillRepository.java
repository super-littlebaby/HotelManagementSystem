package com.project.hotelmanagementsystem.repository;

import com.project.hotelmanagementsystem.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
