package manager;

import org.junit.jupiter.api.AfterEach;
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
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileBackedTaskManagerTest {

    private File file;
    private TaskManager taskManager;
    private BufferedReader bufferedReader;

    @BeforeEach
    void setUp() {
        try {
            file = File.createTempFile("test", ".csv");
            taskManager = Manager.getFileBacked(file);
            bufferedReader = new BufferedReader(new FileReader(file));
        } catch (IOException exception) {
            System.out.println("Ошибка при создании файла для теста");
        }
    }

    @AfterEach
    void tearDown() {
        try {
            bufferedReader.close();
        } catch (IOException exception) {
            System.out.println("Ошибка закрытия потока после теста");
        }
    }

    @Test
    public void firstLineShouldHaveTitle() throws IOException {
        String title = "id,type,name,status,description,epic";

        Task task = new Task("Name", "Description", Status.NEW);
        taskManager.createTask(task);

        String firstLine = bufferedReader.readLine();

        assertEquals(title, firstLine);
    }

    @Test
    public void tasksShouldBeWrittenInFileAfterCreate() throws IOException {
        Task firstTask = new Task("TaskName1", "TaskDescription1", Status.NEW);
        Task secondTask = new Task("TaskName2", "TaskDescription2", Status.IN_PROGRESS);
        Task thirdTask = new Task("TaskName3", "TaskDescription3", Status.DONE);
        Epic firstEpic = new Epic("EpicName1", "EpicDescription1");
        Epic secondEpic = new Epic("EpicName2", "EpicDescription2");
        Epic thirdEpic = new Epic("EpicName3", "EpicDescription3");
        int firstTaskId = taskManager.createTask(firstTask);
        int secondTaskId = taskManager.createTask(secondTask);
        int thirdTaskId = taskManager.createTask(thirdTask);
        int firstEpicId = taskManager.createEpic(firstEpic);
        int secondEpicId = taskManager.createEpic(secondEpic);
        int thirdEpicId = taskManager.createEpic(thirdEpic);
        SubTask firstSubTask = new SubTask("SubTaskName1", "SubTaskDescription1", Status.NEW, firstEpicId);
        SubTask secondSubTask = new SubTask("SubTaskName2", "SubTaskDescription2", Status.IN_PROGRESS, secondEpicId);
        SubTask thirdSubTask = new SubTask("SubTaskName3", "SubTaskDescription3", Status.DONE, thirdEpicId);
        int firstSubTaskId = taskManager.createSubTask(firstSubTask);
        int secondSubTaskId = taskManager.createSubTask(secondSubTask);
        int thirdSubTaskId = taskManager.createSubTask(thirdSubTask);

        bufferedReader.readLine();
        HashMap<Integer, List<String>> substrings = new HashMap<>(9);
        String line;
        int lineNumber = 0;
        while ((line = bufferedReader.readLine()) != null) {
            substrings.put(++lineNumber, List.of(line.split(",")));
        }

        assertEquals(10, substrings.size() + 1, "Файл должен содержать 10 строк");

        assertEquals("1", substrings.get(1).get(0), "Первая строка содержит id на первом месте");
        assertEquals("TASK", substrings.get(1).get(1), "Первая строка содержит type на втором месте");
        assertEquals("TaskName1", substrings.get(1).get(2), "Первая строка содержит name на третьем месте");
        assertEquals("NEW", substrings.get(1).get(3), "Первая строка содержит status на четвёртом месте");
        assertEquals("TaskDescription1", substrings.get(1).get(4), "Первая строка содержит description на пятом месте");

        assertEquals("2", substrings.get(2).get(0), "Вторая строка содержит id на первом месте");
        assertEquals("TASK", substrings.get(2).get(1), "Вторая строка содержит type на втором месте");
        assertEquals("TaskName2", substrings.get(2).get(2), "Вторая строка содержит name на третьем месте");
        assertEquals("IN_PROGRESS", substrings.get(2).get(3), "Вторая строка содержит status на четвёртом месте");
        assertEquals("TaskDescription2", substrings.get(2).get(4), "Вторая строка содержит description на пятом месте");

        assertEquals("3", substrings.get(3).get(0), "Третья строка содержит id на первом месте");
        assertEquals("TASK", substrings.get(3).get(1), "Третья строка содержит type на втором месте");
        assertEquals("TaskName3", substrings.get(3).get(2), "Третья строка содержит name на третьем месте");
        assertEquals("DONE", substrings.get(3).get(3), "Третья строка содержит status на четвёртом месте");
        assertEquals("TaskDescription3", substrings.get(3).get(4), "Третья строка содержит description на пятом месте");

        assertEquals("4", substrings.get(4).get(0), "Четвёртая строка содержит id на первом месте");
        assertEquals("EPIC", substrings.get(4).get(1), "Четвёртая строка содержит type на втором месте");
        assertEquals("EpicName1", substrings.get(4).get(2), "Четвёртая строка содержит name на третьем месте");
        assertEquals("NEW", substrings.get(4).get(3), "Четвёртая строка содержит status на четвёртом месте");
        assertEquals("EpicDescription1", substrings.get(4).get(4), "Четвёртая строка содержит description на пятом месте");

        assertEquals("5", substrings.get(5).get(0), "Пятая строка содержит id на первом месте");
        assertEquals("EPIC", substrings.get(5).get(1), "Пятая строка содержит type на втором месте");
        assertEquals("EpicName2", substrings.get(5).get(2), "Пятая строка содержит name на третьем месте");
        assertEquals("IN_PROGRESS", substrings.get(5).get(3), "Пятая строка содержит status на четвёртом месте");
        assertEquals("EpicDescription2", substrings.get(5).get(4), "Пятая строка содержит description на пятом месте");

        assertEquals("6", substrings.get(6).get(0), "Шестая строка содержит id на первом месте");
        assertEquals("EPIC", substrings.get(6).get(1), "Шестая строка содержит type на втором месте");
        assertEquals("EpicName3", substrings.get(6).get(2), "Шестая строка содержит name на третьем месте");
        assertEquals("DONE", substrings.get(6).get(3), "Шестая строка содержит status на четвёртом месте");
        assertEquals("EpicDescription3", substrings.get(6).get(4), "Шестая строка содержит description на пятом месте");

        assertEquals("7", substrings.get(7).get(0), "Седьмая строка содержит id на первом месте");
        assertEquals("SUBTASK", substrings.get(7).get(1), "Седьмая строка содержит type на втором месте");
        assertEquals("SubTaskName1", substrings.get(7).get(2), "Седьмая строка содержит name на третьем месте");
        assertEquals("NEW", substrings.get(7).get(3), "Седьмая строка содержит status на четвёртом месте");
        assertEquals("SubTaskDescription1", substrings.get(7).get(4), "Седьмая строка_contains description на пятом месте");
        assertEquals("4", substrings.get(7).get(5), "Седьмая строка_contains parent ID на шестом месте");

        assertEquals("8", substrings.get(8).get(0), "Восьмая строка contains id на первом месте");
        assertEquals("SUBTASK", substrings.get(8).get(1), "Восьмая строка contains type на втором месте");
        assertEquals("SubTaskName2", substrings.get(8).get(2), "Восьмая строка contains name на третьем месте");
        assertEquals("IN_PROGRESS", substrings.get(8).get(3), "Восьмая строка contains status на четвёртом месте");
        assertEquals("SubTaskDescription2", substrings.get(8).get(4), "Восьмая строка_contains description на пятом месте");
        assertEquals("5", substrings.get(8).get(5), "Восьмая строка_contains parent ID на шестом месте");

        assertEquals("9", substrings.get(9).get(0), "Девятая строка contains id на первом месте");
        assertEquals("SUBTASK", substrings.get(9).get(1), "Девятая строка contains type на втором месте");
        assertEquals("SubTaskName3", substrings.get(9).get(2), "Девятая строка contains name на третьем месте");
        assertEquals("DONE", substrings.get(9).get(3), "Девятая строка contains status на четвёртом месте");
        assertEquals("SubTaskDescription3", substrings.get(9).get(4), "Девятая строка_contains description на пятом месте");
        assertEquals("6", substrings.get(9).get(5), "Девятая строка_contains parent ID на шестом месте");
    }

}