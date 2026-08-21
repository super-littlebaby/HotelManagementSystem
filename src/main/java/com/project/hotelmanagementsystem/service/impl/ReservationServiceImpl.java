package com.project.hotelmanagementsystem.service.impl;

import com.project.hotelmanagementsystem.dto.checkin.CreateCheckInRequest;
import com.project.hotelmanagementsystem.dto.reservation.CheckInReservationRequest;
import com.project.hotelmanagementsystem.dto.reservation.CreateReservationRequest;
import com.project.hotelmanagementsystem.dto.reservation.ReservationResponse;
import com.project.hotelmanagementsystem.dto.reservation.ReservationRoomRequest;
import com.project.hotelmanagementsystem.dto.reservation.ReservationRoomResponse;
import com.project.hotelmanagementsystem.entity.*;
import com.project.hotelmanagementsystem.repository.*;
import com.project.hotelmanagementsystem.service.ReservationService;
import com.project.hotelmanagementsystem.util.EncryptionUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    private final PaymentRepository paymentRepository;

    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  ReservationRoomRepository reservationRoomRepository,
                                  RoomTypeRepository roomTypeRepository,
                                  RoomRepository roomRepository,
                                  GuestRepository guestRepository,
                                  HotelRepository hotelRepository,
                                  CheckInRepository checkInRepository,
                                  StayGuestRepository stayGuestRepository,
                                  BillRepository billRepository,
                                  PaymentRepository paymentRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationRoomRepository = reservationRoomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.roomRepository = roomRepository;
        this.guestRepository = guestRepository;
        this.hotelRepository = hotelRepository;
        this.checkInRepository = checkInRepository;
        this.stayGuestRepository = stayGuestRepository;
        this.billRepository = billRepository;
        this.paymentRepository = paymentRepository;
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
        reservation.setHotelId(hotelId);
        reservation.setBookingDate(LocalDateTime.now());
        reservation.setCheckInDate(request.getCheckInDate());
        reservation.setCheckOutDate(request.getCheckOutDate());
        reservation.setStatus("pending");
        reservation.setTotalAmount(totalAmount);
        reservation.setSpecialRequests(request.getSpecialRequests());
        reservation.setChannel(request.getChannel());

        if (guest != null) {
            // 有账号客人：同时冗余写入客人快照信息（避免后续查询 JOIN guests 表，并保留预订时的客人信息）
            reservation.setGuestId(guest.getId());
            reservation.setGuestName((guest.getFirstName() != null ? guest.getFirstName() : "")
                    + (guest.getLastName() != null ? guest.getLastName() : ""));
            reservation.setIdType(guest.getIdType());
            // guests.id_number 已加密存储，直接拷贝
            reservation.setIdNumber(guest.getIdNumber());
            reservation.setPhone(guest.getPhone());
        } else {
            // 线下/电话预订无账号：写入预订本身记录的客人信息
            reservation.setGuestName(request.getGuestName());
            reservation.setIdType(request.getIdType() != null ? request.getIdType() : "id_card");
            // 证件号加密存储
            if (request.getIdNumber() != null && !request.getIdNumber().isEmpty()) {
                reservation.setIdNumber(EncryptionUtil.encrypt(request.getIdNumber()));
            }
            reservation.setPhone(request.getPhone());
        }

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

            if (reservation.getHotelId() == null || !room.getHotelId().equals(reservation.getHotelId())) {
                throw new IllegalArgumentException("房间不属于该预订的酒店");
            }

            // 找到对应的预订房间明细并分配房间
            List<ReservationRoom> rooms = reservationRoomRepository.findByReservationId(id);
            if (rooms.isEmpty()) {
                throw new IllegalStateException("预订没有房间明细");
            }

            // 检查该房间是否已被本预订的其他房间明细使用
            boolean alreadyAssignedInThisReservation = rooms.stream()
                    .anyMatch(rr -> roomId.equals(rr.getRoomId()));
            if (alreadyAssignedInThisReservation) {
                throw new IllegalStateException("该房间已分配给本预订中的其他房间明细，不能重复分配");
            }

            // 检查该房间是否已被其他已确认的预订占用
            List<ReservationRoom> otherReservationsRooms = reservationRoomRepository.findByRoomId(roomId);
            for (ReservationRoom other : otherReservationsRooms) {
                if (!other.getReservationId().equals(id)) {
                    Reservation otherRes = reservationRepository.findById(other.getReservationId()).orElse(null);
                    if (otherRes != null && ("confirmed".equals(otherRes.getStatus()) || "checked_in".equals(otherRes.getStatus()))) {
                        throw new IllegalStateException("该房间已被其他预订占用，无法分配");
                    }
                }
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
                    throw new IllegalArgumentException("未找到匹配房型的房间明细进行分配（预订房型与所选房间房型不一致）");
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
    public ReservationResponse checkInReservation(Integer id, CheckInReservationRequest request, Integer employeeId) {
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

        // 构建按 reservationRoomId 索引的入住人信息映射
        java.util.Map<Integer, CheckInReservationRequest.RoomCheckInRequest> roomCheckInMap = new java.util.HashMap<>();
        if (request != null && request.getRooms() != null) {
            for (CheckInReservationRequest.RoomCheckInRequest rc : request.getRooms()) {
                if (rc.getReservationRoomId() != null) {
                    roomCheckInMap.put(rc.getReservationRoomId(), rc);
                }
            }
        }

        // ====== 校验阶段：收集所有入住人信息并做重复/在住检查 ======
        Set<String> allIdNumbers = new HashSet<>();

        for (ReservationRoom rr : rooms) {
            if (rr.getRoomId() == null) continue;
            CheckInReservationRequest.RoomCheckInRequest roomCheckIn = roomCheckInMap.get(rr.getId());
            if (roomCheckIn == null) continue;

            // 主登记人
            String primaryIdNum = roomCheckIn.getPrimaryIdNumber();
            if (primaryIdNum != null && !primaryIdNum.trim().isEmpty()) {
                if (!allIdNumbers.add(primaryIdNum.trim())) {
                    throw new IllegalStateException("证件号 " + primaryIdNum + " 在本次入住中重复填写");
                }
                // 检查是否已在住
                List<CheckIn> inHouse = checkInRepository.findByIdNumberAndStatus(primaryIdNum.trim(), "in_house");
                if (!inHouse.isEmpty()) {
                    throw new IllegalStateException("证件号 " + primaryIdNum + " 的客人已在住中，不能重复入住");
                }
            }

            // 同住人员
            if (roomCheckIn.getStayGuests() != null) {
                for (CreateCheckInRequest.StayGuestRequest sg : roomCheckIn.getStayGuests()) {
                    if (sg == null || sg.getIdNumber() == null || sg.getIdNumber().trim().isEmpty()) continue;
                    String sgIdNum = sg.getIdNumber().trim();
                    if (!allIdNumbers.add(sgIdNum)) {
                        throw new IllegalStateException("证件号 " + sgIdNum + " 在本次入住中重复填写");
                    }
                    List<CheckIn> inHouse = checkInRepository.findByIdNumberAndStatus(sgIdNum, "in_house");
                    if (!inHouse.isEmpty()) {
                        throw new IllegalStateException("证件号 " + sgIdNum + " 的客人已在住中，不能重复入住");
                    }
                }
            }
        }
        // ====== 校验结束 ======

        // 为每个房间创建入住记录
        for (ReservationRoom rr : rooms) {
            if (rr.getRoomId() == null) {
                continue;
            }

            int adults = rr.getAdults() != null ? rr.getAdults() : 1;
            int children = rr.getChildren() != null ? rr.getChildren() : 0;

            CheckInReservationRequest.RoomCheckInRequest roomCheckIn = roomCheckInMap.get(rr.getId());
            String primaryGuestName;
            String primaryIdType = null;
            String primaryIdNumber = null;
            String primaryPhone = null;
            Integer primaryGuestId = null;

            if (roomCheckIn != null) {
                // 使用前端提交的实际入住人信息
                primaryGuestName = roomCheckIn.getPrimaryGuestName();
                primaryIdType = roomCheckIn.getPrimaryIdType();
                primaryIdNumber = roomCheckIn.getPrimaryIdNumber();
                primaryPhone = roomCheckIn.getPrimaryPhone();

                // 优先使用预订本身的 guestId（本人入住场景）
                if (reservation.getGuestId() != null) {
                    primaryGuestId = reservation.getGuestId();
                }

                // 再尝试按证件号匹配 guests 表中的档案
                if (primaryIdNumber != null && !primaryIdNumber.isEmpty()) {
                    String encryptedId = EncryptionUtil.encrypt(primaryIdNumber);
                    Optional<Guest> matchedGuest = guestRepository.findByIdNumber(encryptedId);
                    if (matchedGuest.isPresent()) {
                        primaryGuestId = matchedGuest.get().getId();
                    }
                }
            } else {
                throw new IllegalStateException("缺少房间明细(ID=" + rr.getId() + ")的入住人信息");
            }

            if (primaryGuestName == null || primaryGuestName.trim().isEmpty()) {
                throw new IllegalStateException("房间明细(ID=" + rr.getId() + ")的主登记人姓名不能为空");
            }

            // 1. 创建入住登记记录（使用前端提交的实际入住人信息）
            CheckIn checkIn = new CheckIn();
            checkIn.setReservationId(id);
            checkIn.setHotelId(reservation.getHotelId());
            checkIn.setGuestId(primaryGuestId);
            checkIn.setGuestName(primaryGuestName);
            checkIn.setIdType(primaryIdType);
            checkIn.setIdNumber(primaryIdNumber);
            checkIn.setPhone(primaryPhone);
            checkIn.setRoomId(rr.getRoomId());
            checkIn.setAdults(adults);
            checkIn.setChildren(children);
            checkIn.setCheckInTime(LocalDateTime.now());
            checkIn.setExpectedCheckOutTime(reservation.getCheckOutDate() != null ? reservation.getCheckOutDate().atStartOfDay() : null);
            checkIn.setStatus("in_house");
            checkIn.setRatePerNight(rr.getRatePerNight());
            checkIn.setNotes(reservation.getSpecialRequests());

            CheckIn savedCheckIn = checkInRepository.save(checkIn);

            // 2. 创建同住客人记录（主登记人 + 同住人员）
            List<StayGuest> stayGuestList = new ArrayList<>();

            // 主登记人
            StayGuest primaryStayGuest = new StayGuest();
            primaryStayGuest.setCheckInId(savedCheckIn.getId());
            primaryStayGuest.setGuestId(primaryGuestId);
            primaryStayGuest.setName(primaryGuestName);
            primaryStayGuest.setIdType(primaryIdType);
            primaryStayGuest.setIdNumber(primaryIdNumber);
            primaryStayGuest.setIsPrimary(true);
            stayGuestList.add(primaryStayGuest);

            // 同住人员
            if (roomCheckIn.getStayGuests() != null) {
                for (CreateCheckInRequest.StayGuestRequest req : roomCheckIn.getStayGuests()) {
                    if (req == null || req.getName() == null || req.getName().trim().isEmpty()) {
                        continue;
                    }
                    StayGuest sg = new StayGuest();
                    sg.setCheckInId(savedCheckIn.getId());
                    sg.setName(req.getName());
                    sg.setIdType(req.getIdType());
                    sg.setIdNumber(req.getIdNumber());
                    sg.setIsPrimary(false);

                    // 为同住人员查找对应的 guest_id
                    if (req.getIdNumber() != null && !req.getIdNumber().isEmpty()) {
                        String encryptedId = EncryptionUtil.encrypt(req.getIdNumber());
                        guestRepository.findByIdNumber(encryptedId)
                                .ifPresent(g -> sg.setGuestId(g.getId()));
                    }

                    stayGuestList.add(sg);
                }
            }

            stayGuestRepository.saveAll(stayGuestList);

            // 3. 计算押金并创建账单
            BigDecimal ratePerNight = rr.getRatePerNight() != null ? rr.getRatePerNight() : BigDecimal.ZERO;
            long days = 1;
            if (reservation.getCheckInDate() != null && reservation.getCheckOutDate() != null) {
                days = ChronoUnit.DAYS.between(reservation.getCheckInDate(), reservation.getCheckOutDate());
                days = Math.max(days, 1);
            }
            BigDecimal depositAmount = ratePerNight.multiply(BigDecimal.valueOf(days));

            Bill bill = new Bill();
            bill.setCheckInId(savedCheckIn.getId());
            bill.setBillStatus("open");
            bill.setTotalAmount(BigDecimal.ZERO);
            bill.setPaidAmount(depositAmount);
            bill.setDepositAmount(depositAmount);
            bill.setCreatedAt(LocalDateTime.now());
            Bill savedBill = billRepository.save(bill);

            // 创建押金支付记录
            Payment payment = new Payment();
            payment.setBillId(savedBill.getId());
            payment.setAmount(depositAmount);
            String depositMethod = roomCheckIn.getDepositPaymentMethod();
            payment.setPaymentMethod(depositMethod != null && !depositMethod.isEmpty() ? depositMethod : "cash");
            payment.setPaymentType("deposit");
            payment.setPaymentDate(LocalDateTime.now());
            payment.setEmployeeId(employeeId);
            paymentRepository.save(payment);

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
        return assignRoom(id, roomId, null);
    }

    @Override
    public ReservationResponse assignRoom(Integer id, Integer roomId, Integer roomTypeId) {
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

        if (reservation.getHotelId() == null || !room.getHotelId().equals(reservation.getHotelId())) {
            throw new IllegalArgumentException("房间不属于该预订的酒店");
        }

        List<ReservationRoom> rooms = reservationRoomRepository.findByReservationId(id);
        if (rooms.isEmpty()) {
            throw new IllegalStateException("预订没有房间明细");
        }

        // 检查该房间是否已被本预订的其他房间明细使用
        boolean alreadyAssignedInThisReservation = rooms.stream()
                .anyMatch(rr -> roomId.equals(rr.getRoomId()));
        if (alreadyAssignedInThisReservation) {
            throw new IllegalStateException("该房间已分配给本预订中的其他房间明细，不能重复分配");
        }

        // 检查该房间是否已被其他已确认的预订占用
        List<ReservationRoom> otherReservationsRooms = reservationRoomRepository.findByRoomId(roomId);
        for (ReservationRoom other : otherReservationsRooms) {
            if (!other.getReservationId().equals(id)) {
                Reservation otherRes = reservationRepository.findById(other.getReservationId()).orElse(null);
                if (otherRes != null && ("confirmed".equals(otherRes.getStatus()) || "checked_in".equals(otherRes.getStatus()))) {
                    throw new IllegalStateException("该房间已被其他预订占用，无法分配");
                }
            }
        }

        // 如果更换了房型，需要更新房间明细的房型和价格
        Integer effectiveRoomTypeId = roomTypeId;
        BigDecimal effectiveRatePerNight = null;

        if (roomTypeId != null && !roomTypeId.equals(room.getRoomTypeId())) {
            RoomType newRoomType = roomTypeRepository.findById(roomTypeId)
                    .orElseThrow(() -> new IllegalArgumentException("房型不存在: " + roomTypeId));
            effectiveRatePerNight = newRoomType.getBasePrice();
        } else {
            effectiveRoomTypeId = room.getRoomTypeId();
        }

        boolean assigned = false;
        for (ReservationRoom rr : rooms) {
            if (rr.getRoomId() == null && (effectiveRoomTypeId.equals(rr.getRoomTypeId()) || roomTypeId != null)) {
                rr.setRoomId(roomId);
                if (roomTypeId != null) {
                    rr.setRoomTypeId(effectiveRoomTypeId);
                    if (effectiveRatePerNight != null) {
                        rr.setRatePerNight(effectiveRatePerNight);
                    }
                }
                reservationRoomRepository.save(rr);
                assigned = true;
                break;
            }
        }

        if (!assigned) {
            if (rooms.size() == 1) {
                ReservationRoom rr = rooms.get(0);
                rr.setRoomId(roomId);
                if (roomTypeId != null) {
                    rr.setRoomTypeId(effectiveRoomTypeId);
                    if (effectiveRatePerNight != null) {
                        rr.setRatePerNight(effectiveRatePerNight);
                    }
                }
                reservationRoomRepository.save(rr);
            } else {
                throw new IllegalArgumentException("未找到匹配房型的房间明细进行分配（预订房型与所选房间房型不一致）");
            }
        }

        // 如果更换了房型，更新预订总金额
        if (roomTypeId != null) {
            recalculateReservationTotalAmount(reservation);
        }

        return convertToResponse(reservation);
    }

    private void recalculateReservationTotalAmount(Reservation reservation) {
        List<ReservationRoom> rooms = reservationRoomRepository.findByReservationId(reservation.getId());
        if (rooms.isEmpty() || reservation.getCheckInDate() == null || reservation.getCheckOutDate() == null) {
            return;
        }

        long nights = ChronoUnit.DAYS.between(reservation.getCheckInDate(), reservation.getCheckOutDate());
        nights = Math.max(nights, 1);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ReservationRoom rr : rooms) {
            BigDecimal rate = rr.getRatePerNight() != null ? rr.getRatePerNight() : BigDecimal.ZERO;
            totalAmount = totalAmount.add(rate.multiply(BigDecimal.valueOf(nights)));
        }

        reservation.setTotalAmount(totalAmount);
        reservationRepository.save(reservation);
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
    public List<ReservationResponse> findAllWithHotelFilter(Integer hotelId) {
        autoUpdateExpiredToNoShow();
        
        List<Reservation> reservations;
        if (hotelId != null) {
            reservations = reservationRepository.findByHotelId(hotelId);
        } else {
            reservations = reservationRepository.findAll();
        }
        return reservations.stream()
                .sorted(Comparator.comparing(Reservation::getBookingDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
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

        // 加载客人姓名、手机、证件（统一从预订上的冗余快照取，避免每次 JOIN guests 表）
        response.setGuestName(reservation.getGuestName());
        response.setGuestPhone(reservation.getPhone());
        response.setGuestIdType(reservation.getIdType());
        if (reservation.getIdNumber() != null && !reservation.getIdNumber().isEmpty()) {
            response.setGuestIdNumber(EncryptionUtil.decrypt(reservation.getIdNumber()));
        }
        // email 仅在有账号客人时从 guests 表补充（预订表未冗余 email）
        if (reservation.getGuestId() != null) {
            guestRepository.findById(reservation.getGuestId()).ifPresent(guest ->
                    response.setGuestEmail(guest.getEmail())
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

    @Override
    @Transactional
    public int autoUpdateExpiredToNoShow() {
        return reservationRepository.updateExpiredReservationsToNoShow(LocalDate.now());
    }
}
