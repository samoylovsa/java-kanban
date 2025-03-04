package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private TaskManager taskManager;
    private LocalDateTime startTime = LocalDateTime.now();
    private Duration duration = Duration.ofMinutes(120);

    @BeforeEach
    void beforeEach() {
        taskManager = Manager.getDefault();
    }

    @Test
    void shouldIncreaseIdByOneForAllEntities() {
        int expectedFirstTaskId = 1;
        int actualFirstTaskId = taskManager.createTask(new Task("Name", "Description", Status.NEW, startTime, duration));

        assertEquals(expectedFirstTaskId, actualFirstTaskId);

        int expectedSecondTaskId = 2;
        int actualSecondTaskId = taskManager.createTask(new Task("Name", "Description", Status.NEW, startTime, duration));

        assertEquals(expectedSecondTaskId, actualSecondTaskId);

        int expectedFirstEpicId = 3;
        int actualFirstEpicId = taskManager.createEpic(new Epic("Name", "Description"));

        assertEquals(expectedFirstEpicId, actualFirstEpicId);

        int expectedSecondEpicId = 4;
        int actualSecondEpicId = taskManager.createEpic(new Epic("Name", "Description"));

        assertEquals(expectedSecondEpicId, actualSecondEpicId);

        int expectedFirstSubTaskId = 5;
        int actualFirstSubTaskId = taskManager.createSubTask(new SubTask("Name", "Description", Status.NEW, actualFirstEpicId, startTime, duration));

        assertEquals(expectedFirstSubTaskId, actualFirstSubTaskId);

        int expectedSecondSubTaskId = 6;
        int actualSecondSubTaskId = taskManager.createSubTask(new SubTask("Name", "Description", Status.NEW, actualSecondEpicId, startTime, duration));

        assertEquals(expectedSecondSubTaskId, actualSecondSubTaskId);
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
    void shouldBeNegativeWhenCreateSubTaskWithNonExistEpicId() {
        int nonExistEpicId = 54321;
        int subTaskId = taskManager.createSubTask(new SubTask("Name", "Description", Status.NEW, nonExistEpicId, startTime, duration));

        assertTrue(subTaskId < 0);
    }

    @Test
    void shouldBeTrueAfterSuccessUpdatingTask() {
        Task originalTask = new Task("OriginalName", "OriginalDescription", Status.NEW, startTime, duration);
        int taskId = taskManager.createTask(originalTask);
        Task updatedTask = new Task("UpdatedName", "UpdatedDescription", Status.IN_PROGRESS, startTime, duration);
        updatedTask.setId(taskId);

        boolean isUpdated = taskManager.updateTask(updatedTask);

        assertTrue(isUpdated);
    }

    @Test
    void shouldBeFalseAfterUpdatingNonExistingTask() {
        int nonExistingTaskId = 54321;
        Task nonExistingTask = new Task("Name", "Description", Status.IN_PROGRESS, startTime, duration);
        nonExistingTask.setId(nonExistingTaskId);

        boolean isUpdated = taskManager.updateTask(nonExistingTask);

        assertFalse(isUpdated);
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
    void shouldBeFalseAfterUpdatingNonExistingEpic() {
        int nonExistingEpicId = 54321;
        Epic nonExistingEpic = new Epic("Name", "Description");
        nonExistingEpic.setId(nonExistingEpicId);

        boolean isUpdated = taskManager.updateEpic(nonExistingEpic);

        assertFalse(isUpdated);
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
    void shouldBeFalseAfterUpdatingNonExistingSubTask() {
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        int nonExistingSubTaskId = 54321;
        SubTask nonExistingSubTask = new SubTask("Name", "Description", Status.IN_PROGRESS, epicId, startTime, duration);
        nonExistingSubTask.setId(nonExistingSubTaskId);

        boolean isUpdated = taskManager.updateSubTask(nonExistingSubTask);

        assertFalse(isUpdated);
    }

    @Test
    void shouldBeFalseAfterUpdatingSubTaskAndEpicNotExist() {
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        SubTask originalSubTask = new SubTask("OriginalName", "OriginalDescription", Status.NEW, epicId, startTime, duration);
        int subTaskId = taskManager.createSubTask(originalSubTask);
        int nonExistingEpicId = 54321;
        SubTask nonExistingSubTask = new SubTask("Name", "Description", Status.IN_PROGRESS, nonExistingEpicId, startTime, duration);
        nonExistingSubTask.setId(subTaskId);

        boolean isUpdated = taskManager.updateSubTask(nonExistingSubTask);

        assertFalse(isUpdated);
    }

    @Test
    void shouldBeFalseAfterUpdatingSubTaskAndEpicNotContain() {
        int epicId = taskManager.createEpic(new Epic("FirstName", "FirstDescription"));
        SubTask originalSubTask = new SubTask("OriginalName", "OriginalDescription", Status.NEW, epicId, startTime, duration);
        int subTaskId = taskManager.createSubTask(originalSubTask);

        int otherEpicId = taskManager.createEpic(new Epic("OtherName", "OtherDescription"));
        SubTask updatedSubTask = new SubTask("UpdatedName", "UpdatedDescription", Status.IN_PROGRESS, otherEpicId, startTime, duration);
        updatedSubTask.setId(subTaskId);

        boolean isUpdated = taskManager.updateSubTask(updatedSubTask);

        assertFalse(isUpdated);
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
        SubTask secondSubTask = new SubTask("SecondName", "SecondDescription", Status.NEW, epicId, startTime, duration);
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
        Task secondTask = new Task("SecondName", "SecondDescription", Status.IN_PROGRESS, startTime, duration);
        Task thirdTask = new Task("ThirdName", "ThirdDescription", Status.DONE, startTime, duration);
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
        SubTask secondSubTask = new SubTask("SecondName", "SecondDescription", Status.IN_PROGRESS, epicId, startTime, duration);
        SubTask thirdSubTask = new SubTask("ThirdName", "ThirdDescription", Status.DONE, epicId, startTime, duration);
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
        Task secondTask = new Task("SecondName", "SecondDescription", Status.IN_PROGRESS, startTime, duration);
        Task thirdTask = new Task("ThirdName", "ThirdDescription", Status.DONE, startTime, duration);
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
        SubTask secondSubTask = new SubTask("SecondName", "SecondDescription", Status.IN_PROGRESS, idOfFirstEpic, startTime, duration);
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
        SubTask secondSubTask = new SubTask("SecondName", "SecondDescription", Status.IN_PROGRESS, epicId, startTime, duration);
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
        Task secondTask = new Task("SecondName", "SecondDescription", Status.IN_PROGRESS, startTime, duration);
        Task thirdTask = new Task("ThirdName", "ThirdDescription", Status.DONE, startTime, duration);
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
        SubTask secondSubTask = new SubTask("SecondName", "SecondDescription", Status.IN_PROGRESS, epicId, startTime, duration);
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
        SubTask secondSubTask = new SubTask("SecondName", "SecondDescription", Status.IN_PROGRESS, epicId, startTime, duration);
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
        SubTask secondSubTask = new SubTask("SecondName", "SecondDescription", Status.DONE, idOfSecondEpic, startTime, duration);
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
        taskManager.createSubTask(new SubTask("SecondName", "SecondDescription", Status.DONE, epicId, startTime, duration));

        taskManager.deleteAllSubTasks();
        ArrayList<Integer> actualSubTaskIdList = taskManager.getEpic(epicId).getSubTaskIdList();

        assertTrue(actualSubTaskIdList.isEmpty());
    }

    @Test
    void shouldBeNullIfEpicIdNotExist() {
        int nonExistEpicId = 54321;

        List<SubTask> actualSubTasksByEpicId = taskManager.getSubTasksByEpic(nonExistEpicId);

        assertNull(actualSubTasksByEpicId);
    }

    @Test
    void shouldReturnSubTasksByEpicId() {
        int epicId = taskManager.createEpic(new Epic("FirstName", "FirstDescription"));
        SubTask firstSubTask = new SubTask("FirstName", "FirstDescription", Status.IN_PROGRESS, epicId, startTime, duration);
        SubTask secondSubTask = new SubTask("SecondName", "SecondDescription", Status.DONE, epicId, startTime, duration);
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
        int subTaskId = taskManager.createSubTask(new SubTask("SubTaskName", "SubTaskDescription", Status.NEW, epicId, startTime, duration));

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
}