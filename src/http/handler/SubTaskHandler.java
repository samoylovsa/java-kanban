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
import tasks.SubTask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeParseException;
import java.util.List;

public class SubTaskHandler extends BaseHttpHandler implements HttpHandler {

    private static final String PATH_NAME = "subtasks";

    public SubTaskHandler(TaskManager taskManager) {
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
            List<SubTask> subTasks = taskManager.getSubTasks();
            String responseBody = gson.toJson(subTasks);
            sendResponse(exchange, 200, responseBody);
        } else if (isPathValid(pathParts, pathName, 3)) {
            SubTask subTask = getSubTaskById(exchange, pathParts);
            if (subTask != null) {
                String responseBody = gson.toJson(subTask);
                sendResponse(exchange, 200, responseBody);
            }
        } else {
            sendErrorResponse(exchange, 400, "Ошибка в запросе");
        }
    }

    private void handlePostRequest(HttpExchange exchange, String[] pathParts, String pathName, String requestBody) throws IOException {
        if (isPathValid(pathParts, pathName, 2)) {
            SubTask subTask = parseSubTaskFromJson(exchange, requestBody);
            if (subTask != null) {
                JsonElement jsonElement = JsonParser.parseString(requestBody);
                if (jsonElement.getAsJsonObject().has("id")) {
                    updateSubTask(exchange, subTask);
                } else {
                    createSubTask(exchange, subTask);
                }
            }
        } else {
            sendErrorResponse(exchange, 400, "Некорректный запрос");
        }
    }

    private void handleDeleteRequest(HttpExchange exchange, String[] pathParts, String pathName) throws IOException {
        if (isPathValid(pathParts, pathName, 3)) {
            SubTask subTask = getSubTaskById(exchange, pathParts);
            if (subTask != null) {
                taskManager.deleteSubTask(subTask.getId());
                sendResponse(exchange, 201, "{}");
            }
        } else {
            sendErrorResponse(exchange, 400, "Некорректный запрос");
        }
    }

    private SubTask parseSubTaskFromJson(HttpExchange exchange, String requestBody) throws IOException {
        try {
            JsonElement jsonElement = JsonParser.parseString(requestBody);
            boolean isRequestBodyValid = JsonValidator.isSubTaskJsonValid(jsonElement);
            if (!isRequestBodyValid) {
                sendErrorResponse(exchange, 400, "Некорректное тело запроса");
                return null;
            }
            return gson.fromJson(jsonElement, SubTask.class);
        } catch (JsonSyntaxException e) {
            sendErrorResponse(exchange, 400, "Некорректное тело запроса: " + e.getMessage());
        } catch (DateTimeParseException e) {
            sendErrorResponse(exchange, 400, "Некорректный формат даты: " + e.getMessage());
        }
        return null;
    }

    private void createSubTask(HttpExchange exchange, SubTask subTask) throws IOException {
        try {
            int subTaskId = taskManager.createSubTask(subTask);
            SubTask subTaskFromManager = findSubTaskInManager(subTaskId);
            String responseBody = gson.toJson(subTaskFromManager);
            sendResponse(exchange, 200, responseBody);
        } catch (EntityIntersectionException e) {
            sendErrorResponse(exchange, 406, e.getMessage());
        } catch (EntityNotFoundException e) {
            sendErrorResponse(exchange, 404, e.getMessage());
        }
    }

    private void updateSubTask(HttpExchange exchange, SubTask subTask) throws IOException {
        try {
            boolean isUpdated = taskManager.updateSubTask(subTask);
            if (isUpdated) {
                sendResponse(exchange, 201, "{}");
            }
        } catch (EntityNotFoundException e) {
            sendErrorResponse(exchange, 404, e.getMessage());
        } catch (EntityIntersectionException e) {
            sendErrorResponse(exchange, 406, e.getMessage());
        }
    }

    private SubTask getSubTaskById(HttpExchange exchange, String[] pathParts) throws IOException {
        try {
            int subTaskId = Integer.parseInt(pathParts[2]);
            return taskManager.getSubTask(subTaskId);
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Некорректный запрос: " + e.getMessage());
        } catch (EntityNotFoundException e) {
            sendErrorResponse(exchange, 404, e.getMessage());
        }
        return null;
    }

    private SubTask findSubTaskInManager(int subTaskId) {
        List<SubTask> subTasks = taskManager.getSubTasks();
        for (SubTask subTask : subTasks) {
            if (subTask.getId() == subTaskId) {
                return subTask;
            }
        }
        throw new EntityNotFoundException("Задача с Id " + subTaskId + " не найдена");
    }
}