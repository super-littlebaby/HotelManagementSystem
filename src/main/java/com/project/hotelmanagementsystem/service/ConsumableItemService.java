package com.project.hotelmanagementsystem.service;

import com.project.hotelmanagementsystem.entity.ConsumableItem;

import java.util.List;
import java.util.Optional;

/**
 * 可消费项目Service接口
 */
public interface ConsumableItemService {

    /**
     * 根据ID查询可消费项目
     *
     * @param id 可消费项目ID
     * @return 可消费项目信息
     */
    Optional<ConsumableItem> findById(Integer id);

    /**
     * 查询所有可消费项目
     *
     * @return 可消费项目列表
     */
    List<ConsumableItem> findAll();

    /**
     * 保存/更新可消费项目
     *
     * @param consumableItem 可消费项目信息
     * @return 保存后的可消费项目信息
     */
    ConsumableItem save(ConsumableItem consumableItem);

    /**
     * 根据ID删除可消费项目
     *
     * @param id 可消费项目ID
     */
    void deleteById(Integer id);

    /**
     * 根据酒店ID查询可消费项目列表
     *
     * @param hotelId 酒店ID
     * @return 可消费项目列表
     */
    List<ConsumableItem> findByHotelId(Integer hotelId);

    /**
     * 根据分类查询可消费项目列表
     *
     * @param category 分类
     * @return 可消费项目列表
     */
    List<ConsumableItem> findByCategory(String category);

    /**
     * 根据激活状态查询可消费项目列表
     *
     * @param isActive 激活状态
     * @return 可消费项目列表
     */
    List<ConsumableItem> findByIsActive(Boolean isActive);

    /**
     * 根据酒店ID和分类查询可消费项目列表
     *
     * @param hotelId  酒店ID
     * @param category 分类
     * @return 可消费项目列表
     */
    List<ConsumableItem> findByHotelIdAndCategory(Integer hotelId, String category);

    /**
     * 根据酒店ID和激活状态查询可消费项目列表
     *
     * @param hotelId  酒店ID
     * @param isActive 激活状态
     * @return 可消费项目列表
     */
    List<ConsumableItem> findByHotelIdAndIsActive(Integer hotelId, Boolean isActive);
}