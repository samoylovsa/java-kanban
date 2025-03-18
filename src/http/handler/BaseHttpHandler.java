package http.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import http.adapter.DurationAdapter;
import http.adapter.LocalDateTimeAdapter;
import http.model.ErrorMessage;
import manager.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public class BaseHttpHandler {

    protected final TaskManager taskManager;
    protected final Gson gson;

    BaseHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .serializeNulls()
                .create();
    }

    protected void sendResponse(HttpExchange exchange, int responseCode, String responseText) throws IOException {
        byte[] response = responseText.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(responseCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
        System.out.println("Отправлен ответ " + responseCode + " " + responseText);
    }

    protected boolean isPathValid(String[] pathParts, String pathName, int expectedLength) {
        return pathParts.length == expectedLength && pathParts[1].equals(pathName);
    }

    protected void sendErrorResponse(HttpExchange exchange, int responseCode, String responseText) throws IOException {
        sendResponse(exchange, responseCode, gson.toJson(new ErrorMessage(responseText)));
    }
}