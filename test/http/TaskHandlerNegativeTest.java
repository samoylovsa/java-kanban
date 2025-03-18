package http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import http.adapter.DurationAdapter;
import http.adapter.LocalDateTimeAdapter;
import manager.Manager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Status;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TaskHandlerNegativeTest {

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
        url = URI.create("http://localhost:8080/tasks");
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
    void createIntersectedTaskTest() throws IOException, InterruptedException {
        String requestBody = "{\"name\":\"Task Name\",\"description\":\"Task Description\",\"status\":\"NEW\"," +
                "\"startTime\":\"2025-03-16T14:30\",\"duration\":\"PT1H\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(url)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .build();
        httpClient.send(request, handler);

        HttpResponse<String> response = httpClient.send(request, handler);

        String expectedResponseBody = "{\"errorMessage\":\"Создаваемая задача пересекается по времени с уже существующими задачами\"}";

        assertEquals(406, response.statusCode(), "Код ответа должен быть 406");
        assertEquals(expectedResponseBody, response.body(), "В теле ответа должно быть сообщение о пересечении задач");
    }

    @Test
    void updateNotFoundTaskTest() throws IOException, InterruptedException {
        LocalDateTime dateTime = LocalDateTime.parse("2025-03-16T14:30:00.000");
        Task task = new Task("Task Name", "Task Description", Status.NEW, dateTime, Duration.ofHours(1));
        taskManager.createTask(task);

        String requestBody = "{\"id\":100,\"name\":\"Updated Task Name\",\"description\":\"Updated Task Description\",\"status\":\"DONE\"," +
                "\"startTime\":\"2025-04-20T14:30\",\"duration\":\"PT1H30M\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(url)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, handler);

        String expectedResponseBody = "{\"errorMessage\":\"Не найдена задача с taskId: 100\"}";

        assertEquals(404, response.statusCode(), "Код ответа должен быть 404");
        assertEquals(expectedResponseBody, response.body(), "В теле ответа должно быть сообщение о том что задача не найдена");
    }

    @Test
    void getEmptyTasksListTest() throws IOException, InterruptedException {
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
    void getNotFoundTaskByIdTest() throws IOException, InterruptedException {
        URI urlForGettingTask = url.resolve("/tasks/" + 12344);

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(urlForGettingTask)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, handler);

        String expectedResponseBody = "{\"errorMessage\":\"Не найдена задача с id: 12344\"}";

        assertEquals(404, response.statusCode(), "Код ответа должен быть 404");
        assertEquals(expectedResponseBody, response.body(), "В теле ответа должно быть сообщение о том что задача не найдена");
    }

    @Test
    void deleteNotFoundTaskByIdTest() throws IOException, InterruptedException {
        URI urlForDeletingTask = url.resolve("/tasks/" + 2134);

        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(urlForDeletingTask)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, handler);

        String expectedResponseBody = "{\"errorMessage\":\"Не найдена задача с id: 2134\"}";

        assertEquals(404, response.statusCode(), "Код ответа должен быть 404");
        assertEquals(expectedResponseBody, response.body(), "В теле ответа должно быть сообщение о том что задача не найдена");
    }
}
