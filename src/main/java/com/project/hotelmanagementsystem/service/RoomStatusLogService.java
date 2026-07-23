package com.project.hotelmanagementsystem.service;

import com.project.hotelmanagementsystem.entity.RoomStatusLog;

import java.util.List;
import java.util.Optional;

/**
 * 房间状态变更日志Service接口
 */
public interface RoomStatusLogService {

    /**
     * 根据ID查询房间状态变更日志
     *
     * @param id 日志ID
     * @return 房间状态变更日志信息
     */
    Optional<RoomStatusLog> findById(Integer id);

    /**
     * 查询所有房间状态变更日志
     *
     * @return 房间状态变更日志列表
     */
    List<RoomStatusLog> findAll();

    /**
     * 保存/更新房间状态变更日志
     *
     * @param roomStatusLog 房间状态变更日志信息
     * @return 保存后的房间状态变更日志信息
     */
    RoomStatusLog save(RoomStatusLog roomStatusLog);

    /**
     * 根据ID删除房间状态变更日志
     *
     * @param id 日志ID
     */
    void deleteById(Integer id);

    /**
     * 根据房间ID查询状态变更日志列表
     *
     * @param roomId 房间ID
     * @return 状态变更日志列表
     */
    List<RoomStatusLog> findByRoomId(Integer roomId);

    /**
     * 根据操作人ID查询状态变更日志列表
     *
     * @param changedBy 操作人ID
     * @return 状态变更日志列表
     */
    List<RoomStatusLog> findByChangedBy(Integer changedBy);
}