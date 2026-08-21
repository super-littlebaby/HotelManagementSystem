package com.project.hotelmanagementsystem.service.impl;

import com.project.hotelmanagementsystem.entity.Payment;
import com.project.hotelmanagementsystem.repository.PaymentRepository;
import com.project.hotelmanagementsystem.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 收款记录Service实现类
 */
@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findById(Integer id) {
        return paymentRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }

    @Override
    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Override
    public void deleteById(Integer id) {
        paymentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> findByBillId(Integer billId) {
        return paymentRepository.findByBillId(billId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> findByPaymentMethod(String paymentMethod) {
        return paymentRepository.findByPaymentMethod(paymentMethod);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> findByPaymentType(String paymentType) {
        return paymentRepository.findByPaymentType(paymentType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> findByBillIdAndPaymentType(Integer billId, String paymentType) {
        return paymentRepository.findByBillIdAndPaymentType(billId, paymentType);
    }
}