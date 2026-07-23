package com.project.hotelmanagementsystem.service.impl;

import com.project.hotelmanagementsystem.entity.Guest;
import com.project.hotelmanagementsystem.repository.GuestRepository;
import com.project.hotelmanagementsystem.service.GuestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 客人档案Service实现类
 */
@Service
@Transactional
public class GuestServiceImpl implements GuestService {

    private final GuestRepository guestRepository;

    @Autowired
    public GuestServiceImpl(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Guest> findById(Integer id) {
        return guestRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Guest> findAll() {
        return guestRepository.findAll();
    }

    @Override
    public Guest save(Guest guest) {
        return guestRepository.save(guest);
    }

    @Override
    public void deleteById(Integer id) {
        guestRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Guest> findByIdNumber(String idNumber) {
        return guestRepository.findByIdNumber(idNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Guest> findByPhone(String phone) {
        return guestRepository.findByPhone(phone);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Guest> findByEmail(String email) {
        return guestRepository.findByEmail(email);
    }
}