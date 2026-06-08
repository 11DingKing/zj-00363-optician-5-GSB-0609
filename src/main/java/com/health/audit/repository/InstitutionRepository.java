package com.health.audit.repository;

import com.health.audit.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, Long> {
    List<Institution> findByFrozen(boolean frozen);
}
