package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private TaskManager taskManager;

    @BeforeEach
    void beforeEach() {
        taskManager = Manager.getDefault();
    }

    @Test
    void shouldIncreaseIdByOneForAllEntities() {
        int expectedFirstTaskId = 1;
        int actualFirstTaskId = taskManager.createTask(new Task("Name", "Description", Status.NEW));

        assertEquals(expectedFirstTaskId, actualFirstTaskId);

        int expectedSecondTaskId = 2;
        int actualSecondTaskId = taskManager.createTask(new Task("Name", "Description", Status.NEW));

        assertEquals(expectedSecondTaskId, actualSecondTaskId);

        int expectedFirstEpicId = 3;
        int actualFirstEpicId = taskManager.createEpic(new Epic("Name", "Description"));

        assertEquals(expectedFirstEpicId, actualFirstEpicId);

        int expectedSecondEpicId = 4;
        int actualSecondEpicId = taskManager.createEpic(new Epic("Name", "Description"));

        assertEquals(expectedSecondEpicId, actualSecondEpicId);

        int expectedFirstSubTaskId = 5;
        int actualFirstSubTaskId = taskManager.createSubTask(new SubTask("Name", "Description", Status.NEW, actualFirstEpicId));

        assertEquals(expectedFirstSubTaskId, actualFirstSubTaskId);

        int expectedSecondSubTaskId = 6;
        int actualSecondSubTaskId = taskManager.createSubTask(new SubTask("Name", "Description", Status.NEW, actualSecondEpicId));

        assertEquals(expectedSecondSubTaskId, actualSecondSubTaskId);
    }

    @Test
    void taskFromTaskManagerShouldBeEqualToOriginal() {
        Task expectedTask = new Task("Name", "Description", Status.NEW);
        int taskId = taskManager.createTask(expectedTask);
        Task actualTask = taskManager.getTask(taskId);

        assertEquals(expectedTask, actualTask);
    }

    @Test
    void subTaskFromTaskManagerShouldBeEqualToOriginal() {
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        SubTask expectedSubTask = new SubTask("Name", "Description", Status.NEW, epicId);
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
        int subTaskId = taskManager.createSubTask(new SubTask("Name", "Description", Status.NEW, nonExistEpicId));

        assertTrue(subTaskId < 0);
    }

    @Test
    void shouldBeTrueAfterSuccessUpdatingTask() {
        Task originalTask = new Task("OriginalName", "OriginalDescription", Status.NEW);
        int taskId = taskManager.createTask(originalTask);
        Task updatedTask = new Task("UpdatedName", "UpdatedDescription", Status.IN_PROGRESS);
        updatedTask.setId(taskId);

        boolean isUpdated = taskManager.updateTask(updatedTask);

        assertTrue(isUpdated);
    }

    @Test
    void shouldBeFalseAfterUpdatingNonExistingTask() {
        int nonExistingTaskId = 54321;
        Task nonExistingTask = new Task("Name", "Description", Status.IN_PROGRESS);
        nonExistingTask.setId(nonExistingTaskId);

        boolean isUpdated = taskManager.updateTask(nonExistingTask);

        assertFalse(isUpdated);
    }

    @Test
    void taskFromOriginalTaskManagerShouldBeEqualAfterUpdating() {
        Task originalTask = new Task("OriginalName", "OriginalDescription", Status.NEW);
        int taskId = taskManager.createTask(originalTask);
        Task updatedTask = new Task("UpdatedName", "UpdatedDescription", Status.IN_PROGRESS);
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
        SubTask originalSubTask = new SubTask("OriginalName", "OriginalDescription", Status.NEW, epicId);
        int subTaskId = taskManager.createSubTask(originalSubTask);
        SubTask updatedSubTask = new SubTask("UpdatedName", "UpdatedDescription", Status.IN_PROGRESS, epicId);
        updatedSubTask.setId(subTaskId);

        boolean isUpdated = taskManager.updateSubTask(updatedSubTask);

        assertTrue(isUpdated);
    }

    @Test
    void shouldBeFalseAfterUpdatingNonExistingSubTask() {
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        int nonExistingSubTaskId = 54321;
        SubTask nonExistingSubTask = new SubTask("Name", "Description", Status.IN_PROGRESS, epicId);
        nonExistingSubTask.setId(nonExistingSubTaskId);

        boolean isUpdated = taskManager.updateSubTask(nonExistingSubTask);

        assertFalse(isUpdated);
    }

    @Test
    void shouldBeFalseAfterUpdatingSubTaskAndEpicNotExist() {
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        SubTask originalSubTask = new SubTask("OriginalName", "OriginalDescription", Status.NEW, epicId);
        int subTaskId = taskManager.createSubTask(originalSubTask);
        int nonExistingEpicId = 54321;
        SubTask nonExistingSubTask = new SubTask("Name", "Description", Status.IN_PROGRESS, nonExistingEpicId);
        nonExistingSubTask.setId(subTaskId);

        boolean isUpdated = taskManager.updateSubTask(nonExistingSubTask);

        assertFalse(isUpdated);
    }

    @Test
    void shouldBeFalseAfterUpdatingSubTaskAndEpicNotContain() {
        int epicId = taskManager.createEpic(new Epic("FirstName", "FirstDescription"));
        SubTask originalSubTask = new SubTask("OriginalName", "OriginalDescription", Status.NEW, epicId);
        int subTaskId = taskManager.createSubTask(originalSubTask);

        int otherEpicId = taskManager.createEpic(new Epic("OtherName", "OtherDescription"));
        SubTask updatedSubTask = new SubTask("UpdatedName", "UpdatedDescription", Status.IN_PROGRESS, otherEpicId);
        updatedSubTask.setId(subTaskId);

        boolean isUpdated = taskManager.updateSubTask(updatedSubTask);

        assertFalse(isUpdated);
    }

    @Test
    void subTaskFromOriginalTaskManagerShouldBeEqualAfterUpdating() {
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        SubTask originalSubTask = new SubTask("OriginalName", "OriginalDescription", Status.NEW, epicId);
        int subTaskId = taskManager.createSubTask(originalSubTask);
        SubTask updatedSubTask = new SubTask("UpdatedName", "UpdatedDescription", Status.IN_PROGRESS, epicId);
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
        SubTask originalSubTask = new SubTask("OriginalName", "OriginalDescription", Status.IN_PROGRESS, epicId);
        taskManager.createSubTask(originalSubTask);

        Status actualStatusAfterAdding = taskManager.getEpic(epicId).getStatus();
        Status expectedStatusAfterAdding = Status.IN_PROGRESS;

        assertEquals(expectedStatusAfterAdding, actualStatusAfterAdding);
    }

    @Test
    void epicStatusShouldChangeAfterSubTaskDeleting() {
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        SubTask originalSubTask = new SubTask("OriginalName", "OriginalDescription", Status.IN_PROGRESS, epicId);
        int subTaskId = taskManager.createSubTask(originalSubTask);
        taskManager.deleteSubTask(subTaskId);

        Status actualStatusAfterDeleting = taskManager.getEpic(epicId).getStatus();
        Status expectedStatusAfterDeleting = Status.NEW;

        assertEquals(expectedStatusAfterDeleting, actualStatusAfterDeleting);
    }

    @Test
    void epicStatusShouldChangeAfterSubTasksStatusChanging() {
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        SubTask firstSubTask = new SubTask("FirstName", "FirstDescription", Status.NEW, epicId);
        SubTask secondSubTask = new SubTask("SecondName", "SecondDescription", Status.NEW, epicId);
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
        Task firstTask = new Task("FirstName", "FirstDescription", Status.NEW);
        Task secondTask = new Task("SecondName", "SecondDescription", Status.IN_PROGRESS);
        Task thirdTask = new Task("ThirdName", "ThirdDescription", Status.DONE);
        ArrayList<Task> expectedTasks = new ArrayList<>(Arrays.asList(firstTask, secondTask, thirdTask));
        for (Task task : expectedTasks) {
            taskManager.createTask(task);
        }

        ArrayList<Task> actualTasks = taskManager.getTasks();

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

        ArrayList<Epic> actualEpics = taskManager.getEpics();

        assertEquals(expectedEpics, actualEpics);
    }

    @Test
    void shouldReturnAllCreatedSubTasks() {
        int epicId = taskManager.createEpic(new Epic("FirstName", "FirstDescription"));
        SubTask firstSubTask = new SubTask("FirstName", "FirstDescription", Status.NEW, epicId);
        SubTask secondSubTask = new SubTask("SecondName", "SecondDescription", Status.IN_PROGRESS, epicId);
        SubTask thirdSubTask = new SubTask("ThirdName", "ThirdDescription", Status.DONE, epicId);
        ArrayList<SubTask> expectedSubTasks = new ArrayList<>(Arrays.asList(firstSubTask, secondSubTask, thirdSubTask));
        for (SubTask subTask : expectedSubTasks) {
            taskManager.createSubTask(subTask);
        }

        ArrayList<SubTask> actualSubTasks = taskManager.getSubTasks();

        assertEquals(expectedSubTasks, actualSubTasks);
    }

    @Test
    void shouldReturnTasksListWithoutDeletedTask() {
        HashMap<Integer, Task> originalTasks = new HashMap<>();
        Task firstTask = new Task("FirstName", "FirstDescription", Status.NEW);
        Task secondTask = new Task("SecondName", "SecondDescription", Status.IN_PROGRESS);
        Task thirdTask = new Task("ThirdName", "ThirdDescription", Status.DONE);
        originalTasks.put(1, firstTask);
        originalTasks.put(2, secondTask);
        originalTasks.put(3, thirdTask);
        int taskIdForDeletion = 2;

        for (Integer key : originalTasks.keySet()) {
            taskManager.createTask(originalTasks.get(key));
        }

        ArrayList<Task> tasksBeforeDeletion = taskManager.getTasks();
        int actualSizeBeforeDeletion = tasksBeforeDeletion.size();
        int expectedSizeBeforeDeletion = 3;

        assertEquals(expectedSizeBeforeDeletion, actualSizeBeforeDeletion);

        taskManager.deleteTask(taskIdForDeletion);

        ArrayList<Task> actualTasksAfterDeletion = taskManager.getTasks();
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
        SubTask firstSubTask = new SubTask("FirstName", "FirstDescription", Status.NEW, idOfFirstEpic);
        SubTask secondSubTask = new SubTask("SecondName", "SecondDescription", Status.IN_PROGRESS, idOfFirstEpic);
        ArrayList<SubTask> originalSubTasks = new ArrayList<>(Arrays.asList(firstSubTask, secondSubTask));
        for (SubTask subTask : originalSubTasks) {
            taskManager.createSubTask(subTask);
        }

        taskManager.deleteEpic(idOfFirstEpic);

        ArrayList<Epic> epicsAfterDeletion = taskManager.getEpics();
        ArrayList<SubTask> subTasksAfterDeletion = taskManager.getSubTasks();
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
        SubTask firstSubTask = new SubTask("FirstName", "FirstDescription", Status.NEW, epicId);
        SubTask secondSubTask = new SubTask("SecondName", "SecondDescription", Status.IN_PROGRESS, epicId);
        ArrayList<SubTask> originalSubTasks = new ArrayList<>(Arrays.asList(firstSubTask, secondSubTask));
        for (SubTask subTask : originalSubTasks) {
            taskManager.createSubTask(subTask);
        }

        taskManager.deleteSubTask(2);

        ArrayList<SubTask> subTasksAfterDeletion = taskManager.getSubTasks();
        originalSubTasks.remove(0);
        ArrayList<SubTask> expectedSubTasksAfterDeletion = originalSubTasks;
        int actualSubTasksSize = subTasksAfterDeletion.size();
        int expectedSubTasksSize = 1;

        assertEquals(expectedSubTasksSize, actualSubTasksSize);
        assertEquals(expectedSubTasksAfterDeletion, subTasksAfterDeletion);
    }

    @Test
    void shouldBeEmptyTasksAfterDeletingAllTasks() {
        Task firstTask = new Task("FirstName", "FirstDescription", Status.NEW);
        Task secondTask = new Task("SecondName", "SecondDescription", Status.IN_PROGRESS);
        Task thirdTask = new Task("ThirdName", "ThirdDescription", Status.DONE);
        ArrayList<Task> originalTasks = new ArrayList<>(Arrays.asList(firstTask, secondTask, thirdTask));
        for (Task task : originalTasks) {
            taskManager.createTask(task);
        }

        taskManager.deleteAllTasks();
        ArrayList<Task> actualTasks = taskManager.getTasks();

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
        SubTask firstSubTask = new SubTask("FirstName", "FirstDescription", Status.NEW, epicId);
        SubTask secondSubTask = new SubTask("SecondName", "SecondDescription", Status.IN_PROGRESS, epicId);
        ArrayList<SubTask> originalSubTasks = new ArrayList<>(Arrays.asList(firstSubTask, secondSubTask));
        for (SubTask subTask : originalSubTasks) {
            taskManager.createSubTask(subTask);
        }

        taskManager.deleteAllEpics();
        ArrayList<Epic> actualEpics = taskManager.getEpics();
        ArrayList<SubTask> actualSubTasks = taskManager.getSubTasks();

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
        SubTask firstSubTask = new SubTask("FirstName", "FirstDescription", Status.NEW, epicId);
        SubTask secondSubTask = new SubTask("SecondName", "SecondDescription", Status.IN_PROGRESS, epicId);
        ArrayList<SubTask> originalSubTasks = new ArrayList<>(Arrays.asList(firstSubTask, secondSubTask));
        for (SubTask subTask : originalSubTasks) {
            taskManager.createSubTask(subTask);
        }

        taskManager.deleteAllSubTasks();
        ArrayList<SubTask> actualSubTasks = taskManager.getSubTasks();

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
        SubTask firstSubTask = new SubTask("FirstName", "FirstDescription", Status.IN_PROGRESS, idOfFirstEpic);
        SubTask secondSubTask = new SubTask("SecondName", "SecondDescription", Status.DONE, idOfSecondEpic);
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
        taskManager.createSubTask(new SubTask("FirstName", "FirstDescription", Status.IN_PROGRESS, epicId));
        taskManager.createSubTask(new SubTask("SecondName", "SecondDescription", Status.DONE, epicId));

        taskManager.deleteAllSubTasks();
        ArrayList<Integer> actualSubTaskIdList = taskManager.getEpic(epicId).getSubTaskIdList();

        assertTrue(actualSubTaskIdList.isEmpty());
    }

    @Test
    void shouldBeNullIfEpicIdNotExist() {
        int nonExistEpicId = 54321;

        ArrayList<SubTask> actualSubTasksByEpicId = taskManager.getSubTasksByEpic(nonExistEpicId);

        assertNull(actualSubTasksByEpicId);
    }

    @Test
    void shouldReturnSubTasksByEpicId() {
        int epicId = taskManager.createEpic(new Epic("FirstName", "FirstDescription"));
        SubTask firstSubTask = new SubTask("FirstName", "FirstDescription", Status.IN_PROGRESS, epicId);
        SubTask secondSubTask = new SubTask("SecondName", "SecondDescription", Status.DONE, epicId);
        ArrayList<SubTask> originalSubTasks = new ArrayList<>(Arrays.asList(firstSubTask, secondSubTask));
        for (SubTask subTask : originalSubTasks) {
            taskManager.createSubTask(subTask);
        }

        ArrayList<SubTask> actualSubTasksByEpicId = taskManager.getSubTasksByEpic(epicId);
        ArrayList<SubTask> expectedSubTasks = taskManager.getSubTasks();

        assertEquals(expectedSubTasks, actualSubTasksByEpicId);
    }
}