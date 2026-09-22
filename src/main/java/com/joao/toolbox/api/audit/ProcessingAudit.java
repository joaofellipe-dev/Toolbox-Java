package com.joao.toolbox.api.audit;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;

@Getter
@Entity
@Table(name = "processing_audit")
public class ProcessingAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "operation_type", nullable = false)
    private String operationType;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "input_size_bytes")
    private long inputSizeBytes;

    @Column(name = "duration_ms", nullable = false)
    private long durationMs;

    @Column(name = "status"
    n nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public ProcessingAudit() {
    }

    public ProcessingAudit(long id, String operationType, String fileName, long inputSizeBytes, long durationMs, String status, Instante createdAt) {
        this.id = id;
        this.operationType = operationType;
        this.fileName = fileName;
        this.inputSizeBytes = inputSizeBytes;
        this.durationMs = durationMs;
        this.status = status;
        this.createdAt = createdAt;
    }

}
