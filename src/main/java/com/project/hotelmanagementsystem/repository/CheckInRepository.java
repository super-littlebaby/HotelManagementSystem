package com.project.hotelmanagementsystem.repository;

import com.project.hotelmanagementsystem.entity.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 入住登记Repository接口
 */
public interface CheckInRepository extends JpaRepository<CheckIn, Integer> {

    /**
     * 根据客人ID查询入住记录列表
     *
     * @param guestId 客人ID
     * @return 入住记录列表
     */
    List<CheckIn> findByGuestId(Integer guestId);

    /**
     * 根据房间ID查询入住记录列表
     *
     * @param roomId 房间ID
     * @return 入住记录列表
     */
    List<CheckIn> findByRoomId(Integer roomId);

    /**
     * 根据状态查询入住记录列表
     *
     * @param status 入住状态
     * @return 入住记录列表
     */
    List<CheckIn> findByStatus(String status);

    /**
     * 根据预订ID查询入住记录
     *
     * @param reservationId 预订ID
     * @return 入住记录列表
     */
    List<CheckIn> findByReservationId(Integer reservationId);

    /**
     * 根据房间ID和状态查询入住记录
     *
     * @param roomId 房间ID
     * @param status 入住状态
     * @return 入住记录列表
     */
    List<CheckIn> findByRoomIdAndStatus(Integer roomId, String status);

    /**
     * 根据酒店ID查询入住记录列表
     *
     * @param hotelId 酒店ID
     * @return 入住记录列表
     */
    @Query("SELECT c FROM CheckIn c WHERE c.hotelId = :hotelId")
    List<CheckIn> findAllByHotelId(@Param("hotelId") Integer hotelId);

    /**
     * 根据入住记录ID查询关联的酒店ID
     *
     * @param checkInId 入住记录ID
     * @return 酒店ID
     */
    @Query("SELECT c.hotelId FROM CheckIn c WHERE c.id = :checkInId")
    Optional<Integer> findHotelIdByCheckInId(@Param("checkInId") Integer checkInId);

    /**
     * 根据客人ID和酒店ID查询入住记录列表
     *
     * @param guestId 客人ID
     * @param hotelId 酒店ID
     * @return 入住记录列表
     */
    @Query("SELECT c FROM CheckIn c WHERE c.guestId = :guestId AND c.hotelId = :hotelId")
    List<CheckIn> findByGuestIdAndHotelId(@Param("guestId") Integer guestId, @Param("hotelId") Integer hotelId);

    /**
     * 根据房间ID和酒店ID查询入住记录列表
     *
     * @param roomId 房间ID
     * @param hotelId 酒店ID
     * @return 入住记录列表
     */
    @Query("SELECT c FROM CheckIn c WHERE c.roomId = :roomId AND c.hotelId = :hotelId")
    List<CheckIn> findByRoomIdAndHotelId(@Param("roomId") Integer roomId, @Param("hotelId") Integer hotelId);

    /**
     * 根据状态和酒店ID查询入住记录列表
     *
     * @param status 入住状态
     * @param hotelId 酒店ID
     * @return 入住记录列表
     */
    @Query("SELECT c FROM CheckIn c WHERE c.status = :status AND c.hotelId = :hotelId")
    List<CheckIn> findByStatusAndHotelId(@Param("status") String status, @Param("hotelId") Integer hotelId);

    /**
     * 根据预订ID和酒店ID查询入住记录列表
     *
     * @param reservationId 预订ID
     * @param hotelId 酒店ID
     * @return 入住记录列表
     */
    @Query("SELECT c FROM CheckIn c WHERE c.reservationId = :reservationId AND c.hotelId = :hotelId")
    List<CheckIn> findByReservationIdAndHotelId(@Param("reservationId") Integer reservationId, @Param("hotelId") Integer hotelId);

    /**
     * 根据房间ID、状态和酒店ID查询入住记录列表
     *
     * @param roomId 房间ID
     * @param status 入住状态
     * @param hotelId 酒店ID
     * @return 入住记录列表
     */
    @Query("SELECT c FROM CheckIn c WHERE c.roomId = :roomId AND c.status = :status AND c.hotelId = :hotelId")
    List<CheckIn> findByRoomIdAndStatusAndHotelId(@Param("roomId") Integer roomId, @Param("status") String status, @Param("hotelId") Integer hotelId);

    @Query("SELECT c FROM CheckIn c WHERE c.idNumber = :idNumber AND c.status = :status")
    List<CheckIn> findByIdNumberAndStatus(@Param("idNumber") String idNumber, @Param("status") String status);

    @Query("SELECT c FROM CheckIn c WHERE c.guestId = :guestId AND c.status = :status")
    List<CheckIn> findByGuestIdAndStatus(@Param("guestId") Integer guestId, @Param("status") String status);
}
