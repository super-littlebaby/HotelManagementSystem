package com.project.hotelmanagementsystem.service.impl;

import com.project.hotelmanagementsystem.dto.checkin.CreateCheckInRequest;
import com.project.hotelmanagementsystem.dto.reservation.CreateReservationRequest;
import com.project.hotelmanagementsystem.dto.reservation.ReservationResponse;
import com.project.hotelmanagementsystem.dto.reservation.ReservationRoomRequest;
import com.project.hotelmanagementsystem.dto.reservation.ReservationRoomResponse;
import com.project.hotelmanagementsystem.entity.*;
import com.project.hotelmanagementsystem.repository.*;
import com.project.hotelmanagementsystem.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 预订Service实现类
 */
@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationRoomRepository reservationRoomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final HotelRepository hotelRepository;
    private final CheckInRepository checkInRepository;
    private final StayGuestRepository stayGuestRepository;
    private final BillRepository billRepository;

    @Autowired
    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  ReservationRoomRepository reservationRoomRepository,
                                  RoomTypeRepository roomTypeRepository,
                                  RoomRepository roomRepository,
                                  GuestRepository guestRepository,
                                  HotelRepository hotelRepository,
                                  CheckInRepository checkInRepository,
                                  StayGuestRepository stayGuestRepository,
                                  BillRepository billRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationRoomRepository = reservationRoomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.roomRepository = roomRepository;
        this.guestRepository = guestRepository;
        this.hotelRepository = hotelRepository;
        this.checkInRepository = checkInRepository;
        this.stayGuestRepository = stayGuestRepository;
        this.billRepository = billRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Reservation> findById(Integer id) {
        return reservationRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReservationResponse> findDetailById(Integer id) {
        return reservationRepository.findById(id)
                .map(this::convertToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    @Override
    public Reservation save(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    @Override
    public void deleteById(Integer id) {
        reservationRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findByGuestId(Integer guestId) {
        return reservationRepository.findByGuestId(guestId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findByStatus(String status) {
        return reservationRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findByEmployeeId(Integer employeeId) {
        return reservationRepository.findByEmployeeId(employeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findByGuestIdAndStatus(Integer guestId, String status) {
        return reservationRepository.findByGuestIdAndStatus(guestId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findByCheckInDateBetween(LocalDate checkInDate, LocalDate checkOutDate) {
        return reservationRepository.findByCheckInDateBetween(checkInDate, checkOutDate);
    }

    @Override
    public ReservationResponse createReservation(CreateReservationRequest request, Guest guest) {
        // 校验日期
        LocalDate today = LocalDate.now();
        
        // 入住日期不能早于今天
        if (request.getCheckInDate().isBefore(today)) {
            throw new IllegalArgumentException("入住日期不能早于今天");
        }
        
        // 退房日期必须晚于入住日期
        if (request.getCheckOutDate().isBefore(request.getCheckInDate()) ||
            request.getCheckOutDate().isEqual(request.getCheckInDate())) {
            throw new IllegalArgumentException("退房日期必须晚于入住日期");
        }

        // 校验房型并获取酒店ID
        Integer hotelId = null;
        List<ReservationRoom> reservationRooms = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        long nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());

        for (ReservationRoomRequest roomRequest : request.getRooms()) {
            RoomType roomType = roomTypeRepository.findById(roomRequest.getRoomTypeId())
                    .orElseThrow(() -> new IllegalArgumentException("房型不存在: " + roomRequest.getRoomTypeId()));

            // 确保所有房间属于同一酒店
            if (hotelId == null) {
                hotelId = roomType.getHotelId();
            } else if (!hotelId.equals(roomType.getHotelId())) {
                throw new IllegalArgumentException("所有预订房间必须属于同一酒店");
            }

            // 校验人数
            int adults = roomRequest.getAdults() != null ? roomRequest.getAdults() : 1;
            int children = roomRequest.getChildren() != null ? roomRequest.getChildren() : 0;

            if (roomType.getMaxAdults() != null && adults > roomType.getMaxAdults()) {
                throw new IllegalArgumentException("房型 " + roomType.getName() + " 最多容纳 " + roomType.getMaxAdults() + " 位成人");
            }
            if (roomType.getMaxChildren() != null && children > roomType.getMaxChildren()) {
                throw new IllegalArgumentException("房型 " + roomType.getName() + " 最多容纳 " + roomType.getMaxChildren() + " 位儿童");
            }

            // 锁定房价
            BigDecimal ratePerNight = roomType.getBasePrice();
            BigDecimal roomTotal = ratePerNight.multiply(BigDecimal.valueOf(nights));
            totalAmount = totalAmount.add(roomTotal);

            ReservationRoom reservationRoom = new ReservationRoom();
            reservationRoom.setRoomTypeId(roomType.getId());
            reservationRoom.setAdults(adults);
            reservationRoom.setChildren(children);
            reservationRoom.setRatePerNight(ratePerNight);
            reservationRooms.add(reservationRoom);
        }

        // 创建预订主单
        Reservation reservation = new Reservation();
        reservation.setGuestId(guest.getId());
        reservation.setHotelId(hotelId);
        reservation.setBookingDate(LocalDateTime.now());
        reservation.setCheckInDate(request.getCheckInDate());
        reservation.setCheckOutDate(request.getCheckOutDate());
        reservation.setStatus("pending");
        reservation.setTotalAmount(totalAmount);
        reservation.setSpecialRequests(request.getSpecialRequests());
        reservation.setChannel(request.getChannel());

        Reservation savedReservation = reservationRepository.save(reservation);

        // 保存预订房间明细
        for (ReservationRoom room : reservationRooms) {
            room.setReservationId(savedReservation.getId());
            reservationRoomRepository.save(room);
        }

        return convertToResponse(savedReservation);
    }

    @Override
    public ReservationResponse confirmReservation(Integer id, Integer employeeId, Integer roomId) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("预订不存在: " + id));

        if (!"pending".equals(reservation.getStatus())) {
            throw new IllegalStateException("只有待确认状态的预订才能确认");
        }

        // 分配房间
        if (roomId != null) {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("房间不存在: " + roomId));

            if (!room.getHotelId().equals(reservation.getHotelId())) {
                throw new IllegalArgumentException("房间不属于该预订的酒店");
            }

            // 找到对应的预订房间明细并分配房间
            List<ReservationRoom> rooms = reservationRoomRepository.findByReservationId(id);
            if (rooms.isEmpty()) {
                throw new IllegalStateException("预订没有房间明细");
            }

            // 分配给第一个未分配房间的明细
            boolean assigned = false;
            for (ReservationRoom rr : rooms) {
                if (rr.getRoomId() == null && rr.getRoomTypeId().equals(room.getRoomTypeId())) {
                    rr.setRoomId(roomId);
                    reservationRoomRepository.save(rr);
                    assigned = true;
                    break;
                }
            }

            if (!assigned) {
                // 如果没有找到匹配的房型明细，检查是否所有房间都是同一种房型
                if (rooms.size() == 1) {
                    ReservationRoom rr = rooms.get(0);
                    rr.setRoomId(roomId);
                    reservationRoomRepository.save(rr);
                } else {
                    throw new IllegalArgumentException("未找到匹配房型的房间明细进行分配");
                }
            }
        }

        reservation.setStatus("confirmed");
        reservation.setEmployeeId(employeeId);
        Reservation saved = reservationRepository.save(reservation);

        return convertToResponse(saved);
    }

    @Override
    public ReservationResponse cancelReservation(Integer id) {
        // 验证预订存在
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("预订不存在: " + id));

        // 验证状态（仅 pending/confirmed 可取消）
        String status = reservation.getStatus();
        if ("cancelled".equals(status)) {
            throw new IllegalStateException("预订已取消");
        }
        if ("checked_in".equals(status) || "checked_out".equals(status)) {
            throw new IllegalStateException("已入住或已退房的预订不能取消");
        }

        // 更新状态为 cancelled
        reservation.setStatus("cancelled");

        // 释放已分配的房间
        List<ReservationRoom> reservationRooms = reservationRoomRepository.findByReservationId(id);
        for (ReservationRoom rr : reservationRooms) {
            if (rr.getRoomId() != null) {
                Room room = roomRepository.findById(rr.getRoomId()).orElse(null);
                if (room != null) {
                    room.setStatus("vacant");
                    roomRepository.save(room);
                }
                rr.setRoomId(null);
                reservationRoomRepository.save(rr);
            }
        }

        Reservation saved = reservationRepository.save(reservation);

        return convertToResponse(saved);
    }

    @Override
    public ReservationResponse checkInReservation(Integer id, List<CreateCheckInRequest.StayGuestRequest> stayGuests) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("预订不存在: " + id));

        if (!"confirmed".equals(reservation.getStatus())) {
            throw new IllegalStateException("只有已确认状态的预订才能办理入住");
        }

        List<ReservationRoom> rooms = reservationRoomRepository.findByReservationId(id);
        boolean allRoomsAssigned = rooms.stream().allMatch(r -> r.getRoomId() != null);
        if (!allRoomsAssigned) {
            throw new IllegalStateException("请先为所有房间分配房间号再办理入住");
        }

        // 加载主登记客人档案，用于填充入住记录和同住客人表中的个人信息
        Guest guest = guestRepository.findById(reservation.getGuestId())
                .orElseThrow(() -> new IllegalStateException("预订关联的客人档案不存在: " + reservation.getGuestId()));
        String primaryGuestName = (guest.getFirstName() != null ? guest.getFirstName() : "")
                + (guest.getLastName() != null ? guest.getLastName() : "");
        if (primaryGuestName.isEmpty()) {
            primaryGuestName = "未知";
        }

        // 为每个房间创建入住记录
        for (ReservationRoom rr : rooms) {
            if (rr.getRoomId() == null) {
                continue;
            }

            int adults = rr.getAdults() != null ? rr.getAdults() : 1;
            int children = rr.getChildren() != null ? rr.getChildren() : 0;

            // 1. 创建入住登记记录（从客人档案填充个人信息）
            CheckIn checkIn = new CheckIn();
            checkIn.setReservationId(id);
            checkIn.setHotelId(reservation.getHotelId());
            checkIn.setGuestId(reservation.getGuestId());
            checkIn.setGuestName(primaryGuestName);
            checkIn.setIdType(guest.getIdType());
            checkIn.setIdNumber(guest.getIdNumber());
            checkIn.setPhone(guest.getPhone());
            checkIn.setRoomId(rr.getRoomId());
            checkIn.setAdults(adults);
            checkIn.setChildren(children);
            checkIn.setCheckInTime(LocalDateTime.now());
            checkIn.setExpectedCheckOutTime(reservation.getCheckOutDate() != null ? reservation.getCheckOutDate().atStartOfDay() : null);
            checkIn.setStatus("in_house");
            checkIn.setRatePerNight(rr.getRatePerNight());
            checkIn.setNotes(reservation.getSpecialRequests());

            // 先保存入住记录，获取入住登记ID（确保同住客人表的check_in_id不为空）
            CheckIn savedCheckIn = checkInRepository.save(checkIn);

            // 2. 创建同住客人记录
            // 第一条始终为主登记人（个人信息从客人档案填充，guest_id为主登记人的guest_id）
            List<StayGuest> stayGuestList = new ArrayList<>();
            StayGuest primaryStayGuest = new StayGuest();
            primaryStayGuest.setCheckInId(savedCheckIn.getId());
            primaryStayGuest.setGuestId(reservation.getGuestId());
            primaryStayGuest.setName(primaryGuestName);
            primaryStayGuest.setIdType(guest.getIdType());
            primaryStayGuest.setIdNumber(guest.getIdNumber());
            primaryStayGuest.setIsPrimary(true);
            stayGuestList.add(primaryStayGuest);

            // 其余同住客人按实际填写的数量登记（guest_id统一设为主登记人的guest_id）
            if (stayGuests != null) {
                for (CreateCheckInRequest.StayGuestRequest req : stayGuests) {
                    if (req == null || req.getName() == null || req.getName().trim().isEmpty()) {
                        continue;
                    }
                    StayGuest sg = new StayGuest();
                    sg.setCheckInId(savedCheckIn.getId());
                    sg.setGuestId(reservation.getGuestId());
                    sg.setName(req.getName());
                    sg.setIdType(req.getIdType());
                    sg.setIdNumber(req.getIdNumber());
                    sg.setIsPrimary(false);
                    stayGuestList.add(sg);
                }
            }

            // 保存所有同住客人记录（此时checkInId已确定，不会出现为空的情况）
            stayGuestRepository.saveAll(stayGuestList);

            // 3. 为入住记录创建账单
            Bill bill = new Bill();
            bill.setCheckInId(savedCheckIn.getId());
            bill.setBillStatus("open");
            bill.setTotalAmount(BigDecimal.ZERO);
            bill.setPaidAmount(BigDecimal.ZERO);
            bill.setDepositAmount(BigDecimal.ZERO);
            bill.setCreatedAt(LocalDateTime.now());
            billRepository.save(bill);

            // 4. 更新房间状态为已入住
            roomRepository.findById(rr.getRoomId()).ifPresent(room -> {
                room.setStatus("occupied");
                roomRepository.save(room);
            });
        }

        reservation.setStatus("checked_in");
        Reservation saved = reservationRepository.save(reservation);

        return convertToResponse(saved);
    }

    @Override
    public ReservationResponse checkOutReservation(Integer id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("预订不存在: " + id));

        if (!"checked_in".equals(reservation.getStatus())) {
            throw new IllegalStateException("只有已入住状态的预订才能办理退房");
        }

        // 更新房间状态为脏房（待清洁）
        List<ReservationRoom> rooms = reservationRoomRepository.findByReservationId(id);
        for (ReservationRoom rr : rooms) {
            if (rr.getRoomId() != null) {
                roomRepository.findById(rr.getRoomId()).ifPresent(room -> {
                    room.setStatus("dirty");
                    roomRepository.save(room);
                });
            }
        }

        reservation.setStatus("checked_out");
        Reservation saved = reservationRepository.save(reservation);

        return convertToResponse(saved);
    }

    @Override
    public ReservationResponse assignRoom(Integer id, Integer roomId) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("预订不存在: " + id));

        if (!"confirmed".equals(reservation.getStatus())) {
            throw new IllegalStateException("只有已确认状态的预订才能分配房间");
        }

        if (roomId == null) {
            throw new IllegalArgumentException("房间ID不能为空");
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("房间不存在: " + roomId));

        if (!room.getHotelId().equals(reservation.getHotelId())) {
            throw new IllegalArgumentException("房间不属于该预订的酒店");
        }

        List<ReservationRoom> rooms = reservationRoomRepository.findByReservationId(id);
        if (rooms.isEmpty()) {
            throw new IllegalStateException("预订没有房间明细");
        }

        boolean assigned = false;
        for (ReservationRoom rr : rooms) {
            if (rr.getRoomId() == null && rr.getRoomTypeId().equals(room.getRoomTypeId())) {
                rr.setRoomId(roomId);
                reservationRoomRepository.save(rr);
                assigned = true;
                break;
            }
        }

        if (!assigned) {
            if (rooms.size() == 1) {
                ReservationRoom rr = rooms.get(0);
                rr.setRoomId(roomId);
                reservationRoomRepository.save(rr);
            } else {
                throw new IllegalArgumentException("未找到匹配房型的房间明细进行分配");
            }
        }

        return convertToResponse(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> findDetailByGuestId(Integer guestId) {
        List<Reservation> reservations = reservationRepository.findByGuestId(guestId);
        return reservations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> findByGuestPhone(String phone) {
        List<Reservation> reservations = reservationRepository.findByGuestPhone(phone);
        return reservations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> findByGuestEmail(String email) {
        List<Reservation> reservations = reservationRepository.findByGuestEmail(email);
        return reservations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> findByGuestName(String name) {
        List<Reservation> reservations = reservationRepository.findByGuestName(name);
        return reservations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> findAllWithHotelFilter(Integer hotelId) {
        List<Reservation> reservations;
        if (hotelId != null) {
            reservations = reservationRepository.findByHotelId(hotelId);
        } else {
            reservations = reservationRepository.findAll();
        }
        return reservations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 将预订实体转换为响应DTO
     *
     * @param reservation 预订实体
     * @return 预订响应DTO
     */
    private ReservationResponse convertToResponse(Reservation reservation) {
        ReservationResponse response = new ReservationResponse();
        response.setId(reservation.getId());
        response.setGuestId(reservation.getGuestId());
        response.setHotelId(reservation.getHotelId());
        response.setBookingDate(reservation.getBookingDate());
        response.setCheckInDate(reservation.getCheckInDate());
        response.setCheckOutDate(reservation.getCheckOutDate());
        response.setStatus(reservation.getStatus());
        response.setTotalAmount(reservation.getTotalAmount());
        response.setSpecialRequests(reservation.getSpecialRequests());
        response.setEmployeeId(reservation.getEmployeeId());
        response.setChannel(reservation.getChannel());

        // 加载客人姓名
        if (reservation.getGuestId() != null) {
            guestRepository.findById(reservation.getGuestId()).ifPresent(guest ->
                    response.setGuestName(guest.getFirstName() + guest.getLastName())
            );
        }

        // 加载酒店名称
        if (reservation.getHotelId() != null) {
            hotelRepository.findById(reservation.getHotelId()).ifPresent(hotel ->
                    response.setHotelName(hotel.getName())
            );
        }

        // 加载房间明细
        List<ReservationRoom> reservationRooms = reservationRoomRepository.findByReservationId(reservation.getId());
        List<ReservationRoomResponse> roomResponses = reservationRooms.stream()
                .map(this::convertRoomToResponse)
                .collect(Collectors.toList());
        response.setRooms(roomResponses);

        return response;
    }

    /**
     * 将预订房间明细转换为响应DTO
     *
     * @param reservationRoom 预订房间明细
     * @return 房间响应DTO
     */
    private ReservationRoomResponse convertRoomToResponse(ReservationRoom reservationRoom) {
        ReservationRoomResponse response = new ReservationRoomResponse();
        response.setId(reservationRoom.getId());
        response.setRoomTypeId(reservationRoom.getRoomTypeId());
        response.setRoomId(reservationRoom.getRoomId());
        response.setAdults(reservationRoom.getAdults());
        response.setChildren(reservationRoom.getChildren());
        response.setRatePerNight(reservationRoom.getRatePerNight());

        // 加载房型名称
        if (reservationRoom.getRoomTypeId() != null) {
            roomTypeRepository.findById(reservationRoom.getRoomTypeId()).ifPresent(roomType ->
                    response.setRoomTypeName(roomType.getName())
            );
        }

        // 加载房间号
        if (reservationRoom.getRoomId() != null) {
            roomRepository.findById(reservationRoom.getRoomId()).ifPresent(room ->
                    response.setRoomNumber(room.getRoomNumber())
            );
        }

        return response;
    }
}
