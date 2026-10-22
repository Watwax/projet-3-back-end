package com.openclassrooms.chatop.repository;

import com.openclassrooms.chatop.entity.Rental;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalRepository extends JpaRepository<Rental, Integer> {
}
