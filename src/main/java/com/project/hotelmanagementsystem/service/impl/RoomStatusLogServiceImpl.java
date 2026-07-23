package com.project.hotelmanagementsystem.service.impl;

import com.project.hotelmanagementsystem.entity.RoomStatusLog;
import com.project.hotelmanagementsystem.repository.RoomStatusLogRepository;
import com.project.hotelmanagementsystem.service.RoomStatusLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 房间状态变更日志Service实现类
 */
@Service
@Transactional
public class RoomStatusLogServiceImpl implements RoomStatusLogService {

    private final RoomStatusLogRepository roomStatusLogRepository;

    @Autowired
    public RoomStatusLogServiceImpl(RoomStatusLogRepository roomStatusLogRepository) {
        this.roomStatusLogRepository = roomStatusLogRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RoomStatusLog> findById(Integer id) {
        return roomStatusLogRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomStatusLog> findAll() {
        return roomStatusLogRepository.findAll();
    }

    @Override
    public RoomStatusLog save(RoomStatusLog roomStatusLog) {
        return roomStatusLogRepository.save(roomStatusLog);
    }

    @Override
    public void deleteById(Integer id) {
        roomStatusLogRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomStatusLog> findByRoomId(Integer roomId) {
        return roomStatusLogRepository.findByRoomId(roomId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomStatusLog> findByChangedBy(Integer changedBy) {
        return roomStatusLogRepository.findByChangedBy(changedBy);
    }
}