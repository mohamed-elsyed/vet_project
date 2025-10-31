package com.vetclinic.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * كيان Clinic:
 * - نتعامل مع lazy loading بعقلانية (fetch = LAZY) عشان ما نحملش بيانات غير لازمة.
 * - @JsonIgnoreProperties يتجاهل الـ Hibernate proxy attributes عند السيريالايز.
 * - @JsonIdentityInfo يمنع infinite recursion بين الكيانات المتبادلة عن طريق استخدام الـ id كمرجع.
 */
@Entity
@Table(name = "clinics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Clinic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;
    private String phone;
    private String email;

    @Column(length = 200)
    private String description;

    // حالة العيادة — نستخدم Boolean بدل primitive عشان null-safety عند الـ ORM
    private Boolean active = true;

    // ربط بالمالك — fetchLazy لتحكم في التحميل من الخدمة (service)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // تاريخ اشتراكات العيادة (history). orphanRemoval = true لتنظيف الاشتراكات لو اتشالت من القائمة
    @OneToMany(mappedBy = "clinic", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Subscription> subscriptions = new ArrayList<>();

    /**
     * مساعدة لإضافة اشتراك مُزامنًا (keeps both sides in sync)
     * نستعملها داخل service داخل transaction
     */
    public void addSubscription(Subscription s) {
        subscriptions.add(s);
        s.setClinic(this);
    }

    public void removeSubscription(Subscription s) {

    }

    public void setSubscription(Subscription saved) {
    }
}
