package com.project.hotelmanagementsystem.repository;

import com.project.hotelmanagementsystem.entity.StayGuest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 同住客人Repository接口
 */
@Repository
public interface StayGuestRepository extends JpaRepository<StayGuest, Integer> {

    /**
     * 根据入住ID查询同住客人列表
     *
     * @param checkInId 入住ID
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
     * 根据入住ID和是否主客查询
     *
     * @param checkInId 入住ID
     * @param isPrimary 是否主客
     * @return 同住客人列表
     */
    List<StayGuest> findByCheckInIdAndIsPrimary(Integer checkInId, Boolean isPrimary);
}
