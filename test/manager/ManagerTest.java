package manager;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

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

    @Test
    void shouldReturnFileBackedManagerObject() {
        try {
            TaskManager taskManager = Manager.getFileBacked(File.createTempFile("test", ".csv"));
            assertNotNull(taskManager);
            assertInstanceOf(FileBackedTaskManager.class, taskManager);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка создания файла");
        }
    }
}