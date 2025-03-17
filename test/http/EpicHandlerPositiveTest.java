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

public class EpicHandlerPositiveTest {

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
        url = URI.create("http://localhost:8080/epics");
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
    void createNewEpicTest() throws IOException, InterruptedException {
        String requestBody = "{\"name\": \"Epic Name\", \"description\": \"Epic Description\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(url)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, handler);

        String expectedResponseBody = "{\"subTaskIdList\":[],\"endTime\":null,\"id\":1,\"name\":\"Epic Name\"," +
                "\"description\":\"Epic Description\",\"status\":\"NEW\",\"startTime\":null,\"duration\":null}";
        Epic actualEpic = gson.fromJson(expectedResponseBody, Epic.class);
        Epic expectedEpic = taskManager.getEpic(1);

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");
        assertEquals(expectedResponseBody, response.body(), "Тело ответа должно содержать все данные");
        assertEquals(expectedEpic, actualEpic, "Эпик из менеджера должна совпадать с эпиком из тела ответа");
    }

    @Test
    void updateExistingEpicTest() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic Name", "Epic Description");
        taskManager.createEpic(epic);

        String requestBody = "{\"id\": \"1\", \"name\": \"Updated Epic Name\", \"description\": \"Updated Epic Description\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(url)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, handler);

        assertEquals(201, response.statusCode(), "Код ответа должен быть 201");
        assertEquals("Updated Epic Name", taskManager.getEpic(1).getName(),
                "В менеджере должен храниться эпик с изменёнными данными");
        assertEquals("Updated Epic Description", taskManager.getEpic(1).getDescription(),
                "В менеджере должен храниться эпик с изменёнными данными");
    }

    @Test
    void getAllEpicsTest() throws IOException, InterruptedException {
        Epic firstEpic = new Epic("Epic Name", "Epic Description");
        Epic secondEpic = new Epic("Epic Name", "Epic Description");
        Epic thirdEpic = new Epic("Epic Name", "Epic Description");
        taskManager.createEpic(firstEpic);
        taskManager.createEpic(secondEpic);
        taskManager.createEpic(thirdEpic);

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, handler);

        String expectedResponseBody = "[{\"subTaskIdList\":[],\"endTime\":null,\"id\":1,\"name\":\"Epic Name\",\"description\":\"Epic Description\"," +
                "\"status\":\"NEW\",\"startTime\":null,\"duration\":null},{\"subTaskIdList\":[],\"endTime\":null,\"id\":2,\"name\":\"Epic Name\"," +
                "\"description\":\"Epic Description\",\"status\":\"NEW\",\"startTime\":null,\"duration\":null},{\"subTaskIdList\":[]," +
                "\"endTime\":null,\"id\":3,\"name\":\"Epic Name\",\"description\":\"Epic Description\",\"status\":\"NEW\",\"startTime\":null,\"duration\":null}]";
        List<Epic> expectedListOfAllEpics = taskManager.getEpics();
        List<Epic> actualListOfAllEpics = gson.fromJson(
                response.body(),
                new TypeToken<List<Epic>>() {
                }.getType()
        );

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");
        assertEquals(expectedResponseBody, response.body(), "Тело ответа должно соответствовать структуре и данным хранящимся в менеджере");
        assertEquals(expectedListOfAllEpics, actualListOfAllEpics, "Список эпиков в менеджере должен соответствовать списку в ответе");
    }

    @Test
    void getEpicByIdTest() throws IOException, InterruptedException {
        Epic firstEpic = new Epic("Epic Name", "Epic Description");
        taskManager.createEpic(firstEpic);
        URI urlForGettingEpic = url.resolve("/epics/" + taskManager.getEpics().getFirst().getId());

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(urlForGettingEpic)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, handler);

        String expectedResponseBody = "{\"subTaskIdList\":[],\"endTime\":null,\"id\":1,\"name\":\"Epic Name\"," +
                "\"description\":\"Epic Description\",\"status\":\"NEW\",\"startTime\":null,\"duration\":null}";
        String actualResponseBody = response.body();
        Epic expectedEpic = taskManager.getEpic(1);
        Epic actualEpic = gson.fromJson(response.body(), Epic.class);

        assertEquals(200, response.statusCode(), "Код ответа должен быть 200");
        assertEquals(expectedResponseBody, actualResponseBody, "Тело ответа должно соответствовать структуре и данным хранящимся в менеджере");
        assertEquals(expectedEpic, actualEpic, "Эпик из менеджера должен соответствовать эпику из ответа");
    }

    @Test
    void deleteEpicByIdTest() throws IOException, InterruptedException {
        Epic task = new Epic("Epic Name", "Epic Description");
        taskManager.createEpic(task);
        URI urlForDeletingEpic = url.resolve("/epics/" + taskManager.getEpics().getFirst().getId());

        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(urlForDeletingEpic)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, handler);

        List<Epic> expectedEpics = taskManager.getEpics();

        assertEquals(201, response.statusCode(), "Код ответа должен быть 201");
        assertTrue(expectedEpics.isEmpty(), "Список эпиков в менеджере пустой после удаления");
    }
}
