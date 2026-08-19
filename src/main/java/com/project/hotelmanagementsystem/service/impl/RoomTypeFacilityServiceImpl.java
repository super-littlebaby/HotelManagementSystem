package com.project.hotelmanagementsystem.service.impl;

import com.project.hotelmanagementsystem.entity.Facility;
import com.project.hotelmanagementsystem.entity.RoomTypeFacility;
import com.project.hotelmanagementsystem.entity.RoomTypeFacilityId;
import com.project.hotelmanagementsystem.repository.FacilityRepository;
import com.project.hotelmanagementsystem.repository.RoomTypeFacilityRepository;
import com.project.hotelmanagementsystem.service.RoomTypeFacilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 房型-设施关联Service实现类
 */
@Service
@Transactional
public class RoomTypeFacilityServiceImpl implements RoomTypeFacilityService {

    private final RoomTypeFacilityRepository roomTypeFacilityRepository;
    private final FacilityRepository facilityRepository;

    @Autowired
    public RoomTypeFacilityServiceImpl(RoomTypeFacilityRepository roomTypeFacilityRepository,
                                       FacilityRepository facilityRepository) {
        this.roomTypeFacilityRepository = roomTypeFacilityRepository;
        this.facilityRepository = facilityRepository;
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

    @Override
    @Transactional(readOnly = true)
    public List<Facility> findFacilitiesByRoomTypeId(Integer roomTypeId) {
        List<Integer> facilityIds = roomTypeFacilityRepository.findFacilityIdsByRoomTypeId(roomTypeId);
        if (facilityIds.isEmpty()) {
            return new ArrayList<>();
        }
        return facilityRepository.findAllById(facilityIds);
    }

    @Override
    public void addFacilitiesToRoomType(Integer roomTypeId, List<Integer> facilityIds) {
        if (facilityIds == null || facilityIds.isEmpty()) {
            return;
        }
        // 查询已存在的关联，避免重复
        List<Integer> existingFacilityIds = roomTypeFacilityRepository.findFacilityIdsByRoomTypeId(roomTypeId);
        List<Integer> newFacilityIds = facilityIds.stream()
                .filter(id -> !existingFacilityIds.contains(id))
                .collect(Collectors.toList());

        for (Integer facilityId : newFacilityIds) {
            RoomTypeFacility relation = new RoomTypeFacility();
            relation.setRoomTypeId(roomTypeId);
            relation.setFacilityId(facilityId);
            roomTypeFacilityRepository.save(relation);
        }
    }

    @Override
    public void removeFacilitiesFromRoomType(Integer roomTypeId, List<Integer> facilityIds) {
        if (facilityIds == null || facilityIds.isEmpty()) {
            return;
        }
        roomTypeFacilityRepository.deleteByRoomTypeIdAndFacilityIdIn(roomTypeId, facilityIds);
    }

    @Override
    public void replaceFacilitiesForRoomType(Integer roomTypeId, List<Integer> facilityIds) {
        // 先删除所有关联
        roomTypeFacilityRepository.deleteByRoomTypeId(roomTypeId);
        // 再批量添加新的关联
        if (facilityIds != null && !facilityIds.isEmpty()) {
            for (Integer facilityId : facilityIds) {
                RoomTypeFacility relation = new RoomTypeFacility();
                relation.setRoomTypeId(roomTypeId);
                relation.setFacilityId(facilityId);
                roomTypeFacilityRepository.save(relation);
            }
        }
    }

    @Override
    public void deleteByRoomTypeId(Integer roomTypeId) {
        roomTypeFacilityRepository.deleteByRoomTypeId(roomTypeId);
    }
}
