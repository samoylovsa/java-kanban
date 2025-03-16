package http.handler;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.EntityIntersectionException;
import exceptions.EntityNotFoundException;
import http.utils.JsonValidator;
import manager.TaskManager;
import tasks.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeParseException;
import java.util.List;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {

    private static final String PATH_NAME = "tasks";

    public TaskHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.print("Начинается обработка запроса: ");

        String requestMethod = exchange.getRequestMethod();
        String requestPath = exchange.getRequestURI().getPath();
        String[] pathParts = requestPath.split("/");
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        System.out.println(requestMethod.toUpperCase() + " " + requestPath);

        switch (requestMethod) {
            case "GET":
                handleGetRequest(exchange, pathParts, PATH_NAME);
                break;
            case "POST":
                handlePostRequest(exchange, pathParts, PATH_NAME, requestBody);
                break;
            case "DELETE":
                handleDeleteRequest(exchange, pathParts, PATH_NAME);
                break;
            default:
                sendErrorResponse(exchange, 405, "Метод не поддерживается");
        }
    }

    private void handleGetRequest(HttpExchange exchange, String[] pathParts, String pathName) throws IOException {
        if (isPathValid(pathParts, pathName, 2)) {
            List<Task> tasks = taskManager.getTasks();
            String responseBody = gson.toJson(tasks);
            sendResponse(exchange, 200, responseBody);
        } else if (isPathValid(pathParts, pathName, 3)) {
            Task task = getTaskById(exchange, pathParts);
            if (task != null) {
                String responseBody = gson.toJson(task);
                sendResponse(exchange, 200, responseBody);
            }
        } else {
            sendErrorResponse(exchange, 400, "Ошибка в запросе");
        }
    }

    private void handlePostRequest(HttpExchange exchange, String[] pathParts, String pathName, String requestBody) throws IOException {
        if (isPathValid(pathParts, pathName, 2)) {
            Task task = parseTaskFromJson(exchange, requestBody);
            if (task != null) {
                JsonElement jsonElement = JsonParser.parseString(requestBody);
                if (jsonElement.getAsJsonObject().has("id")) {
                    updateTask(exchange, task);
                } else {
                    createTask(exchange, task);
                }
            }
        } else {
            sendErrorResponse(exchange, 400, "Некорректный запрос");
        }
    }

    private void handleDeleteRequest(HttpExchange exchange, String[] pathParts, String pathName) throws IOException {
        if (isPathValid(pathParts, pathName, 3)) {
            Task task = getTaskById(exchange, pathParts);
            if (task != null) {
                taskManager.deleteTask(task.getId());
                sendResponse(exchange, 201, "{}");
            }
        } else {
            sendErrorResponse(exchange, 400, "Некорректный запрос");
        }
    }

    private Task getTaskById(HttpExchange exchange, String[] pathParts) throws IOException {
        try {
            int taskId = Integer.parseInt(pathParts[2]);
            return taskManager.getTask(taskId);
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Некорректный запрос: " + e.getMessage());
        } catch (EntityNotFoundException e) {
            sendErrorResponse(exchange, 404, e.getMessage());
        }
        return null;
    }

    private Task parseTaskFromJson(HttpExchange exchange, String requestBody) throws IOException {
        try {
            JsonElement jsonElement = JsonParser.parseString(requestBody);
            boolean isRequestBodyValid = JsonValidator.isTaskJsonValid(jsonElement);
            if (!isRequestBodyValid) {
                sendErrorResponse(exchange, 400, "Некорректное тело запроса");
                return null;
            }
            return gson.fromJson(jsonElement, Task.class);
        } catch (JsonSyntaxException e) {
            sendErrorResponse(exchange, 400, "Некорректное тело запроса: " + e.getMessage());
        } catch (DateTimeParseException e) {
            sendErrorResponse(exchange, 400, "Некорректный формат даты: " + e.getMessage());
        }
        return null;
    }

    private void createTask(HttpExchange exchange, Task task) throws IOException {
        try {
            int taskId = taskManager.createTask(task);
            Task taskFromManager = taskManager.getTask(taskId);
            String responseBody = gson.toJson(taskFromManager);
            sendResponse(exchange, 200, responseBody);
        } catch (EntityIntersectionException e) {
            sendErrorResponse(exchange, 406, e.getMessage());
        }
    }

    private void updateTask(HttpExchange exchange, Task task) throws IOException {
        try {
            boolean isUpdated = taskManager.updateTask(task);
            if (isUpdated) {
                sendResponse(exchange, 201, "{}");
            }
        } catch (EntityNotFoundException e) {
            sendErrorResponse(exchange, 404, e.getMessage());
        } catch (EntityIntersectionException e) {
            sendErrorResponse(exchange, 406, e.getMessage());
        }
    }
}