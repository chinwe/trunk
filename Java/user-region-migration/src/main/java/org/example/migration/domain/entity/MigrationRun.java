package org.example.migration.domain.entity;

import lombok.Data;
import org.example.migration.domain.Direction;
import org.example.migration.domain.RegionName;
import org.example.migration.domain.RunStatus;

import java.time.LocalDateTime;

/**
 * 一次迁移执行记录。对应 migration_run 表。
 */
@Data
public class MigrationRun {

    private String runId;
    private String taskName;
    private Direction direction;
    private RegionName sourceRegion;
    private RegionName targetRegion;
    private String product;
    private String bizLine;
    private RunStatus status;
    private int totalTenants;
    private int processedTenants;
    private int failedTenants;
    private LocalDateTime startedAt;
    private LocalDateTime updatedAt;
    private String errorContext;
    /** 回滚 run 指向原正向 run，正向 run 为 null */
    private String parentRunId;
}
