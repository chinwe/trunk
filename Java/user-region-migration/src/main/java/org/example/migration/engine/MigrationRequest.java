package org.example.migration.engine;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.migration.domain.RegionName;

import java.util.List;

/**
 * 迁移执行请求。由 migrate 命令构造，传给 MigrationEngine。
 */
@Getter
@RequiredArgsConstructor
public class MigrationRequest {

    private final String taskName;
    private final RegionName sourceRegion;
    private final RegionName targetRegion;
    private final String product;
    private final String bizLine;
    private final List<String> tenantIds;
    private final int batchSize;
    private final int threads;
}
