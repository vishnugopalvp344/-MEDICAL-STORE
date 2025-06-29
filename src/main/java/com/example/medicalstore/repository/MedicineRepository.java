package com.example.medicalstore.repository;

import com.example.medicalstore.models.Medicine;
import com.example.medicalstore.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    Page<Medicine> findByUser(User user, Pageable pageable);

    Page<Medicine> findByUserAndNameContainingIgnoreCase(User user, String keyword, Pageable pageable);

    long countByUser(User user);
}
