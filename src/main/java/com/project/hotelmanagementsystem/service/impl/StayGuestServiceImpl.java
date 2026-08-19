package com.project.hotelmanagementsystem.service.impl;

import com.project.hotelmanagementsystem.entity.StayGuest;
import com.project.hotelmanagementsystem.repository.StayGuestRepository;
import com.project.hotelmanagementsystem.service.StayGuestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 同住客人服务实现类
 */
@Service
@Transactional
public class StayGuestServiceImpl implements StayGuestService {

    private final StayGuestRepository stayGuestRepository;

    @Autowired
    public StayGuestServiceImpl(StayGuestRepository stayGuestRepository) {
        this.stayGuestRepository = stayGuestRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StayGuest> findById(Integer id) {
        return stayGuestRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StayGuest> findAll() {
        return stayGuestRepository.findAll();
    }

    @Override
    public StayGuest save(StayGuest stayGuest) {
        return stayGuestRepository.save(stayGuest);
    }

    @Override
    public void deleteById(Integer id) {
        stayGuestRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StayGuest> findByCheckInId(Integer checkInId) {
        return stayGuestRepository.findByCheckInId(checkInId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StayGuest> findByGuestId(Integer guestId) {
        return stayGuestRepository.findByGuestId(guestId);
    }

    @Override
    public List<StayGuest> saveAll(List<StayGuest> stayGuests) {
        return stayGuestRepository.saveAll(stayGuests);
    }

    @Override
    public void deleteByCheckInId(Integer checkInId) {
        stayGuestRepository.deleteByCheckInId(checkInId);
    }
}