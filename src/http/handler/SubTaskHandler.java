package http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;

import java.io.IOException;

public class SubTaskHandler extends BaseHttpHandler implements HttpHandler {

    public SubTaskHandler(TaskManager taskManager) {
        super(taskManager);    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

    }
}
