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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubTaskHandlerNegativeTest {

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
        url = URI.create("http://localhost:8080/subtasks");
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
    void createIntersectedSubTaskTest() throws IOException, InterruptedException {
        int epicId = taskManager.createEpic(new Epic("EpicName", "EpicDescription"));
        String requestBody = "{\"name\": \"SubTask Name\", \"description\": \"SubTask Description\", \"status\": \"NEW\", " +
                "\"epicId\": " + epicId + ", \"startTime\": \"2023-12-19T14:30:45.123\", \"duration\": \"PT1H\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(url)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .build();
        httpClient.send(request, handler);

        HttpResponse<String> response = httpClient.send(request, handler);

        String expectedResponseBody = "{\"errorMessage\":\"Создаваемая подзадача пересекается по времени с уже существующими подзадачами\"}";

        assertEquals(406, response.statusCode(), "Код ответа должен быть 406");
        assertEquals(expectedResponseBody, response.body(), "В теле ответа должно быть сообщение о пересечении задач");
    }

    @Test
    void updateSubTaskWithNotFoundEpicIdTest() throws IOException, InterruptedException {
        int epicId = taskManager.createEpic(new Epic("EpicName", "EpicDescription"));
        LocalDateTime dateTime = LocalDateTime.parse("2025-03-16T14:30");
        SubTask subTask = new SubTask("SubTask Name", "SubTask Description", Status.NEW, epicId, dateTime, Duration.ofHours(1));
        int subTaskId = taskManager.createSubTask(subTask);

        String requestBody = "{\"id\":" + subTaskId + ",\"name\":\"Updated SubTask Name\",\"description\":\"Updated SubTask Description\",\"status\":\"DONE\"," +
                "\"epicId\":1231231321,\"startTime\":\"2023-12-19T14:30\",\"duration\":\"PT1H30M\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(url)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, handler);

        String expectedResponseBody = "{\"errorMessage\":\"Не найден эпик с таким epicId: 1231231321\"}";

        assertEquals(404, response.statusCode(), "Код ответа должен быть 404");
        assertEquals(expectedResponseBody, response.body(), "В теле ответа должно быть сообщение о том что не найден такой эпик");
    }

    @Test
    void getEmptySubTasksTest() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, handler);

        String expectedResponseBody = "[]";

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");
        assertEquals(expectedResponseBody, response.body(), "В теле ответа должен быть пустой список");
    }

    @Test
    void getNotFoundSubTaskByIdTest() throws IOException, InterruptedException {
        URI urlForGettingSubTask = url.resolve("/subtasks/" + "777");

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(urlForGettingSubTask)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, handler);

        String expectedResponseBody = "{\"errorMessage\":\"Не найдена подзадача с id: 777\"}";

        assertEquals(404, response.statusCode(), "Код ответа должен быть 404");
        assertEquals(expectedResponseBody, response.body(), "В теле ответа должно быть сообщение о том что задача не найдена");
    }

    @Test
    void deleteNotFoundSubTaskByIdTest() throws IOException, InterruptedException {
        URI urlForDeletingSubTask = url.resolve("/subtasks/" + "777");

        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(urlForDeletingSubTask)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, handler);

        String expectedResponseBody = "{\"errorMessage\":\"Не найдена подзадача с id: 777\"}";

        assertEquals(404, response.statusCode(), "Код ответа должен быть 404");
        assertEquals(expectedResponseBody, response.body(), "В теле ответа должно быть сообщение о том что задача не найдена");
    }
}
