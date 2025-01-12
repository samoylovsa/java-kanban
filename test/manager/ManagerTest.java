package manager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ManagerTest {

    @Test
    void shouldReturnInMemoryTaskManagerObject() {
        TaskManager taskManager = Manager.getDefault();

        assertNotNull(taskManager);
        assertInstanceOf(InMemoryTaskManager.class, taskManager);
    }

    @Test
    void shouldReturnInMemoryHistoryManagerObject() {
        HistoryManager historyManager = Manager.getDefaultHistory();

        assertNotNull(historyManager);
        assertInstanceOf(InMemoryHistoryManager.class, historyManager);
    }
}