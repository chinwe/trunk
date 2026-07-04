package org.example.migration.engine;

/**
 * InMemoryCheckpointStore 的契约测试。
 */
class InMemoryCheckpointStoreContractTest extends CheckpointStoreContractTest {

    private final CheckpointStore store = new InMemoryCheckpointStore();

    @Override
    protected CheckpointStore store() {
        return store;
    }
}
