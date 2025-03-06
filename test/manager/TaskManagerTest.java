package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
    protected LocalDateTime startTime;
    protected Duration duration;
    protected BufferedReader bufferedReader;
    protected File file;

    protected abstract T createTaskManager();

    @BeforeEach
    void beforeEach() {
        taskManager = createTaskManager();
        startTime = LocalDateTime.parse(
                "04.03.2025 21:50",
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        );
        duration = Duration.ofMinutes(60);
    }

    void prepareForFileBackedTests() {
        try {
            file = File.createTempFile("test", ".csv");
            bufferedReader = new BufferedReader(new FileReader(file));

            Task firstTask = new Task("TaskName1", "TaskDescription1", Status.NEW, startTime, duration);
            Task secondTask = new Task("TaskName2", "TaskDescription2", Status.IN_PROGRESS, startTime.plusMinutes(120), duration);
            Task thirdTask = new Task("TaskName3", "TaskDescription3", Status.DONE, startTime.plusMinutes(240), duration);
            Epic firstEpic = new Epic("EpicName1", "EpicDescription1");
            Epic secondEpic = new Epic("EpicName2", "EpicDescription2");
            Epic thirdEpic = new Epic("EpicName3", "EpicDescription3");

            int firstTaskId = taskManager.createTask(firstTask);
            int secondTaskId = taskManager.createTask(secondTask);
            int thirdTaskId = taskManager.createTask(thirdTask);
            int firstEpicId = taskManager.createEpic(firstEpic);
            int secondEpicId = taskManager.createEpic(secondEpic);
            int thirdEpicId = taskManager.createEpic(thirdEpic);

            SubTask firstSubTask = new SubTask("SubTaskName1", "SubTaskDescription1", Status.NEW, firstEpicId, startTime.plusMinutes(360), duration);
            SubTask secondSubTask = new SubTask("SubTaskName2", "SubTaskDescription2", Status.IN_PROGRESS, secondEpicId, startTime.plusMinutes(480), duration);
            SubTask thirdSubTask = new SubTask("SubTaskName3", "SubTaskDescription3", Status.DONE, thirdEpicId, startTime.plusMinutes(600), duration);

            int firstSubTaskId = taskManager.createSubTask(firstSubTask);
            int secondSubTaskId = taskManager.createSubTask(secondSubTask);
            int thirdSubTaskId = taskManager.createSubTask(thirdSubTask);
        } catch (IOException exception) {
            System.out.println("Ошибка при создании файла для теста");
        }
    }

    void tearDownAfterFileBackedTests() {
        try {
            bufferedReader.close();
        } catch (IOException exception) {
            System.out.println("Ошибка закрытия потока после теста");
        }
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

        assertThrows(IllegalArgumentException.class, () -> {
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
        assertThrows(IllegalArgumentException.class, () -> {
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
        assertThrows(IllegalArgumentException.class, () -> {
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
        assertThrows(IllegalArgumentException.class, () -> {
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

        assertThrows(IllegalArgumentException.class, () -> {
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

        assertThrows(IllegalArgumentException.class, () -> {
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
        Task deletedTask = taskManager.getTask(taskIdForDeletion);
        int actualSizeAfterDeletion = actualTasksAfterDeletion.size();
        int expectedSizeAfterDeletion = 2;

        assertNull(deletedTask);
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
        assertThrows(IllegalArgumentException.class, () -> {
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
    public void tasksShouldBeWrittenInFileAfterCreate() throws IOException {
        prepareForFileBackedTests();
        bufferedReader.readLine();
        HashMap<Integer, List<String>> substrings = new HashMap<>(9);
        String line;
        int lineNumber = 0;
        while ((line = bufferedReader.readLine()) != null) {
            substrings.put(++lineNumber, List.of(line.split(",")));
        }

        assertEquals(10, substrings.size() + 1, "Файл должен содержать 10 строк (с заголовком)");

        assertEquals("1", substrings.get(1).get(0), "Первая строка содержит id на первом месте");
        assertEquals("TASK", substrings.get(1).get(1), "Первая строка содержит type на втором месте");
        assertEquals("TaskName1", substrings.get(1).get(2), "Первая строка содержит name на третьем месте");
        assertEquals("NEW", substrings.get(1).get(3), "Первая строка содержит status на четвёртом месте");
        assertEquals("TaskDescription1", substrings.get(1).get(4), "Первая строка содержит description на пятом месте");
        assertEquals("04.03.2025 21:52", substrings.get(1).get(5), "Первая строка содержит startTime на шестом месте");
        assertEquals("04.03.2025 23:52", substrings.get(1).get(6), "Первая строка содержит endTime на седьмом месте");
        assertEquals("PT2H", substrings.get(1).get(7), "Первая строка содержит duration на восьмом месте");

        assertEquals("2", substrings.get(2).get(0), "Вторая строка содержит id на первом месте");
        assertEquals("TASK", substrings.get(2).get(1), "Вторая строка содержит type на втором месте");
        assertEquals("TaskName2", substrings.get(2).get(2), "Вторая строка содержит name на третьем месте");
        assertEquals("IN_PROGRESS", substrings.get(2).get(3), "Вторая строка содержит status на четвёртом месте");
        assertEquals("TaskDescription2", substrings.get(2).get(4), "Вторая строка содержит description на пятом месте");
        assertEquals("04.03.2025 21:52", substrings.get(2).get(5), "Вторая строка содержит startTime на шестом месте");
        assertEquals("04.03.2025 23:52", substrings.get(2).get(6), "Вторая строка содержит endTime на седьмом месте");
        assertEquals("PT2H", substrings.get(2).get(7), "Вторая строка содержит duration на восьмом месте");

        assertEquals("3", substrings.get(3).get(0), "Третья строка содержит id на первом месте");
        assertEquals("TASK", substrings.get(3).get(1), "Третья строка содержит type на втором месте");
        assertEquals("TaskName3", substrings.get(3).get(2), "Третья строка содержит name на третьем месте");
        assertEquals("DONE", substrings.get(3).get(3), "Третья строка содержит status на четвёртом месте");
        assertEquals("TaskDescription3", substrings.get(3).get(4), "Третья строка содержит description на пятом месте");
        assertEquals("04.03.2025 21:52", substrings.get(3).get(5), "Третья строка содержит startTime на шестом месте");
        assertEquals("04.03.2025 23:52", substrings.get(3).get(6), "Третья строка содержит endTime на седьмом месте");
        assertEquals("PT2H", substrings.get(3).get(7), "Третья строка содержит duration на восьмом месте");

        assertEquals("4", substrings.get(4).get(0), "Четвёртая строка содержит id на первом месте");
        assertEquals("EPIC", substrings.get(4).get(1), "Четвёртая строка содержит type на втором месте");
        assertEquals("EpicName1", substrings.get(4).get(2), "Четвёртая строка содержит name на третьем месте");
        assertEquals("NEW", substrings.get(4).get(3), "Четвёртая строка содержит status на четвёртом месте");
        assertEquals("EpicDescription1", substrings.get(4).get(4), "Четвёртая строка содержит description на пятом месте");
        assertEquals("04.03.2025 21:52", substrings.get(4).get(5), "Четвёртая строка содержит startTime на шестом месте");
        assertEquals("04.03.2025 23:52", substrings.get(4).get(6), "Четвёртая строка содержит endTime на седьмом месте");
        assertEquals("PT2H", substrings.get(4).get(7), "Четвёртая строка содержит duration на восьмом месте");

        assertEquals("5", substrings.get(5).get(0), "Пятая строка содержит id на первом месте");
        assertEquals("EPIC", substrings.get(5).get(1), "Пятая строка содержит type на втором месте");
        assertEquals("EpicName2", substrings.get(5).get(2), "Пятая строка содержит name на третьем месте");
        assertEquals("IN_PROGRESS", substrings.get(5).get(3), "Пятая строка содержит status на четвёртом месте");
        assertEquals("EpicDescription2", substrings.get(5).get(4), "Пятая строка содержит description на пятом месте");
        assertEquals("04.03.2025 21:52", substrings.get(5).get(5), "Пятая строка содержит startTime на шестом месте");
        assertEquals("04.03.2025 23:52", substrings.get(5).get(6), "Пятая строка содержит endTime на седьмом месте");
        assertEquals("PT2H", substrings.get(5).get(7), "Пятая строка содержит duration на восьмом месте");

        assertEquals("6", substrings.get(6).get(0), "Шестая строка содержит id на первом месте");
        assertEquals("EPIC", substrings.get(6).get(1), "Шестая строка содержит type на втором месте");
        assertEquals("EpicName3", substrings.get(6).get(2), "Шестая строка содержит name на третьем месте");
        assertEquals("DONE", substrings.get(6).get(3), "Шестая строка содержит status на четвёртом месте");
        assertEquals("EpicDescription3", substrings.get(6).get(4), "Шестая строка содержит description на пятом месте");
        assertEquals("04.03.2025 21:52", substrings.get(6).get(5), "Шестая строка содержит startTime на шестом месте");
        assertEquals("04.03.2025 23:52", substrings.get(6).get(6), "Шестая строка содержит endTime на седьмом месте");
        assertEquals("PT2H", substrings.get(6).get(7), "Шестая строка содержит duration на восьмом месте");

        assertEquals("7", substrings.get(7).get(0), "Седьмая строка содержит id на первом месте");
        assertEquals("SUBTASK", substrings.get(7).get(1), "Седьмая строка содержит type на втором месте");
        assertEquals("SubTaskName1", substrings.get(7).get(2), "Седьмая строка содержит name на третьем месте");
        assertEquals("NEW", substrings.get(7).get(3), "Седьмая строка содержит status на четвёртом месте");
        assertEquals("SubTaskDescription1", substrings.get(7).get(4), "Седьмая строка содержит description на пятом месте");
        assertEquals("04.03.2025 21:52", substrings.get(7).get(5), "Седьмая строка содержит startTime на шестом месте");
        assertEquals("04.03.2025 23:52", substrings.get(7).get(6), "Седьмая строка содержит endTime на седьмом месте");
        assertEquals("PT2H", substrings.get(7).get(7), "Седьмая строка содержит duration на восьмом месте");
        assertEquals("4", substrings.get(7).get(8), "Седьмая строка содержит epicId на девятом месте");

        assertEquals("8", substrings.get(8).get(0), "Восьмая строка содержит id на первом месте");
        assertEquals("SUBTASK", substrings.get(8).get(1), "Восьмая строка содержит type на втором месте");
        assertEquals("SubTaskName2", substrings.get(8).get(2), "Восьмая строка содержит name на третьем месте");
        assertEquals("IN_PROGRESS", substrings.get(8).get(3), "Восьмая строка содержит status на четвёртом месте");
        assertEquals("SubTaskDescription2", substrings.get(8).get(4), "Восьмая строка содержит description на пятом месте");
        assertEquals("04.03.2025 21:52", substrings.get(8).get(5), "Восьмая строка содержит startTime на шестом месте");
        assertEquals("04.03.2025 23:52", substrings.get(8).get(6), "Восьмая строка содержит endTime на седьмом месте");
        assertEquals("PT2H", substrings.get(8).get(7), "Восьмая строка содержит duration на восьмом месте");
        assertEquals("5", substrings.get(8).get(8), "Восьмая строка содержит epicId на девятом месте");

        assertEquals("9", substrings.get(9).get(0), "Девятая строка содержит id на первом месте");
        assertEquals("SUBTASK", substrings.get(9).get(1), "Девятая строка содержит type на втором месте");
        assertEquals("SubTaskName3", substrings.get(9).get(2), "Девятая строка содержит name на третьем месте");
        assertEquals("DONE", substrings.get(9).get(3), "Девятая строка содержит status на четвёртом месте");
        assertEquals("SubTaskDescription3", substrings.get(9).get(4), "Девятая строка содержит description на пятом месте");
        assertEquals("04.03.2025 21:52", substrings.get(9).get(5), "Девятая строка содержит startTime на шестом месте");
        assertEquals("04.03.2025 23:52", substrings.get(9).get(6), "Девятая строка содержит endTime на седьмом месте");
        assertEquals("PT2H", substrings.get(9).get(7), "Девятая строка содержит duration на восьмом месте");
        assertEquals("6", substrings.get(9).get(8), "Девятая строка содержит epicId на девятом месте");

        tearDownAfterFileBackedTests();
    }
}