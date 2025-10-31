package com.vetclinic.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicDTO {
    private String name;
    private String address;
    private String phone;
    private String email;
    private String description;
    // الأفضل أن يكون Long لتفادي تحويل من String إلى Long عند الاستقبال
    private Long ownerId;
}