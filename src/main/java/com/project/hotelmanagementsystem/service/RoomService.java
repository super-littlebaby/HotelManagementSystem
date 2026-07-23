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
}