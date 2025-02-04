package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    void beforeEach() {
        historyManager = Manager.getDefaultHistory();
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
    void shouldBeEmptyAfterRemovingAllKindOfTasks() {
        Task task = new Task("TaskName", "TaskDescription", Status.NEW);
        task.setId(1);
        Epic epic = new Epic("EpicName", "EpicDescription");
        epic.setId(2);
        SubTask subTask = new SubTask("SubTaskName", "SubTaskDescription", Status.NEW, 2);
        subTask.setId(3);

        historyManager.add(task);
        historyManager.add(epic);
        historyManager.add(subTask);

        historyManager.remove(2);
        ArrayList<Task> taskHistoryAfterRemoving = new ArrayList<>(historyManager.getHistory());

        assertEquals(2, taskHistoryAfterRemoving.size());
        assertTrue(taskHistoryAfterRemoving.contains(task));
        assertFalse(taskHistoryAfterRemoving.contains(epic));
        assertTrue(taskHistoryAfterRemoving.contains(subTask));

        historyManager.remove(1);
        taskHistoryAfterRemoving = new ArrayList<>(historyManager.getHistory());

        assertFalse(taskHistoryAfterRemoving.contains(task));

        historyManager.remove(3);
        taskHistoryAfterRemoving = new ArrayList<>(historyManager.getHistory());

        assertFalse(taskHistoryAfterRemoving.contains(subTask));
        assertEquals(0, taskHistoryAfterRemoving.size());
    }

    @Test
    public void duplicateTaskShouldBeDeleted() {
        Task task1 = new Task("TaskName", "TaskDescription", Status.NEW);
        task1.setId(1);
        Task task2 = new Task("TaskName", "TaskDescription", Status.NEW);
        task2.setId(1);

        historyManager.add(task1);
        historyManager.add(task2);

        List<Task> actualHistory = historyManager.getHistory();

        assertEquals(1, actualHistory.size());
        assertEquals(task2, actualHistory.get(0));
    }
}