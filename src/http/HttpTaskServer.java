package http;

import com.sun.net.httpserver.HttpServer;
import http.handler.*;
import manager.Manager;
import manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {

    private static final int PORT = 8080;
    private final HttpServer httpServer;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        httpServer.createContext("/tasks", new TaskHandler(taskManager));
        httpServer.createContext("/subtasks", new SubTaskHandler(taskManager));
        httpServer.createContext("/epics", new EpicHandler(taskManager));
        httpServer.createContext("/history", new HistoryHandler(taskManager));
        httpServer.createContext("/prioritized", new PrioritizedHandler(taskManager));
    }

    public void start() {
        System.out.println("Запускаем HTTP-сервер на " + PORT + " порту!");
        httpServer.start();
    }

    public void stop(int delay) {
        httpServer.stop(delay);
        System.out.println("HTTP-сервер остановлен на " + PORT + " порту!");
    }

    public static void main(String[] args) throws IOException {
        TaskManager taskManager = Manager.getDefault();
        HttpTaskServer taskServer = new HttpTaskServer(taskManager);
        taskServer.start();
        System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
    }
}