package com.project.hotelmanagementsystem.service;

import com.project.hotelmanagementsystem.entity.Facility;
import com.project.hotelmanagementsystem.entity.RoomTypeFacility;
import com.project.hotelmanagementsystem.entity.RoomTypeFacilityId;

import java.util.List;
import java.util.Optional;

/**
 * 房型-设施关联Service接口
 */
public interface RoomTypeFacilityService {

    /**
     * 根据复合主键查询关联信息
     *
     * @param id 复合主键
     * @return 房型-设施关联信息
     */
    Optional<RoomTypeFacility> findById(RoomTypeFacilityId id);

    /**
     * 查询所有关联信息
     *
     * @return 房型-设施关联列表
     */
    List<RoomTypeFacility> findAll();

    /**
     * 保存/更新关联信息
     *
     * @param roomTypeFacility 房型-设施关联信息
     * @return 保存后的关联信息
     */
    RoomTypeFacility save(RoomTypeFacility roomTypeFacility);

    /**
     * 根据复合主键删除关联信息
     *
     * @param id 复合主键
     */
    void deleteById(RoomTypeFacilityId id);

    /**
     * 根据房型ID查询关联的设施列表
     *
     * @param roomTypeId 房型ID
     * @return 房型-设施关联列表
     */
    List<RoomTypeFacility> findByRoomTypeId(Integer roomTypeId);

    /**
     * 根据设施ID查询关联的房型列表
     *
     * @param facilityId 设施ID
     * @return 房型-设施关联列表
     */
    List<RoomTypeFacility> findByFacilityId(Integer facilityId);

    /**
     * 根据房型ID查询设施详情列表
     *
     * @param roomTypeId 房型ID
     * @return 设施详情列表
     */
    List<Facility> findFacilitiesByRoomTypeId(Integer roomTypeId);

    /**
     * 批量添加设施到房型
     *
     * @param roomTypeId  房型ID
     * @param facilityIds 设施ID列表
     */
    void addFacilitiesToRoomType(Integer roomTypeId, List<Integer> facilityIds);

    /**
     * 批量移除房型的设施
     *
     * @param roomTypeId  房型ID
     * @param facilityIds 设施ID列表
     */
    void removeFacilitiesFromRoomType(Integer roomTypeId, List<Integer> facilityIds);

    /**
     * 替换房型的所有设施
     *
     * @param roomTypeId  房型ID
     * @param facilityIds 新的设施ID列表
     */
    void replaceFacilitiesForRoomType(Integer roomTypeId, List<Integer> facilityIds);

    /**
     * 删除房型的所有关联设施
     *
     * @param roomTypeId 房型ID
     */
    void deleteByRoomTypeId(Integer roomTypeId);
}
