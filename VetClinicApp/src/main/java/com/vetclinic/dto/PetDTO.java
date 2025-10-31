package com.vetclinic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

/**
 * DTO مُستخدم كـ request body عند إنشاء / تعديل حيوان.
 * - نحتفظ بـ ownerId كـ Long (مش الـ User object).
 * - نضع validation أساسية هنا.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetDTO {

    private Long id; // موجود لو عايزين نستخدمه في عمليات داخلية

    @NotBlank(message = "اسم الحيوان مطلوب")
    private String name;

    @NotBlank(message = "النوع مطلوب")
    private String species;

    private String breed;

    // العمر ممكن يكون null لو هنعتمد birthDate
    private Integer age;

    private LocalDate birthDate;

    @Size(max = 500, message = "الملاحظات طويلة جداً")
    private String notes;

    @NotNull(message = "ownerId مطلوب")
    private Long ownerId;
}
