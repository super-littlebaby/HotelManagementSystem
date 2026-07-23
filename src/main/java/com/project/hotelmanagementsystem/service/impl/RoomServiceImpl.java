package com.project.hotelmanagementsystem.service.impl;

import com.project.hotelmanagementsystem.entity.Room;
import com.project.hotelmanagementsystem.repository.RoomRepository;
import com.project.hotelmanagementsystem.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 房间Service实现类
 */
@Service
@Transactional
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    @Autowired
    public RoomServiceImpl(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Room> findById(Integer id) {
        return roomRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    @Override
    public Room save(Room room) {
        return roomRepository.save(room);
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
}