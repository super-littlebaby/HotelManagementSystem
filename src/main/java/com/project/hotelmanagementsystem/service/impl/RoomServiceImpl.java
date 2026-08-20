package com.project.hotelmanagementsystem.service.impl;

import com.project.hotelmanagementsystem.entity.Room;
import com.project.hotelmanagementsystem.entity.RoomType;
import com.project.hotelmanagementsystem.repository.RoomRepository;
import com.project.hotelmanagementsystem.repository.RoomTypeRepository;
import com.project.hotelmanagementsystem.service.RoomService;
import com.project.hotelmanagementsystem.service.RoomStatusLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomStatusLogService roomStatusLogService;

    @Autowired
    public RoomServiceImpl(RoomRepository roomRepository,
                           RoomTypeRepository roomTypeRepository,
                           RoomStatusLogService roomStatusLogService) {
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.roomStatusLogService = roomStatusLogService;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Room> findById(Integer id) {
        return roomRepository.findByIdWithRelations(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Room> findAll() {
        return roomRepository.findAllWithRelations();
    }

    @Override
    public Room save(Room room) {
        if (room.getHotelId() == null) {
            throw new IllegalArgumentException("酒店ID不能为空");
        }
        if (room.getRoomTypeId() == null) {
            throw new IllegalArgumentException("房型ID不能为空");
        }
        
        Optional<RoomType> roomTypeOpt = roomTypeRepository.findById(room.getRoomTypeId());
        if (roomTypeOpt.isEmpty()) {
            throw new IllegalArgumentException("房型不存在");
        }
        
        RoomType roomType = roomTypeOpt.get();
        if (!room.getHotelId().equals(roomType.getHotelId())) {
            throw new IllegalArgumentException("房间所属酒店与房型所属酒店不一致");
        }
        
        Room saved = roomRepository.save(room);
        return roomRepository.findByIdWithRelations(saved.getId()).orElse(saved);
    }

    @Override
    public void deleteById(Integer id) {
        roomRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Room> findByRoomTypeId(Integer roomTypeId) {
        return roomRepository.findByRoomTypeId(roomTypeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Room> findByStatus(String status) {
        return roomRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Room> findByRoomNumber(String roomNumber) {
        return roomRepository.findByRoomNumber(roomNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Room> findByRoomTypeIdAndStatus(Integer roomTypeId, String status) {
        return roomRepository.findByRoomTypeIdAndStatus(roomTypeId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Room> findByHotelId(Integer hotelId) {
        return roomRepository.findByHotelId(hotelId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Room> findByHotelIdAndStatus(Integer hotelId, String status) {
        return roomRepository.findByHotelIdAndStatus(hotelId, status);
    }

    @Override
    public Room updateStatus(Integer id, String newStatus, Integer changedBy, String notes) {
        return roomRepository.findByIdWithRelations(id)
                .map(room -> {
                    String oldStatus = room.getStatus();
                    room.setStatus(newStatus);
                    Room saved = roomRepository.save(room);

                    // 通过统一日志入口写入，保证所有状态变更都走同一通道
                    roomStatusLogService.logStatusChange(id, oldStatus, newStatus, changedBy, notes);

                    return roomRepository.findByIdWithRelations(id).orElse(saved);
                })
                .orElseThrow(() -> new RuntimeException("房间不存在"));
    }

    @Override
    public Map<String, Object> convertToDTO(Room room) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", room.getId());
        dto.put("hotelId", room.getHotelId());
        dto.put("hotelName", room.getHotel() != null ? room.getHotel().getName() : "");
        dto.put("roomTypeId", room.getRoomTypeId());
        dto.put("roomTypeName", room.getRoomType() != null ? room.getRoomType().getName() : "");
        dto.put("roomNumber", room.getRoomNumber());
        dto.put("floor", room.getFloor());
        dto.put("status", room.getStatus());
        dto.put("notes", room.getNotes());
        return dto;
    }

    @Override
    public List<Map<String, Object>> convertToDTOList(List<Room> rooms) {
        return rooms.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
}