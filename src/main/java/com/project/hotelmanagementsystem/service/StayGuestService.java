package com.project.hotelmanagementsystem.service;

import com.project.hotelmanagementsystem.entity.StayGuest;

import java.util.List;
import java.util.Optional;

/**
 * 同住客人Service接口
 */
public interface StayGuestService {

    /**
     * 根据ID查询同住客人
     *
     * @param id 同住客人ID
     * @return 同住客人信息
     */
    Optional<StayGuest> findById(Integer id);

    /**
     * 查询所有同住客人
     *
     * @return 同住客人列表
     */
    List<StayGuest> findAll();

    /**
     * 保存/更新同住客人
     *
     * @param stayGuest 同住客人信息
     * @return 保存后的同住客人信息
     */
    StayGuest save(StayGuest stayGuest);

    /**
     * 根据ID删除同住客人
     *
     * @param id 同住客人ID
     */
    void deleteById(Integer id);

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