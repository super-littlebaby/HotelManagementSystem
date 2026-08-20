package com.project.hotelmanagementsystem.service;

import com.project.hotelmanagementsystem.dto.roomstatuslog.RoomStatusLogDTO;
import com.project.hotelmanagementsystem.entity.RoomStatusLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
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
     * 查询所有房间状态变更日志（携带关联信息）
     *
     * @return 房间状态变更日志列表
     */
    List<RoomStatusLog> findAll();

    /**
     * 保存房间状态变更日志
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

    /**
     * 根据酒店ID查询状态变更日志列表
     *
     * @param hotelId 酒店ID
     * @return 状态变更日志列表
     */
    List<RoomStatusLog> findByHotelId(Integer hotelId);

    /**
     * 根据新状态查询状态变更日志列表
     *
     * @param newStatus 新状态
     * @return 状态变更日志列表
     */
    List<RoomStatusLog> findByNewStatus(String newStatus);

    /**
     * 按时间范围查询状态变更日志列表
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 状态变更日志列表
     */
    List<RoomStatusLog> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 多条件组合分页查询
     *
     * @param hotelId   酒店ID（可空）
     * @param roomId    房间ID（可空）
     * @param newStatus 新状态（可空）
     * @param changedBy 操作人ID（可空）
     * @param startTime 变更开始时间（可空）
     * @param endTime   变更结束时间（可空）
     * @param pageable  分页参数
     * @return 分页结果
     */
    Page<RoomStatusLog> search(Integer hotelId, Integer roomId, String newStatus,
                                Integer changedBy, LocalDateTime startTime,
                                LocalDateTime endTime, Pageable pageable);

    /**
     * 统一记录状态变更日志的入口
     * <p>
     * 由 RoomService.updateStatus / CheckInService 等业务调用，
     * 保证所有状态变更都通过同一入口写入日志，避免遗漏。
     * </p>
     *
     * @param roomId     房间ID
     * @param oldStatus   原状态
     * @param newStatus   新状态
     * @param changedBy  操作人ID（可空）
     * @param notes      备注
     * @return 保存后的日志
     */
    RoomStatusLog logStatusChange(Integer roomId, String oldStatus, String newStatus,
                                   Integer changedBy, String notes);

    /**
     * 将实体转换为DTO
     *
     * @param log 日志实体
     * @return DTO
     */
    RoomStatusLogDTO convertToDTO(RoomStatusLog log);

    /**
     * 将实体列表转换为DTO列表
     *
     * @param logs 日志实体列表
     * @return DTO列表
     */
    List<RoomStatusLogDTO> convertToDTOList(List<RoomStatusLog> logs);
}
