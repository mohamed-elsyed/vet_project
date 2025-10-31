package com.vetclinic.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.Map;

@Getter
@AllArgsConstructor
@ToString
public class SubscriptionResponseDTO {

    private final Long clinicId;
    private final String planType;
    private final Double price;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Boolean active;
    private final Map<String, Object> features;

}