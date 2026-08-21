package com.project.hotelmanagementsystem.repository;

import com.project.hotelmanagementsystem.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FacilityRepository extends JpaRepository<Facility, Integer> {

    List<Facility> findByNameContaining(String name);
}