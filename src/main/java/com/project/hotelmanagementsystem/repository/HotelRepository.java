package com.project.hotelmanagementsystem.repository;

import com.project.hotelmanagementsystem.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Integer> {

    List<Hotel> findByNameContaining(String name);

    List<Hotel> findByAddressContaining(String address);
}