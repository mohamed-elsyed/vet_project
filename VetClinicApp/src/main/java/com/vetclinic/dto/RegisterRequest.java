package com.vetclinic.dto;

import lombok.*;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    @NotBlank
    private String username;

    @NotBlank
    @Size(min = 6, message = "password must be at least 6 chars")
    private String password;

    @NotBlank
    private String fullName;

    @Email
    private String email;

    private String phone;

    // role optional: default OWNER. For security, only ADMIN should be able to create ADMIN users.
    private String role ="OWNER";
}
