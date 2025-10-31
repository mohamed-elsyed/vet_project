package com.vetclinic.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * كيان User:
 * - نخفي الـ hibernate proxies و نمنع loops باستخدام JsonIdentityInfo.
 * - نحمي الـ password من الرجوع في الـ responses باستخدام WRITE_ONLY.
 * - نخزن العلاقة العكسية (clinics) كـ lazy لأننا لا نحتاج دائمًا استرجاع كل العيادات مع المستخدم.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // اسم المستخدم فريد
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // لا يظهر في الـ responses أبداً
    private String password;

    private String fullName;

    private String email;

    private String phone;

    @Column(nullable = false)
    private String role; // مثلا: OWNER, ADMIN

    private Boolean active = true;

    // لو عايزين نجيب كل العيادات بتاعت اليوزر
    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = false)
    @Builder.Default
    private List<Clinic> clinics = new ArrayList<>();
}
