package com.project.hotelmanagementsystem.repository;

import com.project.hotelmanagementsystem.entity.RoomStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 房间状态变更日志Repository接口
 */
@Repository
public interface RoomStatusLogRepository extends JpaRepository<RoomStatusLog, Integer> {

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
