package com.project.hotelmanagementsystem.service.impl;

import com.project.hotelmanagementsystem.entity.Reservation;
import com.project.hotelmanagementsystem.repository.ReservationRepository;
import com.project.hotelmanagementsystem.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 预订Service实现类
 */
@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;

    @Autowired
    public ReservationServiceImpl(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Reservation> findById(Integer id) {
        return reservationRepository.findById(id);
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
}