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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EpicHandlerNegativeTest {

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
    void createNewEpicWithBadRequestTest() throws IOException, InterruptedException {
        String requestBody = "{\"name\": \"Epic Name\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(url)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, handler);

        String expectedResponseBody = "{\"errorMessage\":\"Некорректное тело запроса\"}";

        assertEquals(400, response.statusCode(), "Код ответа должен быть 400");
        assertEquals(expectedResponseBody, response.body(), "В теле ответа должно быть сообщение о некорректном теле запроса");
    }

    @Test
    void updateNotFoundEpicTest() throws IOException, InterruptedException {
        String requestBody = "{\"id\": \"12333\", \"name\": \"Updated Epic Name\", \"description\": \"Updated Epic Description\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(url)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, handler);

        String expectedResponseBody = "{\"errorMessage\":\"Не найден эпик с updatedEpicId: 12333\"}";

        assertEquals(404, response.statusCode(), "Код ответа должен быть 404");
        assertEquals(expectedResponseBody, response.body(), "В теле ответа должно быть сообщение о том что эпик не найден");
    }

    @Test
    void getEmptyEpicsListTest() throws IOException, InterruptedException {
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
    void getNotFoundEpicByIdTest() throws IOException, InterruptedException {
        URI urlForGettingEpic = url.resolve("/epics/" + "1234214");

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(urlForGettingEpic)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, handler);

        String expectedResponseBody = "{\"errorMessage\":\"Не найден эпик с id: 1234214\"}";

        assertEquals(404, response.statusCode(), "Код ответа должен быть 404");
        assertEquals(expectedResponseBody, response.body(), "В теле ответа должно быть сообщение о том что эпик не найден");
    }

    @Test
    void deleteNotFoundEpicByIdTest() throws IOException, InterruptedException {
        URI urlForDeletingEpic = url.resolve("/epics/" + "123333");

        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(urlForDeletingEpic)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, handler);

        String expectedResponseBody = "{\"errorMessage\":\"Не найден эпик с id: 123333\"}";

        assertEquals(404, response.statusCode(), "Код ответа должен быть 404");
        assertEquals(expectedResponseBody, response.body(), "В теле ответа должно быть сообщение о том что эпик не найден");
    }

    @Test
    void getSubTasksByNotFoundEpicIdTest() throws IOException, InterruptedException {
        URI urlForGettingSubTasks = url.resolve("/epics/" + "777" + "/subtasks");

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(urlForGettingSubTasks)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, handler);

        String expectedResponseBody = "{\"errorMessage\":\"Не найден эпик с id: 777\"}";

        assertEquals(404, response.statusCode(), "Код ответа должен быть 404");
        assertEquals(expectedResponseBody, response.body(), "В теле ответа должно быть сообщение о том что эпик не найден");
    }
}
