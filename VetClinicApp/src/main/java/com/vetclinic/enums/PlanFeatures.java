package com.vetclinic.enums;

import java.util.HashMap;
import java.util.Map;

public enum PlanFeatures {
    BASIC(createBasicFeatures()),
    PREMIUM(createPremiumFeatures()),
    TRIAL(createTrialFeatures());

    private final Map<String, Object> features;

    PlanFeatures(Map<String, Object> features) {
        this.features = features;
    }

    public Object getFeature(String key) {
        return features.get(key);
    }

    public Map<String, Object> getAllFeatures() {
        return features;
    }

    // ✅ إعداد خصائص خطة BASIC
    private static Map<String, Object> createBasicFeatures() {
        Map<String, Object> map = new HashMap<>();
        map.put("maxDoctors", 3);
        map.put("maxPatients", 300);
        map.put("storageLimitMB", 500);
        map.put("allowReports", true);
        map.put("pricePerMonth", 200.0);
        return map;
    }

    // ✅ إعداد خصائص خطة PREMIUM
    private static Map<String, Object> createPremiumFeatures() {
        Map<String, Object> map = new HashMap<>();
        map.put("maxDoctors", 10);
        map.put("maxPatients", 2000);
        map.put("storageLimitMB", 5000);
        map.put("allowReports", true);
        map.put("allowMultiBranch", true);
        map.put("prioritySupport", true);
        map.put("pricePerMonth", 500.0);
        return map;
    }

    // ✅ إعداد خصائص خطة TRIAL (التجريبية)
    private static Map<String, Object> createTrialFeatures() {
        Map<String, Object> map = new HashMap<>();
        map.put("maxDoctors", 1);
        map.put("maxPatients", 50);
        map.put("storageLimitMB", 100);
        map.put("allowReports", false);
        map.put("pricePerMonth", 0.0);
        map.put("trialPeriodDays", 14);
        return map;
}
}