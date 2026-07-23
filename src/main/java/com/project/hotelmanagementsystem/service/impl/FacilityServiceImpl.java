package com.project.hotelmanagementsystem.service.impl;

import com.project.hotelmanagementsystem.entity.Facility;
import com.project.hotelmanagementsystem.repository.FacilityRepository;
import com.project.hotelmanagementsystem.service.FacilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 设施Service实现类
 */
@Service
@Transactional
public class FacilityServiceImpl implements FacilityService {

    private final FacilityRepository facilityRepository;

    @Autowired
    public FacilityServiceImpl(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Facility> findById(Integer id) {
        return facilityRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Facility> findAll() {
        return facilityRepository.findAll();
    }

    @Override
    public Facility save(Facility facility) {
        return facilityRepository.save(facility);
    }

    @Override
    public void deleteById(Integer id) {
        facilityRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Facility> findByNameContaining(String name) {
        return facilityRepository.findByNameContaining(name);
    }
}