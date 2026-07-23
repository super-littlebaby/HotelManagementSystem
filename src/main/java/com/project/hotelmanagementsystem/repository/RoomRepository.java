package com.project.hotelmanagementsystem.repository;

import com.project.hotelmanagementsystem.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 房间Repository接口
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {

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
