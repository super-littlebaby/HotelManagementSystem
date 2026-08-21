package com.project.hotelmanagementsystem.service.impl;

import com.project.hotelmanagementsystem.dto.roomstatuslog.RoomStatusLogDTO;
import com.project.hotelmanagementsystem.entity.Employee;
import com.project.hotelmanagementsystem.entity.Room;
import com.project.hotelmanagementsystem.entity.RoomStatusLog;
import com.project.hotelmanagementsystem.repository.RoomStatusLogRepository;
import com.project.hotelmanagementsystem.service.RoomStatusLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 房间状态变更日志Service实现类
 */
@Service
@Transactional
public class RoomStatusLogServiceImpl implements RoomStatusLogService {

    private final RoomStatusLogRepository roomStatusLogRepository;

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
        return roomStatusLogRepository.findAllWithRelations();
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

    @Override
    @Transactional(readOnly = true)
    public List<RoomStatusLog> findByHotelId(Integer hotelId) {
        return roomStatusLogRepository.findByHotelId(hotelId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomStatusLog> findByNewStatus(String newStatus) {
        return roomStatusLogRepository.findByNewStatus(newStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomStatusLog> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return roomStatusLogRepository.findByChangedAtBetween(startTime, endTime);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoomStatusLog> search(Integer hotelId, Integer roomId, String newStatus,
                                       Integer changedBy, LocalDateTime startTime,
                                       LocalDateTime endTime, Pageable pageable) {
        return roomStatusLogRepository.search(hotelId, roomId, newStatus, changedBy,
                startTime, endTime, pageable);
    }

    @Override
    public RoomStatusLog logStatusChange(Integer roomId, String oldStatus, String newStatus,
                                          Integer changedBy, String notes) {
        RoomStatusLog log = new RoomStatusLog();
        log.setRoomId(roomId);
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setChangedBy(changedBy);
        log.setNotes(notes);
        return roomStatusLogRepository.save(log);
    }

    @Override
    public RoomStatusLogDTO convertToDTO(RoomStatusLog log) {
        RoomStatusLogDTO dto = new RoomStatusLogDTO();
        dto.setId(log.getId());
        dto.setRoomId(log.getRoomId());
        dto.setOldStatus(log.getOldStatus());
        dto.setNewStatus(log.getNewStatus());
        dto.setChangedBy(log.getChangedBy());
        dto.setChangedAt(log.getChangedAt());
        dto.setNotes(log.getNotes());

        Room room = log.getRoom();
        if (room != null) {
            dto.setRoomNumber(room.getRoomNumber());
            dto.setHotelId(room.getHotelId());
            if (room.getHotel() != null) {
                dto.setHotelName(room.getHotel().getName());
            }
        }

        Employee employee = log.getEmployee();
        if (employee != null) {
            dto.setChangedByUsername(employee.getUsername());
            StringBuilder name = new StringBuilder();
            if (employee.getLastName() != null) {
                name.append(employee.getLastName());
            }
            if (employee.getFirstName() != null) {
                name.append(employee.getFirstName());
            }
            dto.setChangedByName(name.toString().trim());
        }
        return dto;
    }

    @Override
    public List<RoomStatusLogDTO> convertToDTOList(List<RoomStatusLog> logs) {
        return logs.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
}
