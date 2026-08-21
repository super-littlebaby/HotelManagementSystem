package com.project.hotelmanagementsystem.service.impl;

import com.project.hotelmanagementsystem.entity.Hotel;
import com.project.hotelmanagementsystem.repository.HotelRepository;
import com.project.hotelmanagementsystem.service.HotelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 酒店Service实现类
 */
@Service
@Transactional
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;

    public HotelServiceImpl(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Hotel> findById(Integer id) {
        return hotelRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Hotel> findAll() {
        return hotelRepository.findAll();
    }

    @Override
    public Hotel save(Hotel hotel) {
        return hotelRepository.save(hotel);
    }

    @Override
    public void deleteById(Integer id) {
        hotelRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Hotel> findByNameContaining(String name) {
        return hotelRepository.findByNameContaining(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Hotel> findByAddressContaining(String address) {
        return hotelRepository.findByAddressContaining(address);
    }
}