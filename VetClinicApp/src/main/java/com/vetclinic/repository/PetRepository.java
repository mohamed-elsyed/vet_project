package com.vetclinic.repository;

import com.vetclinic.entity.Pet;
import com.vetclinic.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {

    // استرجاع كل الحيوانات لمالك معين
    List<Pet> findByOwnerId(Long ownerId);

}