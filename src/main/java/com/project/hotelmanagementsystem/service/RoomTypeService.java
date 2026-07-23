package com.project.hotelmanagementsystem.service;

import com.project.hotelmanagementsystem.entity.RoomType;

import java.util.List;
import java.util.Optional;

/**
 * 房型Service接口
 */
public interface RoomTypeService {

    /**
     * 根据ID查询房型
     *
     * @param id 房型ID
     * @return 房型信息
     */
    Optional<RoomType> findById(Integer id);

    /**
     * 查询所有房型
     *
     * @return 房型列表
     */
    List<RoomType> findAll();

    /**
     * 保存/更新房型
     *
     * @param roomType 房型信息
     * @return 保存后的房型信息
     */
    RoomType save(RoomType roomType);

    /**
     * 根据ID删除房型
     *
     * @param id 房型ID
     */
    void deleteById(Integer id);

    /**
     * 根据酒店ID查询房型列表
     *
     * @param hotelId 酒店ID
     * @return 房型列表
     */
    List<RoomType> findByHotelId(Integer hotelId);

    /**
     * 根据酒店ID和床型查询房型列表
     *
     * @param hotelId 酒店ID
     * @param bedType 床型
     * @return 房型列表
     */
    List<RoomType> findByHotelIdAndBedType(Integer hotelId, String bedType);
}