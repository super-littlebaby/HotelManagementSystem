package com.project.hotelmanagementsystem.service.impl;

import com.project.hotelmanagementsystem.entity.CheckIn;
import com.project.hotelmanagementsystem.entity.Guest;
import com.project.hotelmanagementsystem.entity.Room;
import com.project.hotelmanagementsystem.entity.StayGuest;
import com.project.hotelmanagementsystem.repository.CheckInRepository;
import com.project.hotelmanagementsystem.repository.GuestRepository;
import com.project.hotelmanagementsystem.repository.RoomRepository;
import com.project.hotelmanagementsystem.repository.StayGuestRepository;
import com.project.hotelmanagementsystem.service.CheckInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 入住登记Service实现类
 */
@Service
@Transactional
public class CheckInServiceImpl implements CheckInService {

    private final CheckInRepository checkInRepository;
    private final RoomRepository roomRepository;
    private final StayGuestRepository stayGuestRepository;
    private final GuestRepository guestRepository;

    @Autowired
    public CheckInServiceImpl(CheckInRepository checkInRepository, RoomRepository roomRepository, StayGuestRepository stayGuestRepository, GuestRepository guestRepository) {
        this.checkInRepository = checkInRepository;
        this.roomRepository = roomRepository;
        this.stayGuestRepository = stayGuestRepository;
        this.guestRepository = guestRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CheckIn> findById(Integer id) {
        return checkInRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheckIn> findAll() {
        return checkInRepository.findAll();
    }

    @Override
    public CheckIn save(CheckIn checkIn) {
        return checkInRepository.save(checkIn);
    }

    @Override
    public void deleteById(Integer id) {
        checkInRepository.deleteById(id);
    }

    @Override
    public CheckIn saveWithStayGuests(CheckIn checkIn, List<StayGuest> stayGuests) {
        if (checkIn.getGuestId() == null && checkIn.getIdNumber() != null && !checkIn.getIdNumber().isEmpty()) {
            Optional<Guest> existingGuest = guestRepository.findByIdNumber(checkIn.getIdNumber());
            if (existingGuest.isPresent()) {
                checkIn.setGuestId(existingGuest.get().getId());
                if (checkIn.getGuestName() == null || checkIn.getGuestName().isEmpty()) {
                    checkIn.setGuestName(existingGuest.get().getFirstName() + " " + existingGuest.get().getLastName());
                }
                if (checkIn.getPhone() == null || checkIn.getPhone().isEmpty()) {
                    checkIn.setPhone(existingGuest.get().getPhone());
                }
            }
        }

        if (checkIn.getExpectedCheckOutTime() != null && checkIn.getCheckInTime() != null) {
            // 只比较日期部分，要求退房日期必须晚于或等于入住日期
            if (checkIn.getExpectedCheckOutTime().toLocalDate().isBefore(checkIn.getCheckInTime().toLocalDate())) {
                throw new IllegalArgumentException("预计退房日期必须晚于或等于入住日期");
            }
        }

        CheckIn saved = checkInRepository.save(checkIn);

        // 构建同住客人列表，包含主登记人
        List<StayGuest> allStayGuests = new java.util.ArrayList<>();

        // 添加主登记人到同住客人表
        StayGuest primaryGuest = new StayGuest();
        primaryGuest.setCheckInId(saved.getId());
        primaryGuest.setGuestId(checkIn.getGuestId());
        primaryGuest.setName(checkIn.getGuestName());
        primaryGuest.setIdType(checkIn.getIdType());
        primaryGuest.setIdNumber(checkIn.getIdNumber());
        primaryGuest.setIsPrimary(true);
        allStayGuests.add(primaryGuest);

        // 添加其他同住人员
        if (stayGuests != null && !stayGuests.isEmpty()) {
            for (StayGuest sg : stayGuests) {
                sg.setCheckInId(saved.getId());
                sg.setIsPrimary(false);
                if (sg.getGuestId() == null && sg.getIdNumber() != null && !sg.getIdNumber().isEmpty()) {
                    Optional<Guest> existingGuest = guestRepository.findByIdNumber(sg.getIdNumber());
                    existingGuest.ifPresent(guest -> sg.setGuestId(guest.getId()));
                }
                allStayGuests.add(sg);
            }
        }

        stayGuestRepository.saveAll(allStayGuests);

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheckIn> findByGuestId(Integer guestId) {
        return checkInRepository.findByGuestId(guestId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheckIn> findByRoomId(Integer roomId) {
        return checkInRepository.findByRoomId(roomId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheckIn> findByStatus(String status) {
        return checkInRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheckIn> findByReservationId(Integer reservationId) {
        return checkInRepository.findByReservationId(reservationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheckIn> findByRoomIdAndStatus(Integer roomId, String status) {
        return checkInRepository.findByRoomIdAndStatus(roomId, status);
    }

    @Override
    public CheckIn checkOut(Integer id) {
        CheckIn checkIn = checkInRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("入住记录不存在: " + id));

        if (!"in_house".equals(checkIn.getStatus())) {
            throw new IllegalStateException("只有在住状态的入住记录才能办理退房");
        }

        // 更新入住记录状态为已退房
        checkIn.setStatus("checked_out");
        checkIn.setActualCheckOutTime(LocalDateTime.now());

        // 更新房间状态为待打扫
        Room room = roomRepository.findById(checkIn.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("房间不存在: " + checkIn.getRoomId()));
        room.setStatus("dirty");
        roomRepository.save(room);

        return checkInRepository.save(checkIn);
    }
}