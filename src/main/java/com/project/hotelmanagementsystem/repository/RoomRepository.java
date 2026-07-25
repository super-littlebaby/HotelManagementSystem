package com.project.hotelmanagementsystem.repository;

import com.project.hotelmanagementsystem.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 房间Repository接口
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {

    /**
     * 查询所有房间，包含关联的酒店和房型信息
     *
     * @return 房间列表
     */
    @Query("SELECT r FROM Room r JOIN FETCH r.hotel JOIN FETCH r.roomType ORDER BY r.id")
    List<Room> findAllWithRelations();

    /**
     * 根据ID查询房间，包含关联的酒店和房型信息
     *
     * @param id 房间ID
     * @return 房间信息
     */
    @Query("SELECT r FROM Room r JOIN FETCH r.hotel JOIN FETCH r.roomType WHERE r.id = :id")
    Optional<Room> findByIdWithRelations(Integer id);

    /**
     * 根据房型ID查询房间列表，包含关联的酒店和房型信息
     *
     * @param roomTypeId 房型ID
     * @return 房间列表
     */
    @Query("SELECT r FROM Room r JOIN FETCH r.hotel JOIN FETCH r.roomType WHERE r.roomTypeId = :roomTypeId")
    List<Room> findByRoomTypeId(Integer roomTypeId);

    /**
     * 根据状态查询房间列表，包含关联的酒店和房型信息
     *
     * @param status 房间状态
     * @return 房间列表
     */
    @Query("SELECT r FROM Room r JOIN FETCH r.hotel JOIN FETCH r.roomType WHERE r.status = :status")
    List<Room> findByStatus(String status);

    /**
     * 根据房间号查询房间，包含关联的酒店和房型信息
     *
     * @param roomNumber 房间号
     * @return 房间信息
     */
    @Query("SELECT r FROM Room r JOIN FETCH r.hotel JOIN FETCH r.roomType WHERE r.roomNumber = :roomNumber")
    Optional<Room> findByRoomNumber(String roomNumber);

    /**
     * 根据房型ID和状态查询房间列表，包含关联的酒店和房型信息
     *
     * @param roomTypeId 房型ID
     * @param status     房间状态
     * @return 房间列表
     */
    @Query("SELECT r FROM Room r JOIN FETCH r.hotel JOIN FETCH r.roomType WHERE r.roomTypeId = :roomTypeId AND r.status = :status")
    List<Room> findByRoomTypeIdAndStatus(Integer roomTypeId, String status);

    /**
     * 根据酒店ID查询房间列表，包含关联的酒店和房型信息
     *
     * @param hotelId 酒店ID
     * @return 房间列表
     */
    @Query("SELECT r FROM Room r JOIN FETCH r.hotel JOIN FETCH r.roomType WHERE r.hotelId = :hotelId")
    List<Room> findByHotelId(Integer hotelId);

    /**
     * 根据酒店ID和状态查询房间列表，包含关联的酒店和房型信息
     *
     * @param hotelId 酒店ID
     * @param status  房间状态
     * @return 房间列表
     */
    @Query("SELECT r FROM Room r JOIN FETCH r.hotel JOIN FETCH r.roomType WHERE r.hotelId = :hotelId AND r.status = :status")
    List<Room> findByHotelIdAndStatus(Integer hotelId, String status);
}