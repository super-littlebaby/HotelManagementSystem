package com.project.hotelmanagementsystem.service.impl;

import com.project.hotelmanagementsystem.entity.ConsumableItem;
import com.project.hotelmanagementsystem.repository.ConsumableItemRepository;
import com.project.hotelmanagementsystem.service.ConsumableItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 可消费项目Service实现类
 */
@Service
@Transactional
public class ConsumableItemServiceImpl implements ConsumableItemService {

    private final ConsumableItemRepository consumableItemRepository;

    public ConsumableItemServiceImpl(ConsumableItemRepository consumableItemRepository) {
        this.consumableItemRepository = consumableItemRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConsumableItem> findById(Integer id) {
        return consumableItemRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsumableItem> findAll() {
        return consumableItemRepository.findAll();
    }

    @Override
    public ConsumableItem save(ConsumableItem consumableItem) {
        return consumableItemRepository.save(consumableItem);
    }

    @Override
    public void deleteById(Integer id) {
        consumableItemRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsumableItem> findByHotelId(Integer hotelId) {
        return consumableItemRepository.findByHotelId(hotelId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsumableItem> findByCategory(String category) {
        return consumableItemRepository.findByCategory(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsumableItem> findByIsActive(Boolean isActive) {
        return consumableItemRepository.findByIsActive(isActive);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsumableItem> findByHotelIdAndCategory(Integer hotelId, String category) {
        return consumableItemRepository.findByHotelIdAndCategory(hotelId, category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsumableItem> findByHotelIdAndIsActive(Integer hotelId, Boolean isActive) {
        return consumableItemRepository.findByHotelIdAndIsActive(hotelId, isActive);
    }
}