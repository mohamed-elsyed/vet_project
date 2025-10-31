package com.vetclinic.service;

import com.vetclinic.dto.SubscriptionResponseDTO; import com.vetclinic.entity.Subscription;

import java.util.List;

public interface SubscriptionService {

    // إنشاء اشتراك جديد (يقوم بإبطال الاشتراكات القديمة الخاصة بنفس العيادة)
    Subscription createSubscription(Subscription subscription);

    // تحديث اشتراك موجود
    Subscription updateSubscription(Long id, Subscription subscription);

    // الحصول على تفاصيل اشتراك لعيادة معينة (DTO جاهز للعرض)
    SubscriptionResponseDTO getSubscriptionDetails(Long clinicId);

    // الحصول على الاشتراك النشط لعيادة معينة
    Subscription getSubscriptionByClinic(Long clinicId);

    // الحصول على كل الاشتراكات
    List<Subscription> getAllSubscriptions();

    // تعطيل الاشتراكات المنتهية
    void deactivateExpiredSubscriptions();

    // تعطيل كل الاشتراكات الخاصة بعيادة معينة (تُستخدم داخليًا)
    void deactivateByClinicId(Long clinicId);

}
