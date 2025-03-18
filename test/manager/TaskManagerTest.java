package manager;

import exceptions.EntityIntersectionException;
import exceptions.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T taskManager;
    protected Task task;
    protected Epic epic;
    protected SubTask subTask;
    private LocalDateTime startTime;
    private Duration duration;

    protected abstract T createTaskManager();

    @BeforeEach
    void beforeEach() {
        taskManager = createTaskManager();
        startTime = LocalDateTime.parse(
                "04.03.2025 01:00",
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        );
        duration = Duration.ofMinutes(60);
    }

    @Test
    void taskFromTaskManagerShouldBeEqualToOriginal() {
        Task expectedTask = new Task("Name", "Description", Status.NEW, startTime, duration);
        int taskId = taskManager.createTask(expectedTask);
        Task actualTask = taskManager.getTask(taskId);

        assertEquals(expectedTask, actualTask);
    }

    @Test
    void subTaskFromTaskManagerShouldBeEqualToOriginal() {
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        SubTask expectedSubTask = new SubTask("Name", "Description", Status.NEW, epicId, startTime, duration);
        int subTaskId = taskManager.createSubTask(expectedSubTask);
        SubTask actualSubTask = taskManager.getSubTask(subTaskId);

        assertEquals(expectedSubTask, actualSubTask);
    }

    @Test
    void epicFromTaskManagerShouldBeEqualToOriginal() {
        Epic expectedEpic = new Epic("Name", "Description");
        int epicId = taskManager.createEpic(expectedEpic);
        Epic actualEpic = taskManager.getEpic(epicId);

        assertEquals(expectedEpic, actualEpic);
    }

    @Test
    void shouldIncreaseIdByOneForAllEntities() {
        int expectedFirstTaskId = 1;
        int actualFirstTaskId = taskManager.createTask(new Task("Name", "Description", Status.NEW, startTime, duration));

        assertEquals(expectedFirstTaskId, actualFirstTaskId);

        int expectedSecondTaskId = 2;
        int actualSecondTaskId = taskManager.createTask(new Task("Name", "Description", Status.NEW, startTime.plusMinutes(120), duration));

        assertEquals(expectedSecondTaskId, actualSecondTaskId);

        int expectedFirstEpicId = 3;
        int actualFirstEpicId = taskManager.createEpic(new Epic("Name", "Description"));

        assertEquals(expectedFirstEpicId, actualFirstEpicId);

        int expectedSecondEpicId = 4;
        int actualSecondEpicId = taskManager.createEpic(new Epic("Name", "Description"));

        assertEquals(expectedSecondEpicId, actualSecondEpicId);

        int expectedFirstSubTaskId = 5;
        int actualFirstSubTaskId = taskManager.createSubTask(new SubTask("Name", "Description", Status.NEW, actualFirstEpicId, startTime.plusMinutes(240), duration));

        assertEquals(expectedFirstSubTaskId, actualFirstSubTaskId);

        int expectedSecondSubTaskId = 6;
        int actualSecondSubTaskId = taskManager.createSubTask(new SubTask("Name", "Description", Status.NEW, actualSecondEpicId, startTime.plusMinutes(360), duration));

        assertEquals(expectedSecondSubTaskId, actualSecondSubTaskId);
    }

    @Test
    void shouldBeAssertWhenCreateSubTaskWithNonExistEpicId() {
        int nonExistEpicId = 54321;

        assertThrows(EntityNotFoundException.class, () -> {
            taskManager.createSubTask(new SubTask("Name", "Description", Status.NEW, nonExistEpicId, startTime, duration));
        });
    }

    @Test
    void shouldBeTrueAfterSuccessUpdatingTask() {
        Task originalTask = new Task("OriginalName", "OriginalDescription", Status.NEW, startTime, duration);
        int taskId = taskManager.createTask(originalTask);
        Task updatedTask = new Task("UpdatedName", "UpdatedDescription", Status.IN_PROGRESS, startTime.plusMinutes(120), duration);
        updatedTask.setId(taskId);

        boolean isUpdated = taskManager.updateTask(updatedTask);

        assertTrue(isUpdated);
    }

    @Test
    void shouldBeAssertAfterUpdatingNonExistingTask() {
        int nonExistingTaskId = 54321;
        Task nonExistingTask = new Task("Name", "Description", Status.IN_PROGRESS, startTime, duration);
        nonExistingTask.setId(nonExistingTaskId);
        assertThrows(EntityNotFoundException.class, () -> {
            taskManager.updateTask(nonExistingTask);
        });
    }

    @Test
    void taskFromOriginalTaskManagerShouldBeEqualAfterUpdating() {
        Task originalTask = new Task("OriginalName", "OriginalDescription", Status.NEW, startTime, duration);
        int taskId = taskManager.createTask(originalTask);
        Task updatedTask = new Task("UpdatedName", "UpdatedDescription", Status.IN_PROGRESS, startTime, duration);
        updatedTask.setId(taskId);

        taskManager.updateTask(updatedTask);
        Task actualTask = taskManager.getTask(taskId);

        assertEquals(updatedTask, actualTask);
    }

    @Test
    void shouldBeTrueAfterUpdatingEpic() {
        Epic originalEpic = new Epic("OriginalName", "OriginalDescription");
        int epicId = taskManager.createEpic(originalEpic);
        Epic updatedEpic = new Epic("UpdatedName", "UpdatedDescription");
        updatedEpic.setId(epicId);

        boolean isUpdated = taskManager.updateEpic(updatedEpic);

        assertTrue(isUpdated);
    }

    @Test
    void shouldBeAssertAfterUpdatingNonExistingEpic() {
        int nonExistingEpicId = 54321;
        Epic nonExistingEpic = new Epic("Name", "Description");
        nonExistingEpic.setId(nonExistingEpicId);
        assertThrows(EntityNotFoundException.class, () -> {
            taskManager.updateEpic(nonExistingEpic);
        });
    }

    @Test
    void epicFromOriginalTaskManagerShouldBeEqualAfterUpdating() {
        Epic originalEpic = new Epic("OriginalName", "OriginalDescription");
        int epicId = taskManager.createEpic(originalEpic);
        Epic updatedEpic = new Epic("UpdatedName", "UpdatedDescription");
        updatedEpic.setId(epicId);

        taskManager.updateEpic(updatedEpic);
        Epic actualEpic = taskManager.getEpic(epicId);

        assertEquals(updatedEpic, actualEpic);
    }

    @Test
    void shouldBeTrueAfterSuccessUpdatingSubTask() {
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        SubTask originalSubTask = new SubTask("OriginalName", "OriginalDescription", Status.NEW, epicId, startTime, duration);
        int subTaskId = taskManager.createSubTask(originalSubTask);
        SubTask updatedSubTask = new SubTask("UpdatedName", "UpdatedDescription", Status.IN_PROGRESS, epicId, startTime, duration);
        updatedSubTask.setId(subTaskId);

        boolean isUpdated = taskManager.updateSubTask(updatedSubTask);

        assertTrue(isUpdated);
    }

    @Test
    void shouldBeAssertAfterUpdatingNonExistingSubTask() {
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        int nonExistingSubTaskId = 54321;
        SubTask nonExistingSubTask = new SubTask("Name", "Description", Status.IN_PROGRESS, epicId, startTime, duration);
        nonExistingSubTask.setId(nonExistingSubTaskId);
        assertThrows(EntityNotFoundException.class, () -> {
            taskManager.updateSubTask(nonExistingSubTask);
        });
    }

    @Test
    void shouldBeAssertAfterUpdatingSubTaskAndEpicNotExist() {
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        SubTask originalSubTask = new SubTask("OriginalName", "OriginalDescription", Status.NEW, epicId, startTime, duration);
        int subTaskId = taskManager.createSubTask(originalSubTask);
        int nonExistingEpicId = 54321;
        SubTask nonExistingSubTask = new SubTask("Name", "Description", Status.IN_PROGRESS, nonExistingEpicId, startTime, duration);
        nonExistingSubTask.setId(subTaskId);

        assertThrows(EntityNotFoundException.class, () -> {
            taskManager.updateSubTask(nonExistingSubTask);
        });
    }

    @Test
    void shouldBeAssertAfterUpdatingSubTaskAndEpicNotContain() {
        int epicId = taskManager.createEpic(new Epic("FirstName", "FirstDescription"));
        SubTask originalSubTask = new SubTask("OriginalName", "OriginalDescription", Status.NEW, epicId, startTime, duration);
        int subTaskId = taskManager.createSubTask(originalSubTask);

        int otherEpicId = taskManager.createEpic(new Epic("OtherName", "OtherDescription"));
        SubTask updatedSubTask = new SubTask("UpdatedName", "UpdatedDescription", Status.IN_PROGRESS, otherEpicId, startTime, duration);
        updatedSubTask.setId(subTaskId);

        assertThrows(EntityNotFoundException.class, () -> {
            taskManager.updateSubTask(updatedSubTask);
        });
    }

    @Test
    void subTaskFromOriginalTaskManagerShouldBeEqualAfterUpdating() {
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        SubTask originalSubTask = new SubTask("OriginalName", "OriginalDescription", Status.NEW, epicId, startTime, duration);
        int subTaskId = taskManager.createSubTask(originalSubTask);
        SubTask updatedSubTask = new SubTask("UpdatedName", "UpdatedDescription", Status.IN_PROGRESS, epicId, startTime, duration);
        updatedSubTask.setId(subTaskId);

        taskManager.updateSubTask(updatedSubTask);

        SubTask actualSubTask = taskManager.getSubTask(subTaskId);

        assertEquals(updatedSubTask, actualSubTask);
    }

    @Test
    void epicStatusShouldBeNewAfterCreation() {
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        Status expectedEpicStatus = Status.NEW;
        Status actualEpicStatus = taskManager.getEpic(epicId).getStatus();

        assertEquals(expectedEpicStatus, actualEpicStatus);
    }

    @Test
    void epicStatusShouldChangeAfterSubTaskAdding() {
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        SubTask originalSubTask = new SubTask("OriginalName", "OriginalDescription", Status.IN_PROGRESS, epicId, startTime, duration);
        taskManager.createSubTask(originalSubTask);

        Status actualStatusAfterAdding = taskManager.getEpic(epicId).getStatus();
        Status expectedStatusAfterAdding = Status.IN_PROGRESS;

        assertEquals(expectedStatusAfterAdding, actualStatusAfterAdding);
    }

    @Test
    void epicStatusShouldChangeAfterSubTaskDeleting() {
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        SubTask originalSubTask = new SubTask("OriginalName", "OriginalDescription", Status.IN_PROGRESS, epicId, startTime, duration);
        int subTaskId = taskManager.createSubTask(originalSubTask);
        taskManager.deleteSubTask(subTaskId);

        Status actualStatusAfterDeleting = taskManager.getEpic(epicId).getStatus();
        Status expectedStatusAfterDeleting = Status.NEW;

        assertEquals(expectedStatusAfterDeleting, actualStatusAfterDeleting);
    }

    @Test
    void epicStatusShouldChangeAfterSubTasksStatusChanging() {
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        SubTask firstSubTask = new SubTask("FirstName", "FirstDescription", Status.NEW, epicId, startTime, duration);
        SubTask secondSubTask = new SubTask("SecondName", "SecondDescription", Status.NEW, epicId, startTime.plusMinutes(240), duration);
        taskManager.createSubTask(firstSubTask);
        taskManager.createSubTask(secondSubTask);

        Status actualEpicStatus = taskManager.getEpic(epicId).getStatus();
        Status expectedEpicStatus = Status.NEW;

        assertEquals(expectedEpicStatus, actualEpicStatus);

        firstSubTask.setStatus(Status.NEW);
        secondSubTask.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubTask(firstSubTask);
        taskManager.updateSubTask(secondSubTask);

        actualEpicStatus = taskManager.getEpic(epicId).getStatus();
        expectedEpicStatus = Status.IN_PROGRESS;

        assertEquals(expectedEpicStatus, actualEpicStatus);

        firstSubTask.setStatus(Status.DONE);
        secondSubTask.setStatus(Status.DONE);
        taskManager.updateSubTask(firstSubTask);
        taskManager.updateSubTask(secondSubTask);

        actualEpicStatus = taskManager.getEpic(epicId).getStatus();
        expectedEpicStatus = Status.DONE;

        assertEquals(expectedEpicStatus, actualEpicStatus);
    }

    @Test
    void shouldReturnAllCreatedTasks() {
        Task firstTask = new Task("FirstName", "FirstDescription", Status.NEW, startTime, duration);
        Task secondTask = new Task("SecondName", "SecondDescription", Status.IN_PROGRESS, startTime.plusMinutes(120), duration);
        Task thirdTask = new Task("ThirdName", "ThirdDescription", Status.DONE, startTime.plusMinutes(240), duration);
        ArrayList<Task> expectedTasks = new ArrayList<>(Arrays.asList(firstTask, secondTask, thirdTask));
        for (Task task : expectedTasks) {
            taskManager.createTask(task);
        }

        ArrayList<Task> actualTasks = new ArrayList<>(taskManager.getTasks());

        assertEquals(expectedTasks, actualTasks);
    }

    @Test
    void shouldReturnAllCreatedEpics() {
        Epic firstEpic = new Epic("FirstName", "FirstDescription");
        Epic secondEpic = new Epic("SecondName", "SecondDescription");
        Epic thirdEpic = new Epic("ThirdName", "ThirdDescription");
        ArrayList<Epic> expectedEpics = new ArrayList<>(Arrays.asList(firstEpic, secondEpic, thirdEpic));
        for (Epic epic : expectedEpics) {
            taskManager.createEpic(epic);
        }

        ArrayList<Epic> actualEpics = new ArrayList<>(taskManager.getEpics());

        assertEquals(expectedEpics, actualEpics);
    }

    @Test
    void shouldReturnAllCreatedSubTasks() {
        int epicId = taskManager.createEpic(new Epic("FirstName", "FirstDescription"));
        SubTask firstSubTask = new SubTask("FirstName", "FirstDescription", Status.NEW, epicId, startTime, duration);
        SubTask secondSubTask = new SubTask("SecondName", "SecondDescription", Status.IN_PROGRESS, epicId, startTime.plusMinutes(120), duration);
        SubTask thirdSubTask = new SubTask("ThirdName", "ThirdDescription", Status.DONE, epicId, startTime.plusMinutes(240), duration);
        ArrayList<SubTask> expectedSubTasks = new ArrayList<>(Arrays.asList(firstSubTask, secondSubTask, thirdSubTask));
        for (SubTask subTask : expectedSubTasks) {
            taskManager.createSubTask(subTask);
        }

        ArrayList<SubTask> actualSubTasks = new ArrayList<>(taskManager.getSubTasks());

        assertEquals(expectedSubTasks, actualSubTasks);
    }

    @Test
    void shouldReturnTasksListWithoutDeletedTask() {
        HashMap<Integer, Task> originalTasks = new HashMap<>();
        Task firstTask = new Task("FirstName", "FirstDescription", Status.NEW, startTime, duration);
        Task secondTask = new Task("SecondName", "SecondDescription", Status.IN_PROGRESS, startTime.plusMinutes(120), duration);
        Task thirdTask = new Task("ThirdName", "ThirdDescription", Status.DONE, startTime.plusMinutes(240), duration);
        originalTasks.put(1, firstTask);
        originalTasks.put(2, secondTask);
        originalTasks.put(3, thirdTask);
        int taskIdForDeletion = 2;

        for (Integer key : originalTasks.keySet()) {
            taskManager.createTask(originalTasks.get(key));
        }

        ArrayList<Task> tasksBeforeDeletion = new ArrayList<>(taskManager.getTasks());
        int actualSizeBeforeDeletion = tasksBeforeDeletion.size();
        int expectedSizeBeforeDeletion = 3;

        assertEquals(expectedSizeBeforeDeletion, actualSizeBeforeDeletion);

        taskManager.deleteTask(taskIdForDeletion);

        ArrayList<Task> actualTasksAfterDeletion = new ArrayList<>(taskManager.getTasks());
        originalTasks.remove(taskIdForDeletion);
        ArrayList<Task> expectedTasksAfterDeletion = new ArrayList<>(originalTasks.values());
        int actualSizeAfterDeletion = actualTasksAfterDeletion.size();
        int expectedSizeAfterDeletion = 2;

        assertEquals(expectedSizeAfterDeletion, actualSizeAfterDeletion);
        assertEquals(expectedTasksAfterDeletion, actualTasksAfterDeletion);
    }

    @Test
    void subTaskShouldBeDeletedAfterEpicDeletion() {
        Epic firstEpic = new Epic("FirstName", "FirstDescription");
        Epic secondEpic = new Epic("SecondName", "SecondDescription");
        ArrayList<Epic> originalEpics = new ArrayList<>(Arrays.asList(firstEpic, secondEpic));
        for (Epic epic : originalEpics) {
            taskManager.createEpic(epic);
        }
        int idOfFirstEpic = taskManager.getEpics().getFirst().getId();
        SubTask firstSubTask = new SubTask("FirstName", "FirstDescription", Status.NEW, idOfFirstEpic, startTime, duration);
        SubTask secondSubTask = new SubTask("SecondName", "SecondDescription", Status.IN_PROGRESS, idOfFirstEpic, startTime.plusMinutes(120), duration);
        ArrayList<SubTask> originalSubTasks = new ArrayList<>(Arrays.asList(firstSubTask, secondSubTask));
        for (SubTask subTask : originalSubTasks) {
            taskManager.createSubTask(subTask);
        }

        taskManager.deleteEpic(idOfFirstEpic);

        ArrayList<Epic> epicsAfterDeletion = new ArrayList<>(taskManager.getEpics());
        ArrayList<SubTask> subTasksAfterDeletion = new ArrayList<>(taskManager.getSubTasks());
        Epic expectedEpic = originalEpics.get(1);
        int actualEpicsSize = epicsAfterDeletion.size();
        int expectedEpicsSize = 1;

        assertTrue(subTasksAfterDeletion.isEmpty());
        assertEquals(expectedEpicsSize, actualEpicsSize);
        assertEquals(expectedEpic, epicsAfterDeletion.getFirst());
    }

    @Test
    void shouldBeSubTasksWithoutDeletedSubTask() {
        int epicId = taskManager.createEpic(new Epic("FirstName", "FirstDescription"));
        SubTask firstSubTask = new SubTask("FirstName", "FirstDescription", Status.NEW, epicId, startTime, duration);
        SubTask secondSubTask = new SubTask("SecondName", "SecondDescription", Status.IN_PROGRESS, epicId, startTime.plusMinutes(120), duration);
        ArrayList<SubTask> originalSubTasks = new ArrayList<>(Arrays.asList(firstSubTask, secondSubTask));
        for (SubTask subTask : originalSubTasks) {
            taskManager.createSubTask(subTask);
        }

        taskManager.deleteSubTask(2);

        ArrayList<SubTask> subTasksAfterDeletion = new ArrayList<>(taskManager.getSubTasks());
        originalSubTasks.remove(0);
        ArrayList<SubTask> expectedSubTasksAfterDeletion = originalSubTasks;
        int actualSubTasksSize = subTasksAfterDeletion.size();
        int expectedSubTasksSize = 1;

        assertEquals(expectedSubTasksSize, actualSubTasksSize);
        assertEquals(expectedSubTasksAfterDeletion, subTasksAfterDeletion);
    }

    @Test
    void shouldBeEmptyTasksAfterDeletingAllTasks() {
        Task firstTask = new Task("FirstName", "FirstDescription", Status.NEW, startTime, duration);
        Task secondTask = new Task("SecondName", "SecondDescription", Status.IN_PROGRESS, startTime.plusMinutes(120), duration);
        Task thirdTask = new Task("ThirdName", "ThirdDescription", Status.DONE, startTime.plusMinutes(240), duration);
        ArrayList<Task> originalTasks = new ArrayList<>(Arrays.asList(firstTask, secondTask, thirdTask));
        for (Task task : originalTasks) {
            taskManager.createTask(task);
        }

        taskManager.deleteAllTasks();
        ArrayList<Task> actualTasks = new ArrayList<>(taskManager.getTasks());

        assertTrue(actualTasks.isEmpty());
    }

    @Test
    void shouldBeEmptyEpicsAndSubTasksAfterDeletingAllEpics() {
        Epic firstEpic = new Epic("FirstName", "FirstDescription");
        Epic secondEpic = new Epic("SecondName", "SecondDescription");
        ArrayList<Epic> originalEpics = new ArrayList<>(Arrays.asList(firstEpic, secondEpic));
        for (Epic epic : originalEpics) {
            taskManager.createEpic(epic);
        }
        int epicId = taskManager.getEpics().getFirst().getId();
        SubTask firstSubTask = new SubTask("FirstName", "FirstDescription", Status.NEW, epicId, startTime, duration);
        SubTask secondSubTask = new SubTask("SecondName", "SecondDescription", Status.IN_PROGRESS, epicId, startTime.plusMinutes(120), duration);
        ArrayList<SubTask> originalSubTasks = new ArrayList<>(Arrays.asList(firstSubTask, secondSubTask));
        for (SubTask subTask : originalSubTasks) {
            taskManager.createSubTask(subTask);
        }

        taskManager.deleteAllEpics();
        ArrayList<Epic> actualEpics = new ArrayList<>(taskManager.getEpics());
        ArrayList<SubTask> actualSubTasks = new ArrayList<>(taskManager.getSubTasks());

        assertTrue(actualEpics.isEmpty());
        assertTrue(actualSubTasks.isEmpty());
    }

    @Test
    void shouldBeEmptySubTasksAfterDeletingAllSubTasks() {
        Epic firstEpic = new Epic("FirstName", "FirstDescription");
        Epic secondEpic = new Epic("SecondName", "SecondDescription");
        ArrayList<Epic> originalEpics = new ArrayList<>(Arrays.asList(firstEpic, secondEpic));
        for (Epic epic : originalEpics) {
            taskManager.createEpic(epic);
        }
        int epicId = taskManager.getEpics().getFirst().getId();
        SubTask firstSubTask = new SubTask("FirstName", "FirstDescription", Status.NEW, epicId, startTime, duration);
        SubTask secondSubTask = new SubTask("SecondName", "SecondDescription", Status.IN_PROGRESS, epicId, startTime.plusMinutes(120), duration);
        ArrayList<SubTask> originalSubTasks = new ArrayList<>(Arrays.asList(firstSubTask, secondSubTask));
        for (SubTask subTask : originalSubTasks) {
            taskManager.createSubTask(subTask);
        }

        taskManager.deleteAllSubTasks();
        ArrayList<SubTask> actualSubTasks = new ArrayList<>(taskManager.getSubTasks());

        assertTrue(actualSubTasks.isEmpty());
    }

    @Test
    void epicsStatusShouldBeNewAfterDeletingAllSubTasks() {
        Epic firstEpic = new Epic("FirstName", "FirstDescription");
        Epic secondEpic = new Epic("SecondName", "SecondDescription");
        ArrayList<Epic> originalEpics = new ArrayList<>(Arrays.asList(firstEpic, secondEpic));
        for (Epic epic : originalEpics) {
            taskManager.createEpic(epic);
        }
        int idOfFirstEpic = taskManager.getEpics().getFirst().getId();
        int idOfSecondEpic = taskManager.getEpics().getLast().getId();
        SubTask firstSubTask = new SubTask("FirstName", "FirstDescription", Status.IN_PROGRESS, idOfFirstEpic, startTime, duration);
        SubTask secondSubTask = new SubTask("SecondName", "SecondDescription", Status.DONE, idOfSecondEpic, startTime.plusMinutes(120), duration);
        ArrayList<SubTask> originalSubTasks = new ArrayList<>(Arrays.asList(firstSubTask, secondSubTask));
        for (SubTask subTask : originalSubTasks) {
            taskManager.createSubTask(subTask);
        }

        taskManager.deleteAllSubTasks();
        Status expectedStatus = Status.NEW;

        for (Epic epic : taskManager.getEpics()) {
            assertEquals(expectedStatus, epic.getStatus());
        }
    }

    @Test
    void epicSubTaskIdListShouldBeEmptyAfterDeletingAllSubTasks() {
        int epicId = taskManager.createEpic(new Epic("FirstName", "FirstDescription"));
        taskManager.createSubTask(new SubTask("FirstName", "FirstDescription", Status.IN_PROGRESS, epicId, startTime, duration));
        taskManager.createSubTask(new SubTask("SecondName", "SecondDescription", Status.DONE, epicId, startTime.plusMinutes(120), duration));

        taskManager.deleteAllSubTasks();
        ArrayList<Integer> actualSubTaskIdList = taskManager.getEpic(epicId).getSubTaskIdList();

        assertTrue(actualSubTaskIdList.isEmpty());
    }

    @Test
    void shouldBeAssertIfEpicIdNotExist() {
        int nonExistEpicId = 54321;
        assertThrows(EntityNotFoundException.class, () -> {
            taskManager.getSubTasksByEpic(nonExistEpicId);
        });
    }

    @Test
    void shouldReturnSubTasksByEpicId() {
        int epicId = taskManager.createEpic(new Epic("FirstName", "FirstDescription"));
        SubTask firstSubTask = new SubTask("FirstName", "FirstDescription", Status.IN_PROGRESS, epicId, startTime, duration);
        SubTask secondSubTask = new SubTask("SecondName", "SecondDescription", Status.DONE, epicId, startTime.plusMinutes(120), duration);
        ArrayList<SubTask> originalSubTasks = new ArrayList<>(Arrays.asList(firstSubTask, secondSubTask));
        for (SubTask subTask : originalSubTasks) {
            taskManager.createSubTask(subTask);
        }

        ArrayList<SubTask> actualSubTasksByEpicId = new ArrayList<>(taskManager.getSubTasksByEpic(epicId));
        ArrayList<SubTask> expectedSubTasks = new ArrayList<>(taskManager.getSubTasks());

        assertEquals(expectedSubTasks, actualSubTasksByEpicId);
    }

    @Test
    void shouldReturnHistoryAfterTasksGetting() {
        int taskId = taskManager.createTask(new Task("TaskName", "TaskDescription", Status.NEW, startTime, duration));
        int epicId = taskManager.createEpic(new Epic("EpicName", "EpicDescription"));
        int subTaskId = taskManager.createSubTask(new SubTask("SubTaskName", "SubTaskDescription", Status.NEW, epicId, startTime.plusMinutes(120), duration));

        Task task = taskManager.getTask(taskId);
        Epic epic = taskManager.getEpic(epicId);
        SubTask subTask = taskManager.getSubTask(subTaskId);

        ArrayList<Task> actualHistory = new ArrayList<>(taskManager.getHistory());
        ArrayList<Task> expectedHistory = new ArrayList<>(Arrays.asList(task, epic, subTask));

        assertEquals(expectedHistory, actualHistory);
    }

    @Test
    void shouldDeleteTaskFromTasksAndHistory() {
        Task task = new Task("TaskName", "TaskDescription", Status.NEW, startTime, duration);
        task.setId(1);
        taskManager.createTask(task);
        Task taskFromManager = taskManager.getTask(task.getId());

        assertTrue(taskManager.getTasks().contains(taskFromManager));
        assertTrue(taskManager.getHistory().contains(taskFromManager));

        taskManager.deleteTask(task.getId());

        assertFalse(taskManager.getTasks().contains(taskFromManager));
        assertFalse(taskManager.getHistory().contains(taskFromManager));
    }

    @Test
    void shouldDeleteEpicFromEpicsAndHistory() {
        Epic epic = new Epic("Name", "Description");
        int epicId = taskManager.createEpic(epic);
        Epic epicFromManager = taskManager.getEpic(epic.getId());

        assertTrue(taskManager.getEpics().contains(epicFromManager));
        assertTrue(taskManager.getHistory().contains(epicFromManager));

        taskManager.deleteEpic(epicId);

        assertFalse(taskManager.getEpics().contains(epicFromManager));
        assertFalse(taskManager.getHistory().contains(epicFromManager));
    }

    @Test
    void shouldDeleteSubTaskFromSubTasksAndHistory() {
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        SubTask subTask = new SubTask("Name", "Description", Status.NEW, epicId, startTime, duration);
        int subTaskId = taskManager.createSubTask(subTask);
        SubTask subTaskFromManager = taskManager.getSubTask(subTaskId);

        assertTrue(taskManager.getSubTasks().contains(subTaskFromManager));
        assertTrue(taskManager.getHistory().contains(subTaskFromManager));

        taskManager.deleteSubTask(subTaskId);

        assertFalse(taskManager.getSubTasks().contains(subTaskFromManager));
        assertFalse(taskManager.getHistory().contains(subTaskFromManager));
    }

    @Test
    void shouldReturnPrioritizedTasksAndSubTasksAfterCreate() {
        LocalDateTime firstStartTime = startTime;
        LocalDateTime secondStartTime = startTime.plusMinutes(120);
        LocalDateTime thirdStartTime = startTime.plusMinutes(240);
        LocalDateTime fourthStartTime = startTime.plusMinutes(360);

        Task firstTask = new Task("Name1", "Description1", Status.NEW, firstStartTime, duration);
        Task secondTask = new Task("Name2", "Description2", Status.NEW, secondStartTime, duration);
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        SubTask firstSubTask = new SubTask("Name1", "Description1", Status.NEW, epicId, thirdStartTime, duration);
        SubTask secondSubTask = new SubTask("Name2", "Description2", Status.NEW, epicId, fourthStartTime, duration);

        int secondSubTaskId = taskManager.createSubTask(secondSubTask);
        int firstTaskId = taskManager.createTask(firstTask);
        int firstSubTaskId = taskManager.createSubTask(firstSubTask);
        int secondTaskId = taskManager.createTask(secondTask);

        Task firstTaskFromManager = taskManager.getTask(firstTaskId);
        Task secondTaskFromManager = taskManager.getTask(secondTaskId);
        SubTask firstSubTaskFromManager = taskManager.getSubTask(firstSubTaskId);
        SubTask secondSubTaskFromManager = taskManager.getSubTask(secondSubTaskId);

        List<Task> prioritizedTasksAndSubTasks = taskManager.getPrioritizedTasks();

        assertEquals(4, prioritizedTasksAndSubTasks.size(),
                "Кол-во элементов в списке соответствует кол-ву задач и подзадач");

        assertEquals(firstTaskFromManager, prioritizedTasksAndSubTasks.get(0),
                "Первая задача в списке соответствует самой ранней задаче");

        assertEquals(secondTaskFromManager, prioritizedTasksAndSubTasks.get(1),
                "Вторая задача в списке соответствует следующей задаче по startTime");

        assertEquals(firstSubTaskFromManager, prioritizedTasksAndSubTasks.get(2),
                "Третья задача в списке соответствует следующей задаче по startTime");

        assertEquals(secondSubTaskFromManager, prioritizedTasksAndSubTasks.get(3),
                "Четвёртая задача в списке соответствует самой поздней подзадаче");
    }

    @Test
    void shouldReturnPrioritizedTasksAndSubTasksAfterUpdate() {
        LocalDateTime firstStartTime = startTime;
        LocalDateTime secondStartTime = startTime.plusMinutes(120);
        LocalDateTime thirdStartTime = startTime.plusMinutes(240);
        LocalDateTime fourthStartTime = startTime.plusMinutes(360);
        LocalDateTime firstStartTimeAfterUpdate = startTime.plusMinutes(480);
        LocalDateTime secondStartTimeAfterUpdate = startTime.plusMinutes(600);
        LocalDateTime thirdStartTimeAfterUpdate = startTime.plusMinutes(720);
        LocalDateTime fourthStartTimeAfterUpdate = startTime.plusMinutes(840);

        Task firstTask = new Task("FirstTaskName", "FirstTaskDescription", Status.NEW, firstStartTime, duration);
        Task secondTask = new Task("SecondTaskName", "SecondTaskDescription", Status.NEW, secondStartTime, duration);
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        SubTask firstSubTask = new SubTask("FirstSubTaskName", "FirstSubTaskDescription", Status.NEW, epicId, thirdStartTime, duration);
        SubTask secondSubTask = new SubTask("SecondSubTaskName", "SecondSubTaskDescription", Status.NEW, epicId, fourthStartTime, duration);

        int firstTaskId = taskManager.createTask(firstTask);
        int secondTaskId = taskManager.createTask(secondTask);
        int firstSubTaskId = taskManager.createSubTask(firstSubTask);
        int secondSubTaskId = taskManager.createSubTask(secondSubTask);

        Task updatedFirstTask = new Task("FirstTaskName", "FirstTaskDescription", Status.NEW, fourthStartTimeAfterUpdate, duration);
        updatedFirstTask.setId(2);
        Task updatedSecondTask = new Task("FirstTaskName", "FirstTaskDescription", Status.NEW, thirdStartTimeAfterUpdate, duration);
        updatedSecondTask.setId(3);
        SubTask updatedFirstSubTask = new SubTask("FirstSubTaskName", "FirstSubTaskDescription", Status.NEW, epicId, secondStartTimeAfterUpdate, duration);
        updatedFirstSubTask.setId(4);
        SubTask updatedSecondSubTask = new SubTask("SecondSubTaskName", "SecondSubTaskDescription", Status.NEW, epicId, firstStartTimeAfterUpdate, duration);
        updatedSecondSubTask.setId(5);

        taskManager.updateSubTask(updatedFirstSubTask);
        taskManager.updateSubTask(updatedSecondSubTask);
        taskManager.updateTask(updatedFirstTask);
        taskManager.updateTask(updatedSecondTask);

        Task firstTaskFromManager = taskManager.getTask(firstTaskId);
        Task secondTaskFromManager = taskManager.getTask(secondTaskId);
        SubTask firstSubTaskFromManager = taskManager.getSubTask(firstSubTaskId);
        SubTask secondSubTaskFromManager = taskManager.getSubTask(secondSubTaskId);

        List<Task> prioritizedTasksAndSubTasks = taskManager.getPrioritizedTasks();

        assertEquals(4, prioritizedTasksAndSubTasks.size(),
                "Кол-во элементов в списке соответствует кол-ву задач и подзадач");

        assertEquals(secondSubTaskFromManager, prioritizedTasksAndSubTasks.get(0),
                "Задача с самым ранним startTime является самым первым в списке приоритезированных задач после обновления");

        assertEquals(firstSubTaskFromManager, prioritizedTasksAndSubTasks.get(1),
                "Вторая задача в списке является следующей задачей по startTime после обновления");

        assertEquals(secondTaskFromManager, prioritizedTasksAndSubTasks.get(2),
                "Третья задача в списке является следующей задачей по startTime после обновления");

        assertEquals(firstTaskFromManager, prioritizedTasksAndSubTasks.get(3),
                "Последняя задача в списке является задачей с самым поздним startTime после обновления");
    }

    @Test
    void prioritizedTasksShouldBeEmptyAfterCreatingTaskWithNullStartTime() {
        Task task = new Task("Name", "Description", Status.NEW, null, duration);
        int taskId = taskManager.createTask(task);
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

        assertTrue(prioritizedTasks.isEmpty(), "Список приоритетных задач должен быть пуст");
    }

    @Test
    void prioritizedTasksShouldBeEmptyAfterCreatingTaskWithNullDurationTime() {
        Task task = new Task("Name", "Description", Status.NEW, startTime, null);
        int taskId = taskManager.createTask(task);
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

        assertTrue(prioritizedTasks.isEmpty(), "Список приоритетных задач должен быть пуст");
    }

    @Test
    void prioritizedTasksShouldContainTaskAfterUpdateFromNullStartTime() {
        Task task = new Task("Name", "Description", Status.NEW, null, duration);
        int taskId = taskManager.createTask(task);
        Task updatedTask = new Task("Name", "Description", Status.NEW, startTime, duration);
        updatedTask.setId(1);
        taskManager.updateTask(updatedTask);
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

        assertFalse(prioritizedTasks.isEmpty(), "Список приоритетных задач НЕ должен быть пуст");
        assertEquals(1, prioritizedTasks.size(), "В списке приоритетных задач только один объект");
        assertEquals(updatedTask, prioritizedTasks.getFirst(), "Задачи из списка приоритетных задач " +
                "соответствует обновлённой задаче");
    }

    @Test
    void prioritizedTasksShouldContainTaskAfterUpdateFromNullDuration() {
        Task task = new Task("Name", "Description", Status.NEW, startTime, null);
        int taskId = taskManager.createTask(task);
        Task updatedTask = new Task("Name", "Description", Status.NEW, startTime, duration);
        updatedTask.setId(1);
        taskManager.updateTask(updatedTask);
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

        assertFalse(prioritizedTasks.isEmpty(), "Список приоритетных задач НЕ должен быть пуст");
        assertEquals(1, prioritizedTasks.size(), "В списке приоритетных задач только один объект");
        assertEquals(updatedTask, prioritizedTasks.getFirst(), "Задачи из списка приоритетных задач " +
                "соответствует обновлённой задаче");
    }

    @Test
    void prioritizedTasksShouldBeEmptyAfterUpdateTaskOnNullStartTime() {
        Task task = new Task("Name", "Description", Status.NEW, startTime, duration);
        int taskId = taskManager.createTask(task);
        Task updatedTask = new Task("Name", "Description", Status.NEW, null, duration);
        updatedTask.setId(1);
        taskManager.updateTask(updatedTask);
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

        assertTrue(prioritizedTasks.isEmpty(), "Список приоритетных задач должен быть пуст");
    }

    @Test
    void prioritizedTasksShouldBeEmptyAfterUpdateTaskOnNullDuration() {
        Task task = new Task("Name", "Description", Status.NEW, startTime, duration);
        int taskId = taskManager.createTask(task);
        Task updatedTask = new Task("Name", "Description", Status.NEW, startTime, null);
        updatedTask.setId(1);
        taskManager.updateTask(updatedTask);
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

        assertTrue(prioritizedTasks.isEmpty(), "Список приоритетных задач должен быть пуст");
    }

    @Test
    void prioritizedTasksShouldBeEmptyAfterCreatingSubTaskWithNullStartTime() {
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        SubTask subTask = new SubTask("Name", "Description", Status.NEW, epicId, null, duration);
        int subTaskId = taskManager.createSubTask(subTask);
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

        assertTrue(prioritizedTasks.isEmpty(), "Список приоритетных задач должен быть пуст");
    }

    @Test
    void prioritizedTasksShouldBeEmptyAfterCreatingSubTaskWithNullDuration() {
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        SubTask subTask = new SubTask("Name", "Description", Status.NEW, epicId, startTime, null);
        int subTaskId = taskManager.createSubTask(subTask);
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

        assertTrue(prioritizedTasks.isEmpty(), "Список приоритетных задач должен быть пуст");
    }

    @Test
    void prioritizedTasksShouldContainSubTaskAfterUpdateFromNullStartTime() {
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        SubTask subTask = new SubTask("Name", "Description", Status.NEW, epicId, null, duration);
        int subTaskId = taskManager.createSubTask(subTask);
        SubTask updatedSubTask = new SubTask("Name", "Description", Status.NEW, epicId, startTime, duration);
        updatedSubTask.setId(2);
        taskManager.updateSubTask(updatedSubTask);
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

        assertFalse(prioritizedTasks.isEmpty(), "Список приоритетных задач НЕ должен быть пуст");
        assertEquals(1, prioritizedTasks.size(), "В списке приоритетных задач только один объект");
        assertEquals(updatedSubTask, prioritizedTasks.getFirst(), "Задачи из списка приоритетных задач " +
                "соответствует обновлённой задаче");
    }

    @Test
    void prioritizedTasksShouldContainSubTaskAfterUpdateFromNullDuration() {
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        SubTask subTask = new SubTask("Name", "Description", Status.NEW, epicId, startTime, null);
        int subTaskId = taskManager.createSubTask(subTask);
        SubTask updatedSubTask = new SubTask("Name", "Description", Status.NEW, epicId, startTime, duration);
        updatedSubTask.setId(2);
        taskManager.updateSubTask(updatedSubTask);
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

        assertFalse(prioritizedTasks.isEmpty(), "Список приоритетных задач НЕ должен быть пуст");
        assertEquals(1, prioritizedTasks.size(), "В списке приоритетных задач только один объект");
        assertEquals(updatedSubTask, prioritizedTasks.getFirst(), "Задачи из списка приоритетных задач " +
                "соответствует обновлённой задаче");
    }

    @Test
    void prioritizedTasksShouldBeEmptyAfterUpdateSubTaskOnNullStartTime() {
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        SubTask subTask = new SubTask("Name", "Description", Status.NEW, epicId, startTime, duration);
        int subTaskId = taskManager.createSubTask(subTask);
        SubTask updatedSubTask = new SubTask("Name", "Description", Status.NEW, epicId, null, duration);
        updatedSubTask.setId(2);
        taskManager.updateSubTask(updatedSubTask);
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

        assertTrue(prioritizedTasks.isEmpty(), "Список приоритетных задач должен быть пуст");
    }

    @Test
    void prioritizedTasksShouldBeEmptyAfterUpdateSubTaskOnNullDuration() {
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        SubTask subTask = new SubTask("Name", "Description", Status.NEW, epicId, startTime, duration);
        int subTaskId = taskManager.createSubTask(subTask);
        SubTask updatedSubTask = new SubTask("Name", "Description", Status.NEW, epicId, startTime, null);
        updatedSubTask.setId(2);
        taskManager.updateSubTask(updatedSubTask);
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

        assertTrue(prioritizedTasks.isEmpty(), "Список приоритетных задач должен быть пуст");
    }

    @Test
    void epicTimeShouldBeEmptyAfterCreation() {
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        Epic epic = taskManager.getEpic(epicId);
        LocalDateTime epicTime = epic.getEndTime();

        assertNull(epicTime, "Время эпика после создания должно быть null");
    }

    @Test
    void epicTimeShouldBeEqualOfOneSubTaskTimeAfterCreation() {
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        SubTask subTask = new SubTask("Name", "Description", Status.NEW, epicId, startTime, duration);
        int subTaskId = taskManager.createSubTask(subTask);
        SubTask subTaskFromManager = taskManager.getSubTask(subTaskId);
        Epic epic = taskManager.getEpic(epicId);

        LocalDateTime expectedStartTime = subTaskFromManager.getStartTime();
        LocalDateTime expectedEndTime = subTaskFromManager.getEndTime();
        Duration expectedDuration = Duration.between(expectedStartTime, expectedEndTime);

        LocalDateTime actualStartTime = epic.getStartTime();
        LocalDateTime actualEndTime = epic.getEndTime();
        Duration actualDuration = epic.getDuration();

        assertEquals(expectedStartTime, actualStartTime, "startTime эпика должно соответствовать startTime " +
                "самой ранней задачи");
        assertEquals(expectedEndTime, actualEndTime, "endTime эпика должно соответствовать endTime задачи");
        assertEquals(expectedDuration, actualDuration, "Duration эпика должно соответствовать времени между " +
                "startTime и endTime");
    }

    @Test
    void epicTimeShouldBeEqualOfAnySubTaskTimeAfterCreation() {
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        SubTask firstSubTask = new SubTask("Name", "Description", Status.NEW, epicId, startTime, duration);
        SubTask secondSubTask = new SubTask("Name", "Description", Status.NEW, epicId,
                startTime.plusMinutes(120), duration);
        int firstSubTaskId = taskManager.createSubTask(firstSubTask);
        int secondSubTaskId = taskManager.createSubTask(secondSubTask);
        SubTask firstSubTaskFromManager = taskManager.getSubTask(firstSubTaskId);
        SubTask secondSubTaskFromManager = taskManager.getSubTask(secondSubTaskId);
        Epic epic = taskManager.getEpic(epicId);
        LocalDateTime firstSubTaskStartTime = firstSubTaskFromManager.getStartTime();
        LocalDateTime firstSubTaskEndTime = firstSubTaskFromManager.getEndTime();
        LocalDateTime secondSubTaskStartTime = secondSubTaskFromManager.getStartTime();
        LocalDateTime secondSubTaskEndTime = secondSubTaskFromManager.getEndTime();

        LocalDateTime expectedStartTime = firstSubTaskStartTime;
        LocalDateTime expectedEndTime = secondSubTaskEndTime;
        Duration expectedDuration = Duration.between(firstSubTaskStartTime, firstSubTaskEndTime)
                .plus(Duration.between(secondSubTaskStartTime, secondSubTaskEndTime));

        LocalDateTime actualStartTime = epic.getStartTime();
        LocalDateTime actualEndTime = epic.getEndTime();
        Duration actualDuration = epic.getDuration();

        assertEquals(expectedStartTime, actualStartTime, "startTime эпика должно соответствовать startTime " +
                "самой ранней задачи");
        assertEquals(expectedEndTime, actualEndTime, "endTime эпика должно соответствовать самому позднему endTime задачи");
        assertEquals(expectedDuration, actualDuration, "Duration эпика должно соответствовать сумме " +
                "startTime и endTime всех подзадач");
    }

    @Test
    void epicTimeShouldBeEqualOfAnySubTaskTimeAfterUpdate() {
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        SubTask firstSubTask = new SubTask("Name", "Description", Status.NEW, epicId, startTime, duration);
        SubTask secondSubTask = new SubTask("Name", "Description", Status.NEW, epicId,
                startTime.plusMinutes(120), duration);
        int firstSubTaskId = taskManager.createSubTask(firstSubTask);
        int secondSubTaskId = taskManager.createSubTask(secondSubTask);
        SubTask updatedFirstSubTask = new SubTask("Name", "Description", Status.NEW, epicId,
                startTime.plusMinutes(240), duration.plusMinutes(30));
        updatedFirstSubTask.setId(2);
        SubTask updatedSecondSubTask = new SubTask("Name", "Description", Status.NEW, epicId,
                startTime.plusMinutes(360), duration.plusMinutes(90));
        updatedSecondSubTask.setId(3);
        taskManager.updateSubTask(updatedFirstSubTask);
        taskManager.updateSubTask(updatedSecondSubTask);

        SubTask firstSubTaskFromManager = taskManager.getSubTask(firstSubTaskId);
        SubTask secondSubTaskFromManager = taskManager.getSubTask(secondSubTaskId);
        Epic epic = taskManager.getEpic(epicId);

        LocalDateTime firstSubTaskStartTime = firstSubTaskFromManager.getStartTime();
        LocalDateTime firstSubTaskEndTime = firstSubTaskFromManager.getEndTime();
        LocalDateTime secondSubTaskStartTime = secondSubTaskFromManager.getStartTime();
        LocalDateTime secondSubTaskEndTime = secondSubTaskFromManager.getEndTime();

        LocalDateTime expectedStartTime = firstSubTaskStartTime;
        LocalDateTime expectedEndTime = secondSubTaskEndTime;
        Duration expectedDuration = Duration.between(firstSubTaskStartTime, firstSubTaskEndTime)
                .plus(Duration.between(secondSubTaskStartTime, secondSubTaskEndTime));

        LocalDateTime actualStartTime = epic.getStartTime();
        LocalDateTime actualEndTime = epic.getEndTime();
        Duration actualDuration = epic.getDuration();

        assertEquals(expectedStartTime, actualStartTime, "startTime эпика должно соответствовать startTime " +
                "самой ранней задачи");
        assertEquals(expectedEndTime, actualEndTime, "endTime эпика должно соответствовать самому позднему endTime задачи");
        assertEquals(expectedDuration, actualDuration, "Duration эпика должно соответствовать сумме " +
                "startTime и endTime всех подзадач");
    }

    @Test
    void epicTimeShouldBeEmptyAfterDeletionAllSubTasks() {
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        SubTask firstSubTask = new SubTask("Name", "Description", Status.NEW, epicId, startTime, duration);
        SubTask secondSubTask = new SubTask("Name", "Description", Status.NEW, epicId,
                startTime.plusMinutes(120), duration);
        int firstSubTaskId = taskManager.createSubTask(firstSubTask);
        int secondSubTaskId = taskManager.createSubTask(secondSubTask);

        taskManager.deleteAllSubTasks();

        Epic epic = taskManager.getEpic(epicId);
        LocalDateTime epicTime = epic.getEndTime();

        assertNull(epicTime, "Время эпика после удаления всех подзадач должно быть null");
    }

    @Test
    void epicTimeShouldBeEmptyIfContainSubTasksWithNullTimeOrDuration() {
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        SubTask firstSubTask = new SubTask("Name", "Description", Status.NEW, epicId, null, duration);
        SubTask secondSubTask = new SubTask("Name", "Description", Status.NEW, epicId,
                startTime.plusMinutes(120), null);
        taskManager.createSubTask(firstSubTask);
        taskManager.createSubTask(secondSubTask);

        Epic epic = taskManager.getEpic(epicId);
        LocalDateTime epicTime = epic.getEndTime();

        assertNull(epicTime, "Время эпика должно быть null если эпик содержит подзадачи с пустым startTime или duration");
    }

    @Test
    void epicTimeShouldBeEmptyAfterUpdateSubTasksToNullTimeOrDuration() {
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        SubTask firstSubTask = new SubTask("Name", "Description", Status.NEW, epicId, startTime, duration);
        SubTask secondSubTask = new SubTask("Name", "Description", Status.NEW, epicId,
                startTime.plusMinutes(120), duration);
        taskManager.createSubTask(firstSubTask);
        taskManager.createSubTask(secondSubTask);

        SubTask updatedFirstSubTask = new SubTask("Name", "Description", Status.NEW, epicId,
                null, duration);
        updatedFirstSubTask.setId(2);
        SubTask updatedSecondSubTask = new SubTask("Name", "Description", Status.NEW, epicId,
                startTime.plusMinutes(120), null);
        updatedSecondSubTask.setId(3);
        taskManager.updateSubTask(updatedFirstSubTask);
        taskManager.updateSubTask(updatedSecondSubTask);

        Epic epic = taskManager.getEpic(epicId);
        LocalDateTime epicTime = epic.getEndTime();

        assertNull(epicTime, "Время эпика должно быть null если эпик содержит подзадачи с пустым startTime или duration");
    }

    @Test
    void shouldBeAssertWhenTasksIsIntersectByTimeWhileCreate() {
        Task firstTask = new Task("Name1", "Description1", Status.NEW, startTime, duration);
        Task secondTask = new Task("Name2", "Description2", Status.NEW, startTime, duration);
        taskManager.createTask(firstTask);
        assertThrows(EntityIntersectionException.class, () -> {
            taskManager.createTask(secondTask);
        });
    }

    @Test
    void shouldBeAssertWhenSubTasksIsIntersectByTimeWhileCreate() {
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        SubTask firstSubTask = new SubTask("Name1", "Description1", Status.NEW, epicId, startTime, duration);
        SubTask secondSubTask = new SubTask("Name2", "Description2", Status.NEW, epicId, startTime, duration);
        taskManager.createSubTask(firstSubTask);
        assertThrows(EntityIntersectionException.class, () -> {
            taskManager.createSubTask(secondSubTask);
        });
    }

    @Test
    void shouldBeAssertWhenTasksIsIntersectByTimeWhileUpdate() {
        Task firstTask = new Task("Name", "Description", Status.NEW, startTime, duration);
        Task secondTask = new Task("Name", "Description", Status.NEW, startTime.plusMinutes(120), duration);
        taskManager.createTask(firstTask);
        taskManager.createTask(secondTask);

        firstTask.setStartTime(startTime.plusMinutes(120));

        assertThrows(EntityIntersectionException.class, () -> {
            taskManager.updateTask(firstTask);
        });
    }

    @Test
    void shouldBeAssertWhenSubTasksIsIntersectByTimeWhileUpdate() {
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        SubTask firstSubTask = new SubTask("Name", "Description", Status.NEW, epicId, startTime, duration);
        SubTask secondSubTask = new SubTask("Name", "Description", Status.NEW, epicId, startTime.plusMinutes(120), duration);
        taskManager.createSubTask(firstSubTask);
        taskManager.createSubTask(secondSubTask);

        firstSubTask.setStartTime(startTime.plusMinutes(120));

        assertThrows(EntityIntersectionException.class, () -> {
            taskManager.updateSubTask(firstSubTask);
        });
    }

    @Test
    void shouldBeAssertWhenFirstTaskStartsBeforeSecondPartialIntersection() {
        Task firstTask = new Task("Name", "Description", Status.NEW, startTime, duration);
        Task secondTask = new Task("Name", "Description", Status.NEW, startTime.plusMinutes(30), duration);
        taskManager.createTask(firstTask);

        assertThrows(EntityIntersectionException.class, () -> {
            taskManager.createTask(secondTask);
        });
    }

    @Test
    void shouldBeAssertWhenSecondTaskStartsBeforeFirstPartialIntersection() {
        Task firstTask = new Task("First Task", "Description", Status.NEW, startTime.plusMinutes(30), duration);
        Task secondTask = new Task("Second Task", "Description", Status.NEW, startTime, duration);
        taskManager.createTask(firstTask);

        assertThrows(EntityIntersectionException.class, () -> {
            taskManager.createTask(secondTask);
        });
    }

    @Test
    void shouldBeAssertWhenOneTaskIsCompletelyInsideAnother() {
        Task firstTask = new Task("First Task", "Description", Status.NEW, startTime, duration);
        Task secondTask = new Task("Second Task", "Description", Status.NEW, startTime.plusMinutes(10), Duration.ofMinutes(10));
        taskManager.createTask(firstTask);

        assertThrows(EntityIntersectionException.class, () -> {
            taskManager.createTask(secondTask);
        });
    }
}