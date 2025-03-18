package http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HistoryHandlerPositiveTest {

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
        url = URI.create("http://localhost:8080/history");
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
    void getHistoryTest() throws IOException, InterruptedException {
        LocalDateTime dateTime = LocalDateTime.parse("2025-03-16T14:30:00.000");
        int taskId = taskManager.createTask(new Task("TaskName", "TaskDescription", Status.NEW, dateTime, Duration.ofHours(1)));
        int epicId = taskManager.createEpic(new Epic("EpicName", "EpicDescription"));
        int subTaskId = taskManager.createSubTask(new SubTask("SubTaskName", "SubTaskDescription", Status.NEW, epicId, dateTime.plusHours(3), Duration.ofHours(1)));

        Task task = taskManager.getTask(taskId);
        Epic epic = taskManager.getEpic(epicId);
        SubTask subTask = taskManager.getSubTask(subTaskId);

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, handler);

        String expectedResponseBody = "[{\"id\":1,\"name\":\"TaskName\",\"description\":\"TaskDescription\",\"status\":\"NEW\"," +
                "\"startTime\":\"2025-03-16T14:30\",\"duration\":\"PT1H\"},{\"subTaskIdList\":[3],\"endTime\":\"2025-03-16T18:30\"," +
                "\"id\":2,\"name\":\"EpicName\",\"description\":\"EpicDescription\",\"status\":\"NEW\",\"startTime\":\"2025-03-16T17:30\"," +
                "\"duration\":\"PT1H\"},{\"epicId\":2,\"id\":3,\"name\":\"SubTaskName\",\"description\":\"SubTaskDescription\",\"status\":\"NEW\"," +
                "\"startTime\":\"2025-03-16T17:30\",\"duration\":\"PT1H\"}]";
        String actualResponseBody = response.body();

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");
        assertEquals(expectedResponseBody, actualResponseBody, "Тело ответа должно соответствовать структуре и данным из менеджера");
    }
}
