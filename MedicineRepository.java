package com.chandan.medical_store;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    // Basic CRUD operations auto-generate ho jayenge
}