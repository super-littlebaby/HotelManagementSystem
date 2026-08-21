package com.project.hotelmanagementsystem.repository;

import com.project.hotelmanagementsystem.entity.StayGuest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 同住客人数据访问层
 * <p>
 * 提供同住客人记录的数据库操作方法。
 * </p>
 */
public interface StayGuestRepository extends JpaRepository<StayGuest, Integer> {

    /**
     * 根据入住记录ID查询同住客人列表
     *
     * @param checkInId 入住记录ID
     * @return 同住客人列表
     */
    List<StayGuest> findByCheckInId(Integer checkInId);

    /**
     * 根据客人ID查询同住记录列表
     *
     * @param guestId 客人ID
     * @return 同住记录列表
     */
    List<StayGuest> findByGuestId(Integer guestId);

    /**
     * 根据入住记录ID和客人ID查询同住记录
     *
     * @param checkInId 入住记录ID
     * @param guestId   客人ID
     * @return 同住记录（可能为空）
     */
    java.util.Optional<StayGuest> findByCheckInIdAndGuestId(Integer checkInId, Integer guestId);

    /**
     * 根据入住记录ID删除所有同住客人记录
     *
     * @param checkInId 入住记录ID
     */
    void deleteByCheckInId(Integer checkInId);
}