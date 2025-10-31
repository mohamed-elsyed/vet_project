package com.vetclinic.service;

import com.vetclinic.dto.ClinicDTO;
import com.vetclinic.entity.Clinic;
import java.util.List;

public interface ClinicService {
    Clinic createClinic(ClinicDTO dto);
    List<Clinic> getAllClinics();
    Clinic getClinicById(Long id);
    Clinic updateClinic(Long id, Clinic clinic);
    void deleteClinic(Long id);
}