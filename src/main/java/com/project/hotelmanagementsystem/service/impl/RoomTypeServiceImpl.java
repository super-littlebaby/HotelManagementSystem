package com.project.hotelmanagementsystem.service.impl;

import com.project.hotelmanagementsystem.entity.RoomType;
import com.project.hotelmanagementsystem.repository.RoomTypeRepository;
import com.project.hotelmanagementsystem.service.RoomTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 房型Service实现类
 */
@Service
@Transactional
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;

    @Autowired
    public RoomTypeServiceImpl(RoomTypeRepository roomTypeRepository) {
        this.roomTypeRepository = roomTypeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RoomType> findById(Integer id) {
        return roomTypeRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomType> findAll() {
        return roomTypeRepository.findAll();
    }

    @Override
    public RoomType save(RoomType roomType) {
        return roomTypeRepository.save(roomType);
    }

    @Override
    public void deleteById(Integer id) {
        roomTypeRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomType> findByHotelId(Integer hotelId) {
        return roomTypeRepository.findByHotelId(hotelId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomType> findByHotelIdAndBedType(Integer hotelId, String bedType) {
        return roomTypeRepository.findByHotelIdAndBedType(hotelId, bedType);
    }
}