package com.vetclinic.dto;

import lombok.*;

import java.time.LocalDate;

/**
 * DTO للـ response: فقط الحقول الآمنة والمفيدة للـ frontend.
 * مفيش password ولا بيانات حساسة.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetResponseDTO {
    private Long id;
    private String name;
    private String species;
    private String breed;
    private Integer age;
    private LocalDate birthDate;
    private String notes;

    // معلومات المالك الأساسية (نرجع id و username فقط — مش كامل User entity)
    private Long ownerId;
    private String ownerUsername;
}
