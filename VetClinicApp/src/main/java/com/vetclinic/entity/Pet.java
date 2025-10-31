package com.vetclinic.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * كيان يمثل الحيوان الأليف.
 * - ربط ManyToOne إلى User كـ owner.
 * - لا نقوم بتضمين medical records هنا الآن (ستكون علاقة منفصلة لاحقًا).
 */
@Entity
@Table(name = "pets", indexes = {
        @Index(name = "idx_pets_owner", columnList = "owner_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // اسم الحيوان
    @Column(nullable = false)
    private String name;

    // النوع (Dog, Cat, ...). مفتوح كنص لكن يمكن لاحقًا تحويله إلى enum.
    private String species;

    // السلالة
    private String breed;

    // العمر بالسنوات (اختياري)
    private Integer age;

    // تاريخ الميلاد (اختياري)
    private LocalDate birthDate;

    // ملاحظات طبية أو وصف قصير
    @Column(length = 500)
    private String notes;

    // الربط بالمالك — نحمّل owner من DB عند الإنشاء/التعديل (server-side authoritative)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
}
