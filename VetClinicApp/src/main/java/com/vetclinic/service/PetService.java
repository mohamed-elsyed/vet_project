package com.vetclinic.service;

import com.vetclinic.dto.PetDTO;
import com.vetclinic.dto.PetResponseDTO;

import java.util.List;

public interface PetService {
    PetResponseDTO createPet(PetDTO dto);
    PetResponseDTO updatePet(Long id, PetDTO dto);
    PetResponseDTO getPetById(Long id);
    List<PetResponseDTO> getPetsByOwner(Long ownerId);
    void deletePet(Long id);
    List<PetResponseDTO> getAllPets();
    List<PetResponseDTO> getPetsByOwnerUsername(String username);
}