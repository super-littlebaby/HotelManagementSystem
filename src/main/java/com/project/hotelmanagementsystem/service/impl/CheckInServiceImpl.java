package com.project.hotelmanagementsystem.service.impl;

import com.project.hotelmanagementsystem.entity.CheckIn;
import com.project.hotelmanagementsystem.repository.CheckInRepository;
import com.project.hotelmanagementsystem.service.CheckInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 入住登记Service实现类
 */
@Service
@Transactional
public class CheckInServiceImpl implements CheckInService {

    private final CheckInRepository checkInRepository;

    @Autowired
    public CheckInServiceImpl(CheckInRepository checkInRepository) {
        this.checkInRepository = checkInRepository;
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
}