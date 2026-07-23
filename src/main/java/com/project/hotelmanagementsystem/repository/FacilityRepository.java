package com.project.hotelmanagementsystem.repository;

import com.project.hotelmanagementsystem.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Integer> {

    List<Facility> findByNameContaining(String name);
}