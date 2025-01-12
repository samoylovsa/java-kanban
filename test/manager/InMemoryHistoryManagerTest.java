package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    void beforeEach() {
        historyManager = Manager.getDefaultHistory();
    }

    @Test
    void taskHistoryShouldContainOnlyTenEntities() {
        Task task = new Task("Name", "Description", Status.NEW);

        for (int i = 0; i < 20; i++) {
            historyManager.add(task);
        }

        int actualHistorySize = historyManager.getHistory().size();
        int expectedHistorySize = 10;

        assertEquals(expectedHistorySize, actualHistorySize);
    }

    @Test
    void taskHistoryCanContainTasksAndHisAncestors() {
        Task task = new Task("TaskName", "TaskDescription", Status.NEW);
        task.setId(1);
        Epic epic = new Epic("EpicName", "EpicDescription");
        epic.setId(2);
        SubTask subTask = new SubTask("SubTaskName", "SubTaskDescription", Status.NEW, 2);
        subTask.setId(3);

        historyManager.add(task);
        historyManager.add(epic);
        historyManager.add(subTask);

        ArrayList<Task> actualTaskHistory = new ArrayList<>(historyManager.getHistory());
        ArrayList<Task> expectedTaskHistory = new ArrayList<>(Arrays.asList(task, epic, subTask));

        assertEquals(expectedTaskHistory, actualTaskHistory);
    }

    @Test
    void lastElementInHistoryShouldBeLastAddedElement() {
        for (int i = 1; i <= 11; i++) {
            Epic epic = new Epic("Name", "Description");
            epic.setId(i);
            historyManager.add(epic);
        }

        Task actualLastElement = historyManager.getHistory().getLast();
        Epic epic = new Epic("Name", "Description");
        epic.setId(11);
        Task expectedLastElement = epic;

        assertEquals(expectedLastElement, actualLastElement);
    }

    @Test
    void firstElementInHistoryShouldBeDeletedAfterOversizing() {
        for (int i = 1; i <= 11; i++) {
            Epic epic = new Epic("Name", "Description");
            epic.setId(i);
            historyManager.add(epic);
        }

        Task actualFirstElement = historyManager.getHistory().getFirst();
        Epic epic = new Epic("Name", "Description");
        epic.setId(2);
        Task expectedFirstElement = epic;

        assertEquals(expectedFirstElement, actualFirstElement);

        epic.setId(1);
        Task expectedDeletedElement = epic;

        assertNotEquals(expectedDeletedElement, actualFirstElement);
    }
}