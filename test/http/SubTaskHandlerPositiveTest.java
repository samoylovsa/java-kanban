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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SubTaskHandlerPositiveTest {

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
    void createNewSubTaskTest() throws IOException, InterruptedException {
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

        HttpResponse<String> response = httpClient.send(request, handler);

        String expectedResponseBody = "{\"epicId\":1,\"id\":2,\"name\":\"SubTask Name\",\"description\":\"SubTask Description\"," +
                "\"status\":\"NEW\",\"startTime\":\"2023-12-19T14:30:45.123\",\"duration\":\"PT1H\"}";
        SubTask actualSubTask = gson.fromJson(response.body(), SubTask.class);
        SubTask expectedSubTask = taskManager.getSubTask(2);

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");
        assertEquals(expectedResponseBody, response.body(), "Тело ответа должно содержать все данные");
        assertEquals(expectedSubTask, actualSubTask, "Задача из менеджера должна совпадать с задачей из тела ответа");
    }

    @Test
    void updateExistingSubTaskTest() throws IOException, InterruptedException {
        int epicId = taskManager.createEpic(new Epic("EpicName", "EpicDescription"));
        LocalDateTime dateTime = LocalDateTime.parse("2025-03-16T14:30");
        SubTask subTask = new SubTask("SubTask Name", "SubTask Description", Status.NEW, epicId, dateTime, Duration.ofHours(1));
        int subTaskId = taskManager.createSubTask(subTask);

        String requestBody = "{\"id\":" + subTaskId + ",\"name\":\"Updated SubTask Name\",\"description\":\"Updated SubTask Description\",\"status\":\"DONE\"," +
                "\"epicId\":1,\"startTime\":\"2023-12-19T14:30\",\"duration\":\"PT1H30M\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(url)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, handler);

        assertEquals(201, response.statusCode(), "Код ответа должен быть 201");
        assertEquals("Updated SubTask Name", taskManager.getSubTask(subTaskId).getName(),
                "В менеджере должна храниться задача с изменёнными данными");
        assertEquals("Updated SubTask Description", taskManager.getSubTask(subTaskId).getDescription(),
                "В менеджере должна храниться задача с изменёнными данными");
        assertEquals(Status.DONE, taskManager.getSubTask(subTaskId).getStatus(),
                "В менеджере должна храниться задача с изменёнными данными");
        assertEquals(LocalDateTime.parse("2023-12-19T14:30"), taskManager.getSubTask(subTaskId).getStartTime(),
                "В менеджере должна храниться задача с изменёнными данными");
        assertEquals(Duration.ofHours(1).plusMinutes(30), taskManager.getSubTask(subTaskId).getDuration(),
                "В менеджере должна храниться задача с изменёнными данными");
    }

    @Test
    void getAllSubTasksTest() throws IOException, InterruptedException {
        int epicId = taskManager.createEpic(new Epic("EpicName", "EpicDescription"));
        LocalDateTime dateTime = LocalDateTime.parse("2025-03-16T14:30:00.000");
        SubTask firstSubTask = new SubTask("SubTask Name", "SubTask Description", Status.NEW, epicId, dateTime, Duration.ofHours(1));
        SubTask secondSubTask = new SubTask("SubTask Name", "SubTask Description", Status.NEW, epicId, dateTime.plusMinutes(120), Duration.ofHours(1));
        SubTask thirdSubTask = new SubTask("SubTask Name", "SubTask Description", Status.NEW, epicId, dateTime.plusMinutes(240), Duration.ofHours(1));
        taskManager.createSubTask(firstSubTask);
        taskManager.createSubTask(secondSubTask);
        taskManager.createSubTask(thirdSubTask);

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, handler);
        String expectedResponseBody = "[{\"epicId\":1,\"id\":2,\"name\":\"SubTask Name\",\"description\":\"SubTask Description\"," +
                "\"status\":\"NEW\",\"startTime\":\"2025-03-16T14:30\",\"duration\":\"PT1H\"},{\"epicId\":1,\"id\":3,\"name\":\"SubTask Name\"," +
                "\"description\":\"SubTask Description\",\"status\":\"NEW\",\"startTime\":\"2025-03-16T16:30\",\"duration\":\"PT1H\"}," +
                "{\"epicId\":1,\"id\":4,\"name\":\"SubTask Name\",\"description\":\"SubTask Description\",\"status\":\"NEW\"," +
                "\"startTime\":\"2025-03-16T18:30\",\"duration\":\"PT1H\"}]";
        List<SubTask> expectedListOfAllSubTasks = taskManager.getSubTasks();
        List<SubTask> actualListOfAllSubTasks = gson.fromJson(
                response.body(),
                new TypeToken<List<SubTask>>() {
                }.getType()
        );

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");
        assertEquals(expectedResponseBody, response.body(), "Тело ответа должно соответствовать структуре и данным хранящимся в менеджере");
        assertEquals(expectedListOfAllSubTasks, actualListOfAllSubTasks, "Список задач в менеджере должен соответствовать списку в ответе");
    }

    @Test
    void getSubTaskByIdTest() throws IOException, InterruptedException {
        int epicId = taskManager.createEpic(new Epic("EpicName", "EpicDescription"));
        LocalDateTime dateTime = LocalDateTime.parse("2025-03-16T14:30");
        SubTask firstSubTask = new SubTask("SubTask Name", "SubTask Description", Status.NEW, epicId, dateTime, Duration.ofHours(1));
        int subTaskId = taskManager.createSubTask(firstSubTask);
        URI urlForGettingSubTask = url.resolve("/subtasks/" + taskManager.getSubTasks().getFirst().getId());

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(urlForGettingSubTask)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, handler);

        String expectedResponseBody = "{\"epicId\":1,\"id\":2,\"name\":\"SubTask Name\",\"description\":\"SubTask Description\",\"status\":\"NEW\",\"startTime\":\"2025-03-16T14:30\",\"duration\":\"PT1H\"}";
        String actualResponseBody = response.body();
        SubTask expectedSubTask = taskManager.getSubTask(subTaskId);
        SubTask actualSubTask = gson.fromJson(response.body(), SubTask.class);

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");
        assertEquals(expectedResponseBody, actualResponseBody, "Тело ответа должно соответствовать структуре и данным хранящимся в менеджере");
        assertEquals(expectedSubTask, actualSubTask, "Задача из менеджера должна соответствовать задачи из ответа");
    }

    @Test
    void deleteSubTaskByIdTest() throws IOException, InterruptedException {
        int epicId = taskManager.createEpic(new Epic("EpicName", "EpicDescription"));
        LocalDateTime dateTime = LocalDateTime.parse("2025-03-16T14:30:00.000");
        SubTask task = new SubTask("SubTask Name", "SubTask Description", Status.NEW, epicId, dateTime, Duration.ofHours(1));
        taskManager.createSubTask(task);
        URI urlForDeletingSubTask = url.resolve("/subtasks/" + taskManager.getSubTasks().getFirst().getId());

        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(urlForDeletingSubTask)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, handler);

        List<SubTask> expectedSubTasks = taskManager.getSubTasks();

        assertEquals(201, response.statusCode(), "Код ответа должен быть 201");
        assertTrue(expectedSubTasks.isEmpty(), "Список задач в менеджере пустой после удаления");
    }
}