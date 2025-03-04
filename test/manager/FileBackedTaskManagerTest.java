package manager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;
import utils.CSVTaskFormatUtils;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileBackedTaskManagerTest {

    private File file;
    private TaskManager taskManager;
    private BufferedReader bufferedReader;
    private int firstTaskId;
    private int secondTaskId;
    private int thirdTaskId;
    private int firstEpicId;
    private int secondEpicId;
    private int thirdEpicId;
    private int firstSubTaskId;
    private int secondSubTaskId;
    private int thirdSubTaskId;
    private LocalDateTime startTime = LocalDateTime.parse(
            "04.03.2025 21:52",
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    );
    private Duration duration = Duration.ofMinutes(120);

    @BeforeEach
    void setUp() {
        try {
            file = File.createTempFile("test", ".csv");
            taskManager = Manager.getFileBacked(file);
            bufferedReader = new BufferedReader(new FileReader(file));

            Task firstTask = new Task("TaskName1", "TaskDescription1", Status.NEW, startTime, duration);
            Task secondTask = new Task("TaskName2", "TaskDescription2", Status.IN_PROGRESS, startTime, duration);
            Task thirdTask = new Task("TaskName3", "TaskDescription3", Status.DONE, startTime, duration);
            Epic firstEpic = new Epic("EpicName1", "EpicDescription1");
            Epic secondEpic = new Epic("EpicName2", "EpicDescription2");
            Epic thirdEpic = new Epic("EpicName3", "EpicDescription3");

            firstTaskId = taskManager.createTask(firstTask);
            secondTaskId = taskManager.createTask(secondTask);
            thirdTaskId = taskManager.createTask(thirdTask);
            firstEpicId = taskManager.createEpic(firstEpic);
            secondEpicId = taskManager.createEpic(secondEpic);
            thirdEpicId = taskManager.createEpic(thirdEpic);

            SubTask firstSubTask = new SubTask("SubTaskName1", "SubTaskDescription1", Status.NEW, firstEpicId, startTime, duration);
            SubTask secondSubTask = new SubTask("SubTaskName2", "SubTaskDescription2", Status.IN_PROGRESS, secondEpicId, startTime, duration);
            SubTask thirdSubTask = new SubTask("SubTaskName3", "SubTaskDescription3", Status.DONE, thirdEpicId, startTime, duration);

            firstSubTaskId = taskManager.createSubTask(firstSubTask);
            secondSubTaskId = taskManager.createSubTask(secondSubTask);
            thirdSubTaskId = taskManager.createSubTask(thirdSubTask);
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
        String title = "id,type,name,status,description,startTime,endTime,duration,epic";

        Task task = new Task("Name", "Description", Status.NEW, startTime, duration);
        taskManager.createTask(task);

        String firstLine = bufferedReader.readLine();

        assertEquals(title, firstLine);
    }

    @Test
    public void tasksShouldBeWrittenInFileAfterCreate() throws IOException {
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
    }

    @Test
    public void tasksShouldBeWrittenInFileAfterUpdate() throws IOException {
        Task firstTaskFromManager = taskManager.getTask(firstTaskId);
        firstTaskFromManager.setStatus(Status.DONE);
        taskManager.updateTask(firstTaskFromManager);
        Task secondTaskFromManager = taskManager.getTask(secondTaskId);
        secondTaskFromManager.setStatus(Status.DONE);
        taskManager.updateTask(secondTaskFromManager);

        SubTask firstSubTaskFromManager = taskManager.getSubTask(firstSubTaskId);
        firstSubTaskFromManager.setStatus(Status.DONE);
        taskManager.updateSubTask(firstSubTaskFromManager);
        SubTask secondSubTaskFromManager = taskManager.getSubTask(secondSubTaskId);
        secondSubTaskFromManager.setStatus(Status.DONE);
        taskManager.updateSubTask(secondSubTaskFromManager);

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
        assertEquals("DONE", substrings.get(1).get(3), "Первая строка содержит status на четвёртом месте");
        assertEquals("TaskDescription1", substrings.get(1).get(4), "Первая строка содержит description на пятом месте");
        assertEquals("04.03.2025 21:52", substrings.get(1).get(5), "Первая строка содержит startTime на шестом месте");
        assertEquals("04.03.2025 23:52", substrings.get(1).get(6), "Первая строка содержит endTime на седьмом месте");
        assertEquals("PT2H", substrings.get(1).get(7), "Первая строка содержит duration на восьмом месте");

        assertEquals("2", substrings.get(2).get(0), "Вторая строка содержит id на первом месте");
        assertEquals("TASK", substrings.get(2).get(1), "Вторая строка содержит type на втором месте");
        assertEquals("TaskName2", substrings.get(2).get(2), "Вторая строка содержит name на третьем месте");
        assertEquals("DONE", substrings.get(2).get(3), "Вторая строка содержит status на четвёртом месте");
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
        assertEquals("DONE", substrings.get(4).get(3), "Четвёртая строка содержит status на четвёртом месте");
        assertEquals("EpicDescription1", substrings.get(4).get(4), "Четвёртая строка содержит description на пятом месте");
        assertEquals("04.03.2025 21:52", substrings.get(4).get(5), "Четвёртая строка содержит startTime на шестом месте");
        assertEquals("04.03.2025 23:52", substrings.get(4).get(6), "Четвёртая строка содержит endTime на седьмом месте");
        assertEquals("PT2H", substrings.get(4).get(7), "Четвёртая строка содержит duration на восьмом месте");

        assertEquals("5", substrings.get(5).get(0), "Пятая строка содержит id на первом месте");
        assertEquals("EPIC", substrings.get(5).get(1), "Пятая строка содержит type на втором месте");
        assertEquals("EpicName2", substrings.get(5).get(2), "Пятая строка содержит name на третьем месте");
        assertEquals("DONE", substrings.get(5).get(3), "Пятая строка содержит status на четвёртом месте");
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
        assertEquals("DONE", substrings.get(7).get(3), "Седьмая строка содержит status на четвёртом месте");
        assertEquals("SubTaskDescription1", substrings.get(7).get(4), "Седьмая строка содержит description на пятом месте");
        assertEquals("04.03.2025 21:52", substrings.get(7).get(5), "Седьмая строка содержит startTime на шестом месте");
        assertEquals("04.03.2025 23:52", substrings.get(7).get(6), "Седьмая строка содержит endTime на седьмом месте");
        assertEquals("PT2H", substrings.get(7).get(7), "Седьмая строка содержит duration на восьмом месте");
        assertEquals("4", substrings.get(7).get(8), "Седьмая строка содержит epicId на девятом месте");

        assertEquals("8", substrings.get(8).get(0), "Восьмая строка содержит id на первом месте");
        assertEquals("SUBTASK", substrings.get(8).get(1), "Восьмая строка содержит type на втором месте");
        assertEquals("SubTaskName2", substrings.get(8).get(2), "Восьмая строка содержит name на третьем месте");
        assertEquals("DONE", substrings.get(8).get(3), "Восьмая строка содержит status на четвёртом месте");
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
    }

    @Test
    public void tasksShouldBeDeletedFromFileAfterDelete() throws IOException {
        taskManager.deleteTask(firstTaskId);
        taskManager.deleteTask(secondTaskId);
        taskManager.deleteSubTask(firstSubTaskId);
        taskManager.deleteSubTask(secondSubTaskId);
        taskManager.deleteEpic(firstEpicId);
        taskManager.deleteEpic(secondEpicId);

        bufferedReader.readLine();
        HashMap<Integer, List<String>> substrings = new HashMap<>(9);
        String line;
        int lineNumber = 0;
        while ((line = bufferedReader.readLine()) != null) {
            substrings.put(++lineNumber, List.of(line.split(",")));
        }

        assertEquals(4, substrings.size() + 1, "Файл должен содержать 4 строки (с заголовком)");

        assertEquals("3", substrings.get(1).get(0), "Первая строка содержит id на первом месте");
        assertEquals("TASK", substrings.get(1).get(1), "Первая строка содержит type на втором месте");
        assertEquals("TaskName3", substrings.get(1).get(2), "Первая строка содержит name на третьем месте");
        assertEquals("DONE", substrings.get(1).get(3), "Первая строка содержит status на четвёртом месте");
        assertEquals("TaskDescription3", substrings.get(1).get(4), "Первая строка содержит description на пятом месте");
        assertEquals("04.03.2025 21:52", substrings.get(1).get(5), "Первая строка содержит startTime на шестом месте");
        assertEquals("04.03.2025 23:52", substrings.get(1).get(6), "Первая строка содержит endTime на седьмом месте");
        assertEquals("PT2H", substrings.get(1).get(7), "Первая строка содержит duration на восьмом месте");

        assertEquals("6", substrings.get(2).get(0), "Вторая строка содержит id на первом месте");
        assertEquals("EPIC", substrings.get(2).get(1), "Вторая строка содержит type на втором месте");
        assertEquals("EpicName3", substrings.get(2).get(2), "Вторая строка содержит name на третьем месте");
        assertEquals("DONE", substrings.get(2).get(3), "Вторая строка содержит status на четвёртом месте");
        assertEquals("EpicDescription3", substrings.get(2).get(4), "Вторая строка содержит description на пятом месте");
        assertEquals("04.03.2025 21:52", substrings.get(1).get(5), "Вторая строка содержит startTime на шестом месте");
        assertEquals("04.03.2025 23:52", substrings.get(1).get(6), "Вторая строка содержит endTime на седьмом месте");
        assertEquals("PT2H", substrings.get(1).get(7), "Вторая строка содержит duration на восьмом месте");

        assertEquals("9", substrings.get(3).get(0), "Третья строка содержит id на первом месте");
        assertEquals("SUBTASK", substrings.get(3).get(1), "Третья строка содержит type на втором месте");
        assertEquals("SubTaskName3", substrings.get(3).get(2), "Третья строка содержит name на третьем месте");
        assertEquals("DONE", substrings.get(3).get(3), "Третья строка содержит status на четвёртом месте");
        assertEquals("SubTaskDescription3", substrings.get(3).get(4), "Третья строка содержит description на пятом месте");
        assertEquals("04.03.2025 21:52", substrings.get(3).get(5), "Третья строка содержит startTime на шестом месте");
        assertEquals("04.03.2025 23:52", substrings.get(3).get(6), "Третья строка содержит endTime на седьмом месте");
        assertEquals("PT2H", substrings.get(3).get(7), "Третья строка содержит duration на восьмом месте");
        assertEquals("6", substrings.get(3).get(8), "Третья строка содержит epicId на девятом месте");
    }

    @Test
    public void fileShouldBeEmptyAfterDeletingAllEntities() throws IOException {
        taskManager.deleteAllTasks();
        taskManager.deleteAllEpics();

        String firstLine = bufferedReader.readLine();
        HashMap<Integer, List<String>> substrings = new HashMap<>(9);
        String line;
        int lineNumber = 0;
        while ((line = bufferedReader.readLine()) != null) {
            substrings.put(++lineNumber, List.of(line.split(",")));
        }

        assertEquals(1, substrings.size() + 1, "Файл должен содержать 1 строчку (только заголовок)");
        String title = "id,type,name,status,description,startTime,endTime,duration,epic";
        assertEquals(title, firstLine, "Первая строка это и есть заголовок");
    }

    @Test
    public void shouldRestoreManagerFromFile() {
        FileBackedTaskManager restoredManager = FileBackedTaskManager.loadFromFile(file);

        assertEquals(9, restoredManager.idCounter,
                "Счётчик id имеет то же значение что и в изначальном менеджере");

        List<Task> tasksFromOriginalManager = taskManager.getTasks();
        List<Task> tasksFromRestoredManager = restoredManager.getTasks();
        List<SubTask> subTasksFromOriginalManager = taskManager.getSubTasks();
        List<SubTask> subTasksFromRestoredManager = restoredManager.getSubTasks();
        List<Epic> epicsFromOriginalManager = taskManager.getEpics();
        List<Epic> epicsFromRestoredManager = restoredManager.getEpics();

        assertEquals(tasksFromOriginalManager.size(), tasksFromRestoredManager.size(),
                "Количество тасков должно совпадать");
        assertEquals(epicsFromOriginalManager.size(), epicsFromRestoredManager.size(),
                "Количество эпиков должно совпадать");
        assertEquals(subTasksFromOriginalManager.size(), subTasksFromRestoredManager.size(),
                "Количество подзадач должно совпадать");

        for (int i = 0; i < tasksFromOriginalManager.size(); i++) {
            Task originalTask = tasksFromOriginalManager.get(i);
            Task restoredTask = tasksFromRestoredManager.get(i);

            assertEquals(originalTask.getId(), restoredTask.getId(),
                    "ID задач должны совпадать");

            assertEquals(originalTask.getName(), restoredTask.getName(),
                    "Названия задач должны совпадать");

            assertEquals(originalTask.getDescription(), restoredTask.getDescription(),
                    "Описания задач должны совпадать");

            assertEquals(originalTask.getStatus(), restoredTask.getStatus(),
                    "Статусы задач должны совпадать");

            assertEquals(originalTask.getStartTime(), restoredTask.getStartTime(),
                    "StartTime задач должны совпадать");

            assertEquals(originalTask.getEndTime(), restoredTask.getEndTime(),
                    "EndTime задач должны совпадать");

            assertEquals(originalTask.getDuration(), restoredTask.getDuration(),
                    "Duration задач должны совпадать");
        }

        for (int i = 0; i < epicsFromOriginalManager.size(); i++) {
            Epic originalEpic = epicsFromOriginalManager.get(i);
            Epic restoredEpic = epicsFromRestoredManager.get(i);

            assertEquals(originalEpic.getId(), restoredEpic.getId(),
                    "ID эпиков должны совпадать");

            assertEquals(originalEpic.getName(), restoredEpic.getName(),
                    "Названия эпиков должны совпадать");

            assertEquals(originalEpic.getDescription(), restoredEpic.getDescription(),
                    "Описания эпиков должны совпадать");

            assertEquals(originalEpic.getStatus(), restoredEpic.getStatus(),
                    "Статусы эпиков должны совпадать");

            assertEquals(originalEpic.getSubTaskIdList(), restoredEpic.getSubTaskIdList(),
                    "Списки ID подзадач должны совпадать");

            assertEquals(originalEpic.getStartTime(), restoredEpic.getStartTime(),
                    "StartTime эпиков должны совпадать");

            assertEquals(originalEpic.getEndTime(), restoredEpic.getEndTime(),
                    "EndTime эпиков должны совпадать");

            assertEquals(originalEpic.getDuration(), restoredEpic.getDuration(),
                    "Duration эпиков должны совпадать");
        }

        for (int i = 0; i < subTasksFromOriginalManager.size(); i++) {
            SubTask originalSubTask = subTasksFromOriginalManager.get(i);
            SubTask restoredSubTask = subTasksFromRestoredManager.get(i);

            assertEquals(originalSubTask.getId(), restoredSubTask.getId(),
                    "ID подзадач должны совпадать");

            assertEquals(originalSubTask.getName(), restoredSubTask.getName(),
                    "Названия подзадач должны совпадать");

            assertEquals(originalSubTask.getDescription(), restoredSubTask.getDescription(),
                    "Описания подзадач должны совпадать");

            assertEquals(originalSubTask.getStatus(), restoredSubTask.getStatus(),
                    "Статусы подзадач должны совпадать");

            assertEquals(originalSubTask.getStartTime(), restoredSubTask.getStartTime(),
                    "StartTime подзадач должны совпадать");

            assertEquals(originalSubTask.getEndTime(), restoredSubTask.getEndTime(),
                    "EndTime подзадач должны совпадать");

            assertEquals(originalSubTask.getDuration(), restoredSubTask.getDuration(),
                    "Duration подзадач должны совпадать");
        }
    }

    @Test
    public void shouldBeEmptyAfterRestoreFromEmptyFile() throws IOException {
        File emptyFile = File.createTempFile("empty", "csv");
        String title = CSVTaskFormatUtils.getCSVTitle();
        Writer writer = new BufferedWriter(new FileWriter(emptyFile));
        writer.write(title);

        FileBackedTaskManager managerFromEmptyFile = FileBackedTaskManager.loadFromFile(emptyFile);

        List<Task> tasksFromRestoredManager = managerFromEmptyFile.getTasks();
        List<SubTask> subTasksFromRestoredManager = managerFromEmptyFile.getSubTasks();
        List<Epic> epicsFromRestoredManager = managerFromEmptyFile.getEpics();

        assertEquals(0, managerFromEmptyFile.idCounter, "Счётчик id должен быть на нуле");
        assertTrue(tasksFromRestoredManager.isEmpty(), "Список задач должен быть пуст");
        assertTrue(subTasksFromRestoredManager.isEmpty(), "Список подзадач должен быть пуст");
        assertTrue(epicsFromRestoredManager.isEmpty(), "Список эпиков должен быть пуст");
    }
}