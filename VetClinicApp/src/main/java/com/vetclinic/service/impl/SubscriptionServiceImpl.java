package com.vetclinic.service.impl;

import com.vetclinic.dto.SubscriptionResponseDTO;
import com.vetclinic.entity.Clinic;
import com.vetclinic.entity.Subscription;
import com.vetclinic.enums.PlanFeatures;
import com.vetclinic.repository.ClinicRepository;
import com.vetclinic.repository.SubscriptionRepository;
import com.vetclinic.service.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionServiceImpl.class);

    private final SubscriptionRepository subscriptionRepository;
    private final ClinicRepository clinicRepository;

    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository,
                                   ClinicRepository clinicRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.clinicRepository = clinicRepository;
    }

    /**
     * إنشاء اشتراك جديد لعيادة.
     *
     * قواعد التصميم المطبقة:
     *  - نطلب clinic.id صريح (لا نثق بكائن Clinic مفصّل من العميل).
     *  - نحمل الكيـان Clinic من DB للحصول على Managed Entity (حتى يعمل الـ FK صحيح).
     *  - نعطّل كل الاشتراكات النشطة القديمة بنفس المعاملة قبل حفظ الجديد لضمان وجود اشتراك نشط واحد فقط.
     *  - نستخدم ResponseStatusException لردود HTTP واضحة (BAD_REQUEST, NOT_FOUND).
     */
    @Override
    @Transactional
    public Subscription createSubscription(Subscription subscription) {
        if (subscription == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subscription payload cannot be null");
        }

        if (subscription.getClinic() == null || subscription.getClinic().getId() == null) {
            // خطأ واضح للعميل: يجب تزويد clinic.id
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clinic.id is required when creating a subscription");
        }

        Long clinicId = subscription.getClinic().getId();

        // جلب العيادة من DB (server-side authoritative)
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinic not found with id: " + clinicId));

        // تعطيل الاشتراكات القديمة للعيادة داخل نفس المعاملة
        subscriptionRepository.deactivateByClinicId(clinicId);

        // إعداد الاشتراك الجديد
        subscription.setClinic(clinic);            // نضع الكيان المُدار لضمان صحة العلاقة
        subscription.setStartDate(LocalDate.now()); // تاريخ بداية الآن

        if ("TRIAL".equalsIgnoreCase(subscription.getPlanType())) {
            // سياسة: تجربة لمدة 14 يوم
            subscription.setEndDate(LocalDate.now().plusDays(14));
            subscription.setPrice(0.0);
        }

        subscription.setActive(true);

        // حفظ الاشتراك
        Subscription saved = subscriptionRepository.save(subscription);

        // مزامنة جهة Clinic (مفيد عندما تعيد الـ Clinic في نفس الـ TX)
        clinic.setSubscription(saved);
        clinicRepository.save(clinic);

        log.info("Created subscription id={} for clinicId={}", saved.getId(), clinicId);
        return saved;
    }

    /**
     * تحديث اشتراك موجود — نحدّث الحقول الآمنة فقط.
     */
    @Override
    @Transactional
    public Subscription updateSubscription(Long id, Subscription updated) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found: " + id));

        // تحديث حذر: لا نغير الـ clinic هنا (حتى لا نكسر العلاقات بدون تحقق)
        sub.setPlanType(updated.getPlanType());
        sub.setPrice(updated.getPrice());
        sub.setDiscount(updated.getDiscount());
        sub.setEndDate(updated.getEndDate());
        sub.setActive(updated.isActive());

        Subscription saved = subscriptionRepository.save(sub);
        log.info("Updated subscription id={}", saved.getId());
        return saved;
    }

    /**
     * إرجاع تفاصيل الاشتراك الحالي لعيادة (DTO يحتوي الخصائص والمميزات)
     */
    @Override
    public SubscriptionResponseDTO getSubscriptionDetails(Long clinicId) {
        Subscription sub = subscriptionRepository.findTopByClinic_IdAndActiveTrueOrderByStartDateDesc(clinicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No active subscription found for clinic ID " + clinicId));

        PlanFeatures plan;
        try {
            plan = PlanFeatures.valueOf(sub.getPlanType().toUpperCase());
        } catch (Exception e) {
            // لو نوع الخطة غير معروف، نرجع قيمة افتراضية بدل الكسر
            plan = PlanFeatures.BASIC;
        }

        return new SubscriptionResponseDTO(
                clinicId,
                sub.getPlanType(),
                sub.getPrice(),
                sub.getStartDate(),
                sub.getEndDate(),
                sub.isActive(),
                plan.getAllFeatures()
        );
    }

    /**
     * إرجاع الكيان Subscription الحالي لعيادة (للاستخدام الداخلي)
     */
    @Override
    public Subscription getSubscriptionByClinic(Long clinicId) {
        return subscriptionRepository.findTopByClinic_IdAndActiveTrueOrderByStartDateDesc(clinicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No active subscription found for clinic ID " + clinicId));
    }

    @Override
    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }

    /**
     * تعطيل الاشتراكات المنتهية (ممكن تربطه بـ @Scheduled لاحقًا)
     */
    @Override
    @Transactional
    public void deactivateExpiredSubscriptions() {
        List<Subscription> subs = subscriptionRepository.findAll();
        LocalDate today = LocalDate.now();

        subs.forEach(sub -> {
            if (sub.getEndDate() != null && sub.getEndDate().isBefore(today) && sub.isActive()) {
                sub.setActive(false);
                subscriptionRepository.save(sub);
                log.info("Deactivated expired subscription id={}", sub.getId());
            }
        });
    }

    /**
     * تعطيل جميع الاشتراكات لعيادة (غلاف على طريقة الـ Repository)
     */
    @Override
    @Transactional
    public void deactivateByClinicId(Long clinicId) {
        subscriptionRepository.deactivateByClinicId(clinicId);
}
}
