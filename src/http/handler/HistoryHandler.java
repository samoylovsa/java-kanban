package http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import tasks.Task;

import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {

    private static final String PATH_NAME = "history";

    public HistoryHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.print("Начинается обработка запроса: ");

        String requestMethod = exchange.getRequestMethod();
        String requestPath = exchange.getRequestURI().getPath();
        String[] pathParts = requestPath.split("/");

        System.out.println(requestMethod.toUpperCase() + " " + requestPath);

        switch (requestMethod) {
            case "GET":
                handleGetRequest(exchange, pathParts, PATH_NAME);
                break;
            default:
                sendErrorResponse(exchange, 405, "Метод не поддерживается");
        }
    }

    private void handleGetRequest(HttpExchange exchange, String[] pathParts, String pathName) throws IOException {
        if (isPathValid(pathParts, pathName, 2)) {
            List<Task> history = taskManager.getHistory();
            String responseBody = gson.toJson(history);
            sendResponse(exchange, 200, responseBody);
        } else {
            sendErrorResponse(exchange, 400, "Ошибка в запросе");
        }
    }
}