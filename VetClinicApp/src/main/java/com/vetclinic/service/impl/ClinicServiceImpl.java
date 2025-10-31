package com.vetclinic.service.impl;

import com.vetclinic.dto.ClinicDTO;
import com.vetclinic.entity.Clinic;
import com.vetclinic.entity.Subscription;
import com.vetclinic.entity.User;
import com.vetclinic.repository.ClinicRepository;
import com.vetclinic.repository.SubscriptionRepository;
import com.vetclinic.repository.UserRepository;
import com.vetclinic.service.ClinicService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ClinicServiceImpl implements ClinicService {

    private final ClinicRepository clinicRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    public ClinicServiceImpl(ClinicRepository clinicRepository,
                             UserRepository userRepository,
                             SubscriptionRepository subscriptionRepository) {
        this.clinicRepository = clinicRepository;
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    /**
     * ننشئ العيادة أولاً ثم ننشئ اشتراك ابتدائي مرتبط بالعيادة المحفوظة.
     * نستخدم @Transactional لضمان الاتساق (اذا فشل شيء يتم التراجع).
     *
     * ملاحظات تصميم:
     * - نتحقق من ownerId (fail-fast).
     * - نحفظ الـ clinic أولاً للحصول على managed entity مع id (مهم ربط الاشتراك).
     * - نستخدم helper methods لإدارة ثنائية العلاقة.
     */
    @Override
    @Transactional
    public Clinic createClinic(ClinicDTO dto) {
        if (dto.getOwnerId() == null) {
            throw new IllegalArgumentException("ownerId required in DTO");
        }

        User owner = userRepository.findById(dto.getOwnerId())
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        // Build clinic and persist to obtain id & managed entity
        Clinic clinic = new Clinic();
        clinic.setName(dto.getName());
        clinic.setAddress(dto.getAddress());
        clinic.setPhone(dto.getPhone());
        clinic.setEmail(dto.getEmail());
        clinic.setDescription(dto.getDescription());
        clinic.setOwner(owner);
        clinic.setActive(true);

        Clinic savedClinic = clinicRepository.save(clinic); // now managed, has id

        // Create initial subscription and attach to clinic (use helper to keep both sides)
        Subscription subscription = Subscription.builder()
                .planType("BASIC")
                .active(true)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .price(0.0)
                .build();

        // use helper to set both sides
        savedClinic.addSubscription(subscription);

        // saving the subscription explicitly (optional because Cascade.ALL on clinic -> but explicit is clear)
        subscriptionRepository.save(subscription);

        // save clinic to ensure the collection is persisted (optional with cascade, but safe)
        clinicRepository.save(savedClinic);

        return savedClinic;
    }

    @Override
    public List<Clinic> getAllClinics() {
        return clinicRepository.findAll();
    }

    @Override
    public Clinic getClinicById(Long id) {
        return clinicRepository.findById(id).orElse(null);
    }

    @Override
    public Clinic updateClinic(Long id, Clinic clinic) {
        Clinic existing = getClinicById(id);
        if (existing == null) return null;

        existing.setName(clinic.getName());
        existing.setAddress(clinic.getAddress());
        existing.setPhone(clinic.getPhone());
        existing.setEmail(clinic.getEmail());
        existing.setDescription(clinic.getDescription());
        return clinicRepository.save(existing);
    }

    @Override
    public void deleteClinic(Long id) {
        Clinic clinic = getClinicById(id);
        if (clinic != null) {
            clinic.setActive(false); // تعطيل بدل الحذف
            clinicRepository.save(clinic);
 }
}
}
