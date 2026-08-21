package com.project.hotelmanagementsystem.service.impl;

import com.project.hotelmanagementsystem.entity.RoomType;
import com.project.hotelmanagementsystem.repository.RoomTypeRepository;
import com.project.hotelmanagementsystem.service.RoomTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;

    public RoomTypeServiceImpl(RoomTypeRepository roomTypeRepository) {
        this.roomTypeRepository = roomTypeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RoomType> findById(Integer id) {
        return roomTypeRepository.findByIdWithHotel(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomType> findAll() {
        return roomTypeRepository.findAllWithHotel();
    }

    @Override
    public RoomType save(RoomType roomType) {
        RoomType saved = roomTypeRepository.save(roomType);
        return roomTypeRepository.findByIdWithHotel(saved.getId()).orElse(saved);
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

    public Map<String, Object> convertToDTO(RoomType roomType) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", roomType.getId());
        dto.put("hotelId", roomType.getHotelId());
        dto.put("hotelName", roomType.getHotel() != null ? roomType.getHotel().getName() : "");
        dto.put("name", roomType.getName());
        dto.put("description", roomType.getDescription());
        dto.put("maxAdults", roomType.getMaxAdults());
        dto.put("maxChildren", roomType.getMaxChildren());
        dto.put("basePrice", roomType.getBasePrice());
        dto.put("area", roomType.getArea());
        dto.put("bedType", roomType.getBedType());
        return dto;
    }

    public List<Map<String, Object>> convertToDTOList(List<RoomType> roomTypes) {
        return roomTypes.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
}