package com.project.hotelmanagementsystem.repository;

import com.project.hotelmanagementsystem.entity.RoomStatusLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 房间状态变更日志Repository接口
 */
@Repository
public interface RoomStatusLogRepository extends JpaRepository<RoomStatusLog, Integer> {

    /**
     * 根据房间ID查询状态变更日志列表（携带关联信息），按变更时间倒序
     *
     * @param roomId 房间ID
     * @return 状态变更日志列表
     */
    @Query("SELECT l FROM RoomStatusLog l " +
           "LEFT JOIN FETCH l.room r " +
           "LEFT JOIN FETCH r.hotel " +
           "LEFT JOIN FETCH l.employee " +
           "WHERE l.roomId = :roomId " +
           "ORDER BY l.changedAt DESC")
    List<RoomStatusLog> findByRoomId(@Param("roomId") Integer roomId);

    /**
     * 根据操作人ID查询状态变更日志列表（携带关联信息），按变更时间倒序
     *
     * @param changedBy 操作人ID
     * @return 状态变更日志列表
     */
    @Query("SELECT l FROM RoomStatusLog l " +
           "LEFT JOIN FETCH l.room r " +
           "LEFT JOIN FETCH r.hotel " +
           "LEFT JOIN FETCH l.employee " +
           "WHERE l.changedBy = :changedBy " +
           "ORDER BY l.changedAt DESC")
    List<RoomStatusLog> findByChangedBy(@Param("changedBy") Integer changedBy);

    /**
     * 根据酒店ID查询状态变更日志列表（携带关联信息），按变更时间倒序
     *
     * @param hotelId 酒店ID
     * @return 状态变更日志列表
     */
    @Query("SELECT l FROM RoomStatusLog l " +
           "LEFT JOIN FETCH l.room r " +
           "LEFT JOIN FETCH r.hotel h " +
           "LEFT JOIN FETCH l.employee " +
           "WHERE h.id = :hotelId " +
           "ORDER BY l.changedAt DESC")
    List<RoomStatusLog> findByHotelId(@Param("hotelId") Integer hotelId);

    /**
     * 根据新状态查询日志列表
     *
     * @param newStatus 新状态
     * @return 状态变更日志列表
     */
    @Query("SELECT l FROM RoomStatusLog l " +
           "LEFT JOIN FETCH l.room r " +
           "LEFT JOIN FETCH r.hotel " +
           "LEFT JOIN FETCH l.employee " +
           "WHERE l.newStatus = :newStatus " +
           "ORDER BY l.changedAt DESC")
    List<RoomStatusLog> findByNewStatus(@Param("newStatus") String newStatus);

    /**
     * 按时间范围查询日志列表
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 状态变更日志列表
     */
    @Query("SELECT l FROM RoomStatusLog l " +
           "LEFT JOIN FETCH l.room r " +
           "LEFT JOIN FETCH r.hotel " +
           "LEFT JOIN FETCH l.employee " +
           "WHERE l.changedAt BETWEEN :startTime AND :endTime " +
           "ORDER BY l.changedAt DESC")
    List<RoomStatusLog> findByChangedAtBetween(@Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);

    /**
     * 多条件组合分页查询（所有条件均可选）
     *
     * @param hotelId    酒店ID（可空）
     * @param roomId     房间ID（可空）
     * @param newStatus  新状态（可空）
     * @param changedBy  操作人ID（可空）
     * @param startTime  变更开始时间（可空）
     * @param endTime    变更结束时间（可空）
     * @param pageable   分页参数
     * @return 分页结果
     */
    @Query("SELECT l FROM RoomStatusLog l " +
           "LEFT JOIN FETCH l.room r " +
           "LEFT JOIN FETCH r.hotel h " +
           "LEFT JOIN FETCH l.employee " +
           "WHERE (:hotelId IS NULL OR h.id = :hotelId) " +
           "AND (:roomId IS NULL OR l.roomId = :roomId) " +
           "AND (:newStatus IS NULL OR l.newStatus = :newStatus) " +
           "AND (:changedBy IS NULL OR l.changedBy = :changedBy) " +
           "AND (:startTime IS NULL OR l.changedAt >= :startTime) " +
           "AND (:endTime IS NULL OR l.changedAt <= :endTime) " +
           "ORDER BY l.changedAt DESC")
    Page<RoomStatusLog> search(@Param("hotelId") Integer hotelId,
                                @Param("roomId") Integer roomId,
                                @Param("newStatus") String newStatus,
                                @Param("changedBy") Integer changedBy,
                                @Param("startTime") LocalDateTime startTime,
                                @Param("endTime") LocalDateTime endTime,
                                Pageable pageable);

    /**
     * 查询所有日志（携带关联信息），按变更时间倒序
     *
     * @return 状态变更日志列表
     */
    @Query("SELECT l FROM RoomStatusLog l " +
           "LEFT JOIN FETCH l.room r " +
           "LEFT JOIN FETCH r.hotel " +
           "LEFT JOIN FETCH l.employee " +
           "ORDER BY l.changedAt DESC")
    List<RoomStatusLog> findAllWithRelations();
}
