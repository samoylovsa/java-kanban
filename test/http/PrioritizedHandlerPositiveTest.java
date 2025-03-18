package http;

import com.google.gson.*;
import http.adapter.DurationAdapter;
import http.adapter.LocalDateTimeAdapter;
import manager.Manager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrioritizedHandlerPositiveTest {

    TaskManager taskManager;
    HttpTaskServer httpTaskServer;
    HttpClient httpClient;
    URI url;
    HttpResponse.BodyHandler<String> handler;
    Gson gson;

    @BeforeEach
    void beforeEach() {
        taskManager = Manager.getDefault();
        try {
            httpTaskServer = new HttpTaskServer(taskManager);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        httpTaskServer.start();
        httpClient = HttpClient.newHttpClient();
        url = URI.create("http://localhost:8080/prioritized");
        handler = HttpResponse.BodyHandlers.ofString();
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .serializeNulls()
                .create();
    }

    @AfterEach
    void afterEach() {
        httpTaskServer.stop(1);
    }

    @Test
    void getPrioritizedTasksTest() throws IOException, InterruptedException {
        LocalDateTime firstStartTime = LocalDateTime.parse("2025-03-16T14:30:00.000");
        LocalDateTime secondStartTime = LocalDateTime.parse("2025-03-19T14:30:00.000").plusMinutes(120);

        Task firstTask = new Task("Name1", "Description1", Status.NEW, firstStartTime, Duration.ofHours(1));
        int epicId = taskManager.createEpic(new Epic("Name", "Description"));
        SubTask firstSubTask = new SubTask("Name1", "Description1", Status.NEW, epicId, secondStartTime, Duration.ofHours(1));

        int subTaskId = taskManager.createSubTask(firstSubTask);
        int taskId = taskManager.createTask(firstTask);

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, handler);

        Task expectedTask = taskManager.getTask(taskId);
        SubTask expectedSubTask = taskManager.getSubTask(subTaskId);
        List<Task> expectedPrioritizedList = taskManager.getPrioritizedTasks();
        String expectedResponseBody = "[{\"id\":3,\"name\":\"Name1\",\"description\":\"Description1\",\"status\":\"NEW\"," +
                "\"startTime\":\"2025-03-16T14:30\",\"duration\":\"PT1H\"},{\"epicId\":1,\"id\":2,\"name\":\"Name1\"," +
                "\"description\":\"Description1\",\"status\":\"NEW\",\"startTime\":\"2025-03-19T16:30\",\"duration\":\"PT1H\"}]";
        String actualResponseBody = response.body();
        JsonArray jsonArray = JsonParser.parseString(actualResponseBody).getAsJsonArray();
        JsonElement taskElement = jsonArray.get(0);
        Task actualTask = gson.fromJson(taskElement, Task.class);
        JsonElement subTaskElement = jsonArray.get(1);
        SubTask actualSubTask = gson.fromJson(subTaskElement, SubTask.class);
        List<Task> actualPrioritizedList = List.of(actualTask, actualSubTask);

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");
        assertEquals(expectedResponseBody, actualResponseBody, "Тело ответа должно соответствовать структуре и данным из менеджера");
        assertEquals(expectedTask, actualTask, "Задача из тела ответа соответствует задаче в менеджере");
        assertEquals(expectedSubTask, actualSubTask, "Подзадача из тела ответа соответствует подзадаче в менеджере");
        assertEquals(expectedPrioritizedList, actualPrioritizedList, "Список задач из ответа соответствует списку задач в менеджере");
    }
}