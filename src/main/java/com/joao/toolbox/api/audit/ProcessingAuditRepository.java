package com.joao.toolbox.api.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessingAuditRepository extends JpaRepository<ProcessingAudit, Long> {
}
