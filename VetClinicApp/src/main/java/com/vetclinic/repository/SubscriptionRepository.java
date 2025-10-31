package com.vetclinic.repository;

import com.vetclinic.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    // يرجع أحدث اشتراك نشط لعيادة معينة (نستخدم clinic.id عبر Property traversal)
    Optional<Subscription> findTopByClinic_IdAndActiveTrueOrderByStartDateDesc(Long clinicId);

    // يرجع كل الاشتراكات الخاصة بعيادة معينة (نشطة أو غير نشطة)
    List<Subscription> findByClinic_Id(Long clinicId);

    // تعطيل كل الاشتراكات الخاصة بعيادة معينة
    // ملاحظة: @Modifying + @Transactional هنا لأننا ننفذ تحديث مباشر على DB (bulk update)
    @Modifying
    @Transactional
    @Query("UPDATE Subscription s SET s.active = false WHERE s.clinic.id = :clinicId")
    void deactivateByClinicId(@Param("clinicId") Long clinicId);

    // حذف الاشتراكات المنتهية (ممكن تستخدمها في scheduled job)
    @Modifying
    @Transactional
    @Query("DELETE FROM Subscription s WHERE s.endDate < CURRENT_DATE")
    void deleteExpiredSubscriptions();
}
