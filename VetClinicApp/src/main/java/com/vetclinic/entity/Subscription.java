package com.vetclinic.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // نوع الباقة: BASIC, PREMIUM, TRIAL
    @Column(nullable = false)
    private String planType;

    // تاريخ بداية الاشتراك
    private LocalDate startDate;

    // تاريخ نهاية الاشتراك
    private LocalDate endDate;

    // السعر الفعلي بعد الخصم
    private Double price;

    // هل الاشتراك نشط الآن؟
    private boolean active;

    // نسبة الخصم لو فيه خصم خاص
    private Double discount;

    // الربط مع العيادة — هنا ManyToOne لأن كل عيادة لها history من الاشتراكات
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id", nullable = false) // لا تُجعل unique!
    private Clinic clinic;
}
