package com.project.hotelmanagementsystem.service.impl;

import com.project.hotelmanagementsystem.entity.Refund;
import com.project.hotelmanagementsystem.repository.RefundRepository;
import com.project.hotelmanagementsystem.service.RefundService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 退款记录Service实现类
 */
@Service
@Transactional
public class RefundServiceImpl implements RefundService {

    private final RefundRepository refundRepository;

    public RefundServiceImpl(RefundRepository refundRepository) {
        this.refundRepository = refundRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Refund> findById(Integer id) {
        return refundRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Refund> findAll() {
        return refundRepository.findAll();
    }

    @Override
    public Refund save(Refund refund) {
        return refundRepository.save(refund);
    }

    @Override
    public void deleteById(Integer id) {
        refundRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Refund> findByBillId(Integer billId) {
        return refundRepository.findByBillId(billId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Refund> findByRefundMethod(String refundMethod) {
        return refundRepository.findByRefundMethod(refundMethod);
    }
}