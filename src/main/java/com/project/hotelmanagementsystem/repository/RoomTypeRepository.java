package com.project.hotelmanagementsystem.repository;

import com.project.hotelmanagementsystem.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, Integer> {

    List<RoomType> findByHotelId(Integer hotelId);

    List<RoomType> findByHotelIdAndBedType(Integer hotelId, String bedType);
}