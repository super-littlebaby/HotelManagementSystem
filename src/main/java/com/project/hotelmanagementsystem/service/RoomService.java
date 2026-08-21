package com.project.hotelmanagementsystem.service;

import com.project.hotelmanagementsystem.entity.Room;

import java.util.List;
import java.util.Optional;

/**
 * 房间Service接口
 */
public interface RoomService {

    /**
     * 根据ID查询房间
     *
     * @param id 房间ID
     * @return 房间信息
     */
    Optional<Room> findById(Integer id);

    /**
     * 查询所有房间
     *
     * @return 房间列表
     */
    List<Room> findAll();

    /**
     * 保存/更新房间
     *
     * @param room 房间信息
     * @return 保存后的房间信息
     */
    Room save(Room room);

    /**
     * 根据ID删除房间
     *
     * @param id 房间ID
     */
    void deleteById(Integer id);

    /**
     * 根据房型ID查询房间列表
     *
     * @param roomTypeId 房型ID
     * @return 房间列表
     */
    List<Room> findByRoomTypeId(Integer roomTypeId);

    /**
     * 根据状态查询房间列表
     *
     * @param status 房间状态
     * @return 房间列表
     */
    List<Room> findByStatus(String status);

    /**
     * 根据房间号查询房间
     *
     * @param roomNumber 房间号
     * @return 房间信息
     */
    Optional<Room> findByRoomNumber(String roomNumber);

    /**
     * 根据房型ID和状态查询房间列表
     *
     * @param roomTypeId 房型ID
     * @param status     房间状态
     * @return 房间列表
     */
    List<Room> findByRoomTypeIdAndStatus(Integer roomTypeId, String status);

    /**
     * 根据酒店ID查询房间列表
     *
     * @param hotelId 酒店ID
     * @return 房间列表
     */
    List<Room> findByHotelId(Integer hotelId);

    /**
     * 根据酒店ID和状态查询房间列表
     *
     * @param hotelId 酒店ID
     * @param status  房间状态
     * @return 房间列表
     */
    List<Room> findByHotelIdAndStatus(Integer hotelId, String status);

    /**
     * 查询可用房间（过滤掉已被确认/入住预订占用的房间）
     * 用于预订分配房间场景
     */
    List<Room> findAvailableByRoomTypeIdAndStatus(Integer roomTypeId, String status);

    /**
     * 查询可用房间（过滤掉已被确认/入住预订占用的房间）
     * 用于预订分配房间场景
     */
    List<Room> findAvailableByHotelIdAndStatus(Integer hotelId, String status);

    /**
     * 更新房间状态并记录日志
     *
     * @param id        房间ID
     * @param newStatus 新状态
     * @param changedBy 操作人ID
     * @param notes     备注
     * @return 更新后的房间信息
     */
    Room updateStatus(Integer id, String newStatus, Integer changedBy, String notes);

    /**
     * 将房间实体转换为DTO
     *
     * @param room 房间实体
     * @return DTO
     */
    java.util.Map<String, Object> convertToDTO(Room room);

    /**
     * 将房间实体列表转换为DTO列表
     *
     * @param rooms 房间实体列表
     * @return DTO列表
     */
    java.util.List<java.util.Map<String, Object>> convertToDTOList(java.util.List<Room> rooms);
}