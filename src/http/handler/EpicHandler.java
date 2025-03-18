package http.handler;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.EntityNotFoundException;
import http.utils.JsonValidator;
import manager.TaskManager;
import tasks.Epic;
import tasks.SubTask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeParseException;
import java.util.List;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {

    private static final String PATH_NAME = "epics";

    public EpicHandler(TaskManager taskManager) {
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
            List<Epic> epics = taskManager.getEpics();
            String responseBody = gson.toJson(epics);
            sendResponse(exchange, 200, responseBody);
        } else if (isPathValid(pathParts, pathName, 3)) {
            Epic epic = getEpicById(exchange, pathParts);
            if (epic != null) {
                String responseBody = gson.toJson(epic);
                sendResponse(exchange, 200, responseBody);
            }
        } else if (isPathValid(pathParts, pathName, 4) && pathParts[3].equals("subtasks")) {
            Epic epic = getEpicById(exchange, pathParts);
            if (epic != null) {
                List<SubTask> subTasks = getSubTasksByEpic(exchange, epic.getId());
                String responseBody = gson.toJson(subTasks);
                sendResponse(exchange, 200, responseBody);
            }
        } else {
            sendErrorResponse(exchange, 400, "Ошибка в запросе");
        }
    }

    private void handlePostRequest(HttpExchange exchange, String[] pathParts, String pathName, String requestBody) throws IOException {
        if (isPathValid(pathParts, pathName, 2)) {
            Epic epic = parseEpicFromJson(exchange, requestBody);
            if (epic != null) {
                JsonElement jsonElement = JsonParser.parseString(requestBody);
                if (jsonElement.getAsJsonObject().has("id")) {
                    updateEpic(exchange, epic);
                } else {
                    createEpic(exchange, epic);
                }
            }
        } else {
            sendErrorResponse(exchange, 400, "Некорректный запрос");
        }
    }

    private void handleDeleteRequest(HttpExchange exchange, String[] pathParts, String pathName) throws IOException {
        if (isPathValid(pathParts, pathName, 3)) {
            Epic epic = getEpicById(exchange, pathParts);
            if (epic != null) {
                taskManager.deleteEpic(epic.getId());
                sendResponse(exchange, 201, "{}");
            }
        } else {
            sendErrorResponse(exchange, 400, "Некорректный запрос");
        }
    }

    private Epic getEpicById(HttpExchange exchange, String[] pathParts) throws IOException {
        try {
            int epicId = Integer.parseInt(pathParts[2]);
            return taskManager.getEpic(epicId);
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Некорректный запрос: " + e.getMessage());
        } catch (EntityNotFoundException e) {
            sendErrorResponse(exchange, 404, e.getMessage());
        }
        return null;
    }

    private List<SubTask> getSubTasksByEpic(HttpExchange exchange, int epicId) throws IOException {
        try {
            return taskManager.getSubTasksByEpic(epicId);
        } catch (EntityNotFoundException e) {
            sendErrorResponse(exchange, 404, e.getMessage());
        }
        return List.of();
    }

    private Epic parseEpicFromJson(HttpExchange exchange, String requestBody) throws IOException {
        try {
            JsonElement jsonElement = JsonParser.parseString(requestBody);
            boolean isRequestBodyValid = JsonValidator.isEpicJsonValid(jsonElement);
            if (!isRequestBodyValid) {
                sendErrorResponse(exchange, 400, "Некорректное тело запроса");
                return null;
            }
            return gson.fromJson(jsonElement, Epic.class);
        } catch (JsonSyntaxException e) {
            sendErrorResponse(exchange, 400, "Некорректное тело запроса: " + e.getMessage());
        } catch (DateTimeParseException e) {
            sendErrorResponse(exchange, 400, "Некорректный формат даты: " + e.getMessage());
        }
        return null;
    }

    private void createEpic(HttpExchange exchange, Epic epic) throws IOException {
        try {
            int epicId = taskManager.createEpic(epic);
            Epic epicFromManager = findEpicInManager(epicId);
            String responseBody = gson.toJson(epicFromManager);
            sendResponse(exchange, 200, responseBody);
        } catch (EntityNotFoundException e) {
            sendErrorResponse(exchange, 404, e.getMessage());
        }
    }

    private void updateEpic(HttpExchange exchange, Epic epic) throws IOException {
        try {
            boolean isUpdated = taskManager.updateEpic(epic);
            if (isUpdated) {
                sendResponse(exchange, 201, "{}");
            }
        } catch (EntityNotFoundException e) {
            sendErrorResponse(exchange, 404, e.getMessage());
        }
    }

    private Epic findEpicInManager(int epicId) {
        List<Epic> epics = taskManager.getEpics();
        for (Epic epic : epics) {
            if (epic.getId() == epicId) {
                return epic;
            }
        }
        throw new EntityNotFoundException("Задача с Id " + epicId + " не найдена");
    }
}