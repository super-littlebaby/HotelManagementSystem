package com.project.hotelmanagementsystem.repository;

import com.project.hotelmanagementsystem.entity.ConsumableItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 可消费项目Repository接口
 */
@Repository
public interface ConsumableItemRepository extends JpaRepository<ConsumableItem, Integer> {

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
