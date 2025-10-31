package com.vetclinic.util;

import com.vetclinic.dto.PetDTO;
import com.vetclinic.dto.PetResponseDTO;
import com.vetclinic.entity.Pet;
import com.vetclinic.entity.User;

/**
 * Mapper بسيط (يدوي) لتحويلات Pet <-> DTO.
 * لاحقًا لو عايز، ممكن تستبدله بـ MapStruct لعمل مابنج اتوماتيكي.
 */
public final class PetMapper {

    private PetMapper() {}

    // من DTO -> Entity (نحتاج تمرّر User المُدار خارجيًا)
    public static Pet toEntity(PetDTO dto, User owner) {
        if (dto == null) return null;
        return Pet.builder()
                .id(dto.getId())
                .name(dto.getName())
                .species(dto.getSpecies())
                .breed(dto.getBreed())
                .age(dto.getAge())
                .birthDate(dto.getBirthDate())
                .notes(dto.getNotes())
                .owner(owner) // استخدم User مُدار من DB
                .build();
    }

    // من Entity -> Response DTO (نمنع تسريب بيانات User كاملة)
    public static PetResponseDTO toResponse(Pet pet) {
        if (pet == null) return null;
        User owner = pet.getOwner();
        return PetResponseDTO.builder()
                .id(pet.getId())
                .name(pet.getName())
                .species(pet.getSpecies())
                .breed(pet.getBreed())
                .age(pet.getAge())
                .birthDate(pet.getBirthDate())
                .notes(pet.getNotes())
                .ownerId(owner != null ? owner.getId() : null)
                .ownerUsername(owner != null ? owner.getUsername() : null)
                .build();
}
}
