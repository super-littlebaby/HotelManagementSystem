package com.project.hotelmanagementsystem.service.impl;

import com.project.hotelmanagementsystem.entity.ReservationRoom;
import com.project.hotelmanagementsystem.repository.ReservationRoomRepository;
import com.project.hotelmanagementsystem.service.ReservationRoomService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 预订房间明细Service实现类
 */
@Service
@Transactional
public class ReservationRoomServiceImpl implements ReservationRoomService {

    private final ReservationRoomRepository reservationRoomRepository;

    public ReservationRoomServiceImpl(ReservationRoomRepository reservationRoomRepository) {
        this.reservationRoomRepository = reservationRoomRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReservationRoom> findById(Integer id) {
        return reservationRoomRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationRoom> findAll() {
        return reservationRoomRepository.findAll();
    }

    @Override
    public ReservationRoom save(ReservationRoom reservationRoom) {
        return reservationRoomRepository.save(reservationRoom);
    }

    @Override
    public void deleteById(Integer id) {
        reservationRoomRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationRoom> findByReservationId(Integer reservationId) {
        return reservationRoomRepository.findByReservationId(reservationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationRoom> findByRoomId(Integer roomId) {
        return reservationRoomRepository.findByRoomId(roomId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationRoom> findByRoomTypeId(Integer roomTypeId) {
        return reservationRoomRepository.findByRoomTypeId(roomTypeId);
    }
}