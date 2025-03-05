package manager;

import exceptions.ManagerSaveException;
import tasks.Epic;
import tasks.SubTask;
import tasks.Task;
import utils.CSVTaskFormatUtils;

import java.io.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    @Override
    public int createTask(Task newTask) {
        int id = super.createTask(newTask);
        save();
        return id;
    }

    @Override
    public int createEpic(Epic newEpic) {
        int id = super.createEpic(newEpic);
        save();
        return id;
    }

    @Override
    public int createSubTask(SubTask newSubTask) {
        int id = super.createSubTask(newSubTask);
        save();
        return id;
    }

    @Override
    public boolean updateTask(Task updatedTask) {
        boolean isUpdated = super.updateTask(updatedTask);
        save();
        return isUpdated;
    }

    @Override
    public boolean updateEpic(Epic updatedEpic) {
        boolean isUpdated = super.updateEpic(updatedEpic);
        save();
        return isUpdated;
    }

    @Override
    public boolean updateSubTask(SubTask updatedSubTask) {
        boolean isUpdated = super.updateSubTask(updatedSubTask);
        save();
        return isUpdated;
    }

    @Override
    public void deleteTask(int taskId) {
        super.deleteTask(taskId);
        save();
    }

    @Override
    public void deleteEpic(int epicId) {
        super.deleteEpic(epicId);
        save();
    }

    @Override
    public void deleteSubTask(int subTaskId) {
        super.deleteSubTask(subTaskId);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubTasks() {
        super.deleteAllSubTasks();
        save();
    }

    private void save() {
        try (Writer writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(CSVTaskFormatUtils.getCSVTitle() + "\n");
            Stream.concat(Stream.concat(tasks.values().stream(), epics.values().stream()), subTasks.values().stream())
                    .map(CSVTaskFormatUtils::toString)
                    .forEach(taskString -> {
                        try {
                            writer.write(taskString + "\n");
                        } catch (IOException e) {
                            throw new ManagerSaveException("Ошибка сохранения данных в файл");
                        }
                    });
        } catch (IOException exception) {
            throw new ManagerSaveException("Ошибка сохранения данных в файл");
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager taskManager = new FileBackedTaskManager(file);
        Set<Integer> taskIds = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    Task task = CSVTaskFormatUtils.fromString(line);
                    taskIds.add(task.getId());
                    if (task instanceof Epic epic) {
                        taskManager.epics.put(epic.getId(), epic);
                    } else if (task instanceof SubTask subTask) {
                        taskManager.subTasks.put(subTask.getId(), subTask);
                        taskManager.addToPrioritizedTasks(subTask);
                    } else {
                        taskManager.tasks.put(task.getId(), task);
                        taskManager.addToPrioritizedTasks(task);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Не удалось прочитать данные из файла");
        }

        taskManager.subTasks.values()
                .forEach(subTask -> {
                    int epicId = subTask.getEpicId();
                    int subTaskId = subTask.getId();
                    taskManager.epics.get(epicId)
                            .addSubTaskId(subTaskId);
                });

        if (!taskIds.isEmpty()) {
            taskManager.idCounter = Collections.max(taskIds);
        }

        return taskManager;
    }

    private void addToPrioritizedTasks(Task task) {
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }
}