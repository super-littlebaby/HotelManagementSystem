package com.project.hotelmanagementsystem.service;

import com.project.hotelmanagementsystem.dto.reservation.CheckInReservationRequest;
import com.project.hotelmanagementsystem.dto.reservation.CreateReservationRequest;
import com.project.hotelmanagementsystem.dto.reservation.ReservationResponse;
import com.project.hotelmanagementsystem.entity.Guest;
import com.project.hotelmanagementsystem.entity.Reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 预订Service接口
 */
public interface ReservationService {

    /**
     * 根据ID查询预订
     *
     * @param id 预订ID
     * @return 预订信息
     */
    Optional<Reservation> findById(Integer id);

    /**
     * 根据ID查询预订详情（包含房间明细、酒店、客人信息）
     *
     * @param id 预订ID
     * @return 预订详情响应
     */
    Optional<ReservationResponse> findDetailById(Integer id);

    /**
     * 查询所有预订
     *
     * @return 预订列表
     */
    List<Reservation> findAll();

    /**
     * 保存/更新预订
     *
     * @param reservation 预订信息
     * @return 保存后的预订信息
     */
    Reservation save(Reservation reservation);

    /**
     * 根据ID删除预订
     *
     * @param id 预订ID
     */
    void deleteById(Integer id);

    /**
     * 根据客人ID查询预订列表
     *
     * @param guestId 客人ID
     * @return 预订列表
     */
    List<Reservation> findByGuestId(Integer guestId);

    /**
     * 根据状态查询预订列表
     *
     * @param status 预订状态
     * @return 预订列表
     */
    List<Reservation> findByStatus(String status);

    /**
     * 根据员工ID查询预订列表
     *
     * @param employeeId 员工ID
     * @return 预订列表
     */
    List<Reservation> findByEmployeeId(Integer employeeId);

    /**
     * 办理退房（员工端）
     *
     * @param id 预订ID
     * @return 预订详情
     */
    ReservationResponse checkOutReservation(Integer id);

    /**
     * 根据客人ID和状态查询预订列表
     *
     * @param guestId 客人ID
     * @param status  预订状态
     * @return 预订列表
     */
    List<Reservation> findByGuestIdAndStatus(Integer guestId, String status);

    /**
     * 根据入住日期范围查询预订列表
     *
     * @param checkInDate  入住日期
     * @param checkOutDate 退房日期
     * @return 预订列表
     */
    List<Reservation> findByCheckInDateBetween(LocalDate checkInDate, LocalDate checkOutDate);

    /**
     * 创建预订
     *
     * @param request 创建预订请求
     * @param guest   客人信息
     * @return 预订详情响应
     */
    ReservationResponse createReservation(CreateReservationRequest request, Guest guest);

    /**
     * 确认预订（分配房间）
     *
     * @param id         预订ID
     * @param employeeId 员工ID
     * @param roomId     房间ID
     * @return 预订详情响应
     */
    ReservationResponse confirmReservation(Integer id, Integer employeeId, Integer roomId);

    /**
     * 取消预订
     *
     * @param id 预订ID
     * @return 预订详情响应
     */
    ReservationResponse cancelReservation(Integer id);

    /**
     * 办理入住（按房间携带实际入住人信息）
     *
     * @param id         预订ID
     * @param request    按房间分组的入住人信息
     * @param employeeId 办理入住的员工ID
     * @return 预订详情响应
     */
    ReservationResponse checkInReservation(Integer id, CheckInReservationRequest request, Integer employeeId);

    /**
     * 分配房间（在已确认状态下分配房间）
     *
     * @param id     预订ID
     * @param roomId 房间ID
     * @return 预订详情响应
     */
    ReservationResponse assignRoom(Integer id, Integer roomId);

    /**
     * 分配房间并可更换房型
     *
     * @param id        预订ID
     * @param roomId    房间ID
     * @param roomTypeId 房型ID（可选，更换房型时使用）
     * @return 预订详情响应
     */
    ReservationResponse assignRoom(Integer id, Integer roomId, Integer roomTypeId);

    /**
     * 根据客人ID查询预订详情列表
     *
     * @param guestId 客人ID
     * @return 预订详情列表
     */
    List<ReservationResponse> findDetailByGuestId(Integer guestId);

    /**
     * 根据客人手机号查询预订列表
     *
     * @param phone 客人手机号
     * @return 预订详情列表
     */
    List<ReservationResponse> findByGuestPhone(String phone);

    /**
     * 根据客人邮箱查询预订列表
     *
     * @param email 客人邮箱
     * @return 预订详情列表
     */
    List<ReservationResponse> findByGuestEmail(String email);

    /**
     * 根据客人姓名查询预订列表
     *
     * @param name 客人姓名
     * @return 预订详情列表
     */
    List<ReservationResponse> findByGuestName(String name);

    /**
     * 按酒店过滤查询所有预订
     *
     * @param hotelId 酒店ID（为空则查询全部）
     * @return 预订详情列表
     */
    List<ReservationResponse> findAllWithHotelFilter(Integer hotelId);

    /**
     * 自动更新过期预订状态为"未到场"
     * 查询时动态判断，将超过入住日期且未入住/取消的预订标记为 no_show
     *
     * @return 更新的记录数
     */
    int autoUpdateExpiredToNoShow();
}
