package com.project.hotelmanagementsystem.service.impl;

import com.project.hotelmanagementsystem.entity.RoomTypeFacility;
import com.project.hotelmanagementsystem.entity.RoomTypeFacilityId;
import com.project.hotelmanagementsystem.repository.RoomTypeFacilityRepository;
import com.project.hotelmanagementsystem.service.RoomTypeFacilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 房型-设施关联Service实现类
 */
@Service
@Transactional
public class RoomTypeFacilityServiceImpl implements RoomTypeFacilityService {

    private final RoomTypeFacilityRepository roomTypeFacilityRepository;

    @Autowired
    public RoomTypeFacilityServiceImpl(RoomTypeFacilityRepository roomTypeFacilityRepository) {
        this.roomTypeFacilityRepository = roomTypeFacilityRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RoomTypeFacility> findById(RoomTypeFacilityId id) {
        return roomTypeFacilityRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomTypeFacility> findAll() {
        return roomTypeFacilityRepository.findAll();
    }

    @Override
    public RoomTypeFacility save(RoomTypeFacility roomTypeFacility) {
        return roomTypeFacilityRepository.save(roomTypeFacility);
    }

    @Override
    public void deleteById(RoomTypeFacilityId id) {
        roomTypeFacilityRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomTypeFacility> findByRoomTypeId(Integer roomTypeId) {
        return roomTypeFacilityRepository.findByRoomTypeId(roomTypeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomTypeFacility> findByFacilityId(Integer facilityId) {
        return roomTypeFacilityRepository.findByFacilityId(facilityId);
    }
}