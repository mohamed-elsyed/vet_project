package com.vetclinic.controller;

import com.vetclinic.dto.ClinicDTO;
import com.vetclinic.entity.Clinic;
import com.vetclinic.service.ClinicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/clinics")
public class ClinicController {

    private final ClinicService service;

    public ClinicController(ClinicService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Clinic> create(@RequestBody ClinicDTO clinicDTO) {
        Clinic clinic = service.createClinic(clinicDTO);
        // إرجاع 201 مع Location مفيد عند التجربة في Postman
        return ResponseEntity.created(URI.create("/api/clinics/" + clinic.getId())).body(clinic);
    }

    @GetMapping
    public ResponseEntity<List<Clinic>> getAll() {
        return ResponseEntity.ok(service.getAllClinics());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Clinic> getById(@PathVariable Long id) {
        Clinic c = service.getClinicById(id);
        if (c == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(c);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Clinic> update(@PathVariable Long id, @RequestBody Clinic clinic) {
        Clinic updated = service.updateClinic(id, clinic);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteClinic(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<String> activate(@PathVariable Long id) {
        Clinic clinic = service.getClinicById(id);
        if (clinic == null) return ResponseEntity.notFound().build();
        clinic.setActive(true);
        service.updateClinic(id, clinic);
        return ResponseEntity.ok("Clinic activated successfully.");
}
}
