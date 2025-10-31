package com.vetclinic.controller;

import com.vetclinic.dto.SubscriptionResponseDTO;
import com.vetclinic.entity.Subscription;
import com.vetclinic.service.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService service;

    public SubscriptionController(SubscriptionService service) {
        this.service = service;
    }

    // ✅ إنشاء اشتراك جديد (عيادة أو تجربة)
    @PostMapping
    public ResponseEntity<Subscription> createSubscription(@RequestBody Subscription subscription) {
        Subscription saved = service.createSubscription(subscription);
        return ResponseEntity.ok(saved);
    }

    // ✅ تحديث اشتراك موجود
    @PutMapping("/{id}")
    public ResponseEntity<Subscription> updateSubscription(
            @PathVariable Long id,
            @RequestBody Subscription subscription) {

        Subscription updated = service.updateSubscription(id, subscription);
        return ResponseEntity.ok(updated);
    }

    // ✅ عرض اشتراك عيادة معينة
    @GetMapping("/clinic/{clinicId}")
    public ResponseEntity<SubscriptionResponseDTO> getSubscriptionDetails(@PathVariable Long clinicId) {
        return ResponseEntity.ok(service.getSubscriptionDetails(clinicId));
    }
    // ✅ عرض كل الاشتراكات (لـ admin أو owner)
    @GetMapping
    public ResponseEntity<List<Subscription>> getAll() {
        return ResponseEntity.ok(service.getAllSubscriptions());
    }

    // ✅ تعطيل الاشتراكات المنتهية (ممكن تربطها بـ Scheduled)
    @PostMapping("/deactivate-expired")
    public ResponseEntity<String> deactivateExpired() {
        service.deactivateExpiredSubscriptions();
        return ResponseEntity.ok("Expired subscriptions deactivated successfully.");
}
}