package com.vetclinic.service.impl;

import com.vetclinic.dto.PetDTO;
import com.vetclinic.dto.PetResponseDTO;
import com.vetclinic.entity.Pet;
import com.vetclinic.entity.User;
import com.vetclinic.repository.PetRepository;
import com.vetclinic.repository.UserRepository;
import com.vetclinic.service.PetService;
import com.vetclinic.util.PetMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation:
 * - لا نثق في owner المرسل من العميل — نحمل User من DB ونربطه.
 * - كل العمليات المتغيرة داخل @Transactional.
 * - يعيد PetResponseDTO لطبقة الـ Controller.
 */
@Service
public class PetServiceImpl implements PetService {

    private final PetRepository petRepository;
    private final UserRepository userRepository;

    public PetServiceImpl(PetRepository petRepository, UserRepository userRepository) {
        this.petRepository = petRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public PetResponseDTO createPet(PetDTO dto) {
        User owner = userRepository.findById(dto.getOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("Owner not found: " + dto.getOwnerId()));

        Pet pet = PetMapper.toEntity(dto, owner);
        Pet saved = petRepository.save(pet);
        return PetMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PetResponseDTO updatePet(Long id, PetDTO dto) {
        Pet existing = petRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pet not found: " + id));

        // لو تم تمرير ownerId مختلف — نحمّل المالك الجديد
        if (dto.getOwnerId() != null && !dto.getOwnerId().equals(existing.getOwner().getId())) {
            User newOwner = userRepository.findById(dto.getOwnerId())
                    .orElseThrow(() -> new IllegalArgumentException("Owner not found: " + dto.getOwnerId()));
            existing.setOwner(newOwner);
        }

        // تحديث باقي الحقول
        existing.setName(dto.getName());
        existing.setSpecies(dto.getSpecies());
        existing.setBreed(dto.getBreed());
        existing.setAge(dto.getAge());
        existing.setBirthDate(dto.getBirthDate());
        existing.setNotes(dto.getNotes());

        Pet saved = petRepository.save(existing);
        return PetMapper.toResponse(saved);
    }

    @Override
    public PetResponseDTO getPetById(Long id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pet not found: " + id));
        return PetMapper.toResponse(pet);
    }

    @Override
    public List<PetResponseDTO> getPetsByOwner(Long ownerId) {
        List<Pet> list = petRepository.findByOwnerId(ownerId);
        return list.stream().map(PetMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deletePet(Long id) {
        petRepository.deleteById(id);
    }

    @Override
    public List<PetResponseDTO> getAllPets() {
        return petRepository.findAll().stream().map(PetMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<PetResponseDTO> getPetsByOwnerUsername(String username) {
        // يفترض وجود method في UserRepository لإيجاد user by username،
        // أو يمكنك تحميل user ثم استخراج id واستخدام findByOwnerId.
        // هنا افترضت وجود userRepository.findByUsername
        throw new UnsupportedOperationException("Implement if needed");
}
}
