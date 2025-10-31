package com.vetclinic.util;

import com.vetclinic.entity.Clinic;
import com.vetclinic.entity.Subscription;
import com.vetclinic.enums.PlanFeatures;
import com.vetclinic.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Validator بسيط للفحص إذا كانت العيادة مسموح لها تضيف (Doctor / Patient)
 * التغييرات الأساسية:
 *  - استعملت اسم الـ repository الصحيح: findTopByClinic_IdAndActiveTrueOrderByStartDateDesc
 *  - تحقُّق من null لِـ clinic و clinic.id و planType
 *  - تحويل آمن لقيمة الميزة (قد تكون Number أو String)
 *  - تعليقات لشرح المنطق بالعربي
 */
@Component
public class SubscriptionValidator {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionValidator.class);

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionValidator(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    /**
     * يحدد إذا ممكن إضافة دكتور جديد للعيادة بحسب الحد المسموح به في الخطة الحالية.
     * @param clinic كائن العيادة (يجب أن يحتوي على id)
     * @param currentDoctorCount عدد الدكاترة الحاليين
     * @return true لو العدد أقل من الحد المسموح، false خلاف ذلك أو لو لا يوجد اشتراك نشط
     */
    public Boolean canAddDoctor(Clinic clinic, int currentDoctorCount) {
        if (clinic == null || clinic.getId() == null) {
            log.debug("canAddDoctor: clinic or clinic.id is null");
            return false;
        }

        Optional<Subscription> subOpt = subscriptionRepository
                .findTopByClinic_IdAndActiveTrueOrderByStartDateDesc(clinic.getId());

        if (subOpt.isEmpty()) {
            log.debug("canAddDoctor: no active subscription for clinicId={}", clinic.getId());
            return false;
        }

        Subscription sub = subOpt.get();
        String planType = sub.getPlanType();
        if (planType == null) {
            log.debug("canAddDoctor: subscription.planType is null for subscription id={}", sub.getId());
            return false;
        }

        PlanFeatures plan;
        try {
            plan = PlanFeatures.valueOf(planType.toUpperCase());
        } catch (Exception e) {
            log.warn("canAddDoctor: unknown planType='{}' for subscription id={}, falling back to BASIC", planType, sub.getId());
            plan = PlanFeatures.BASIC;
        }

        Object featureObj = plan.getFeature("maxDoctors");
        int maxDoctors = safeToInt(featureObj, 0);

        return currentDoctorCount < maxDoctors;
    }

    /**
     * يحدد إذا ممكن إضافة مريض جديد للعيادة بحسب حد الخطة الحالية.
     * @param clinic كائن العيادة (يجب أن يحتوي على id)
     * @param currentPatientCount عدد المرضى الحاليين
     * @return true لو العدد أقل من الحد المسموح، false خلاف ذلك أو لو لا يوجد اشتراك نشط
     */
    public Boolean canAddPatient(Clinic clinic, int currentPatientCount) {
        if (clinic == null || clinic.getId() == null) {
            log.debug("canAddPatient: clinic or clinic.id is null");
            return false;
        }

        Optional<Subscription> subOpt = subscriptionRepository
                .findTopByClinic_IdAndActiveTrueOrderByStartDateDesc(clinic.getId());

        if (subOpt.isEmpty()) {
            log.debug("canAddPatient: no active subscription for clinicId={}", clinic.getId());
            return false;
        }

        Subscription sub = subOpt.get();
        String planType = sub.getPlanType();
        if (planType == null) {
            log.debug("canAddPatient: subscription.planType is null for subscription id={}", sub.getId());
            return false;
        }

        PlanFeatures plan;
        try {
            plan = PlanFeatures.valueOf(planType.toUpperCase());
        } catch (Exception e) {
            log.warn("canAddPatient: unknown planType='{}' for subscription id={}, falling back to BASIC", planType, sub.getId());
            plan = PlanFeatures.BASIC;
        }

        Object featureObj = plan.getFeature("maxPatients");
        int maxPatients = safeToInt(featureObj, 0);

        return currentPatientCount < maxPatients;
    }

    /**
     * تحويل آمن لـ Object -> int مع fallback
     */
    private int safeToInt(Object val, int fallback) {
        if (val == null) return fallback;
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        try {
            return Integer.parseInt(val.toString());
        } catch (Exception e) {
            log.warn("safeToInt: cannot convert feature value '{}' to int, using fallback={}", val, fallback);
            return fallback;
        }
}
}
