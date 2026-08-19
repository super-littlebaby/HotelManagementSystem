package com.project.hotelmanagementsystem.service;

import com.project.hotelmanagementsystem.entity.StayGuest;

import java.util.List;
import java.util.Optional;

/**
 * 同住客人服务接口
 * <p>
 * 提供同住客人管理相关的业务逻辑。
 * </p>
 */
public interface StayGuestService {

    /**
     * 根据ID查询同住客人记录
     *
     * @param id 同住客人记录ID
     * @return 同住客人记录（可能为空）
     */
    Optional<StayGuest> findById(Integer id);

    /**
     * 查询所有同住客人记录
     *
     * @return 同住客人记录列表
     */
    List<StayGuest> findAll();

    /**
     * 保存同住客人记录
     *
     * @param stayGuest 同住客人记录
     * @return 保存后的记录
     */
    StayGuest save(StayGuest stayGuest);

    /**
     * 根据ID删除同住客人记录
     *
     * @param id 同住客人记录ID
     */
    void deleteById(Integer id);

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
     * 批量保存同住客人记录
     *
     * @param stayGuests 同住客人记录列表
     * @return 保存后的记录列表
     */
    List<StayGuest> saveAll(List<StayGuest> stayGuests);

    /**
     * 根据入住记录ID删除所有同住客人记录
     *
     * @param checkInId 入住记录ID
     */
    void deleteByCheckInId(Integer checkInId);
}