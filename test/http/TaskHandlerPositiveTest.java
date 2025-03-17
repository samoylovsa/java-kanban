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

public class TaskHandlerPositiveTest {

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
    void createNewTaskTest() throws IOException, InterruptedException {
        String requestBody = "{\"name\":\"Task Name\",\"description\":\"Task Description\",\"status\":\"NEW\"," +
                "\"startTime\":\"2025-03-16T14:30\",\"duration\":\"PT1H\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(url)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, handler);

        String expectedResponseBody = "{\"id\":1,\"name\":\"Task Name\",\"description\":\"Task Description\",\"status\":\"NEW\"," +
                "\"startTime\":\"2025-03-16T14:30\",\"duration\":\"PT1H\"}";
        Task actualTask = gson.fromJson(response.body(), Task.class);
        Task expectedTask = taskManager.getTask(1);

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");
        assertEquals(expectedResponseBody, response.body(), "Тело ответа должно содержать все данные");
        assertEquals(expectedTask, actualTask, "Задача из менеджера должна совпадать с задачей из тела ответа");
    }

    @Test
    void updateExistingTaskTest() throws IOException, InterruptedException {
        LocalDateTime dateTime = LocalDateTime.parse("2025-03-16T14:30:00.000");
        Task task = new Task("Task Name", "Task Description", Status.NEW, dateTime, Duration.ofHours(1));
        taskManager.createTask(task);

        String requestBody = "{\"id\":1,\"name\":\"Updated Task Name\",\"description\":\"Updated Task Description\",\"status\":\"DONE\"," +
                "\"startTime\":\"2025-04-20T14:30\",\"duration\":\"PT1H30M\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(url)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, handler);

        assertEquals(201, response.statusCode(), "Код ответа должен быть 201");
        assertEquals("Updated Task Name", taskManager.getTask(1).getName(),
                "В менеджере должна храниться задача с изменёнными данными");
        assertEquals("Updated Task Description", taskManager.getTask(1).getDescription(),
                "В менеджере должна храниться задача с изменёнными данными");
        assertEquals(Status.DONE, taskManager.getTask(1).getStatus(),
                "В менеджере должна храниться задача с изменёнными данными");
        assertEquals(LocalDateTime.parse("2025-04-20T14:30"), taskManager.getTask(1).getStartTime(),
                "В менеджере должна храниться задача с изменёнными данными");
        assertEquals(Duration.ofHours(1).plusMinutes(30), taskManager.getTask(1).getDuration(),
                "В менеджере должна храниться задача с изменёнными данными");
    }

    @Test
    void getAllTasksTest() throws IOException, InterruptedException {
        LocalDateTime dateTime = LocalDateTime.parse("2025-03-16T14:30:00.000");
        Task firstTask = new Task("Task Name", "Task Description", Status.NEW, dateTime, Duration.ofHours(1));
        Task secondTask = new Task("Task Name", "Task Description", Status.NEW, dateTime.plusMinutes(120), Duration.ofHours(1));
        Task thirdTask = new Task("Task Name", "Task Description", Status.NEW, dateTime.plusMinutes(240), Duration.ofHours(1));
        taskManager.createTask(firstTask);
        taskManager.createTask(secondTask);
        taskManager.createTask(thirdTask);

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, handler);
        String expectedResponseBody = "[{\"id\":1,\"name\":\"Task Name\",\"description\":\"Task Description\"," +
                "\"status\":\"NEW\",\"startTime\":\"2025-03-16T14:30\",\"duration\":\"PT1H\"},{\"id\":2,\"name\":\"Task Name\"," +
                "\"description\":\"Task Description\",\"status\":\"NEW\",\"startTime\":\"2025-03-16T16:30\",\"duration\":\"PT1H\"}," +
                "{\"id\":3,\"name\":\"Task Name\",\"description\":\"Task Description\",\"status\":\"NEW\"," +
                "\"startTime\":\"2025-03-16T18:30\",\"duration\":\"PT1H\"}]";
        List<Task> expectedListOfAllTasks = taskManager.getTasks();
        List<Task> actualListOfAllTasks = gson.fromJson(
                response.body(),
                new TypeToken<List<Task>>() {}.getType()
        );

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");
        assertEquals(expectedResponseBody, response.body(), "Тело ответа должно соответствовать структуре и данным хранящимся в менеджере");
        assertEquals(expectedListOfAllTasks, actualListOfAllTasks, "Список задач в менеджере должен соответствовать списку в ответе");
    }

    @Test
    void getTaskByIdTest() throws IOException, InterruptedException {
        LocalDateTime dateTime = LocalDateTime.parse("2025-03-16T14:30:00.000");
        Task firstTask = new Task("Task Name", "Task Description", Status.NEW, dateTime, Duration.ofHours(1));
        taskManager.createTask(firstTask);
        URI urlForGettingTask = url.resolve("/tasks/" + taskManager.getTasks().getFirst().getId());

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(urlForGettingTask)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, handler);

        String expectedResponseBody = "{\"id\":1,\"name\":\"Task Name\",\"description\":\"Task Description\",\"status\":\"NEW\",\"startTime\":\"2025-03-16T14:30\",\"duration\":\"PT1H\"}";
        String actualResponseBody = response.body();
        Task expectedTask = taskManager.getTask(1);
        Task actualTask = gson.fromJson(response.body(), Task.class);

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");
        assertEquals(expectedResponseBody, actualResponseBody, "Тело ответа должно соответствовать структуре и данным хранящимся в менеджере");
        assertEquals(expectedTask, actualTask, "Задача из менеджера должна соответствовать задачи из ответа");
    }

    @Test
    void deleteTaskByIdTest() throws IOException, InterruptedException {
        LocalDateTime dateTime = LocalDateTime.parse("2025-03-16T14:30:00.000");
        Task task = new Task("Task Name", "Task Description", Status.NEW, dateTime, Duration.ofHours(1));
        taskManager.createTask(task);
        URI urlForDeletingTask = url.resolve("/tasks/" + taskManager.getTasks().getFirst().getId());

        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(urlForDeletingTask)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, handler);

        List<Task> expectedTasks = taskManager.getTasks();

        assertEquals(201, response.statusCode(), "Код ответа должен быть 201");
        assertTrue(expectedTasks.isEmpty(), "Список задач в менеджере пустой после удаления");
    }
}
