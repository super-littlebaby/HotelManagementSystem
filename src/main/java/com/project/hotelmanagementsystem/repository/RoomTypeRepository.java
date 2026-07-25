package com.project.hotelmanagementsystem.repository;

import com.project.hotelmanagementsystem.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, Integer> {

    @Query("SELECT rt FROM RoomType rt JOIN FETCH rt.hotel ORDER BY rt.id")
    List<RoomType> findAllWithHotel();

    @Query("SELECT rt FROM RoomType rt JOIN FETCH rt.hotel WHERE rt.id = :id")
    Optional<RoomType> findByIdWithHotel(Integer id);

    @Query("SELECT rt FROM RoomType rt JOIN FETCH rt.hotel WHERE rt.hotelId = :hotelId")
    List<RoomType> findByHotelId(Integer hotelId);

    @Query("SELECT rt FROM RoomType rt JOIN FETCH rt.hotel WHERE rt.hotelId = :hotelId AND rt.bedType = :bedType")
    List<RoomType> findByHotelIdAndBedType(Integer hotelId, String bedType);
}