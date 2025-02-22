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

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    @Override
    public int createTask(Task task) {
        int id = super.createTask(task);
        save();
        return id;
    }

    @Override
    public int createEpic(Epic epic) {
        int id = super.createEpic(epic);
        save();
        return id;
    }

    @Override
    public int createSubTask(SubTask subTask) {
        int id = super.createSubTask(subTask);
        save();
        return id;
    }

    @Override
    public boolean updateTask(Task task) {
        boolean isUpdated = super.updateTask(task);
        save();
        return isUpdated;
    }

    @Override
    public boolean updateEpic(Epic epic) {
        boolean isUpdated = super.updateEpic(epic);
        save();
        return isUpdated;
    }

    @Override
    public boolean updateSubTask(SubTask subTask) {
        boolean isUpdated = super.updateSubTask(subTask);
        save();
        return isUpdated;
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubTask(int id) {
        super.deleteSubTask(id);
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
            for (Task task : tasks.values()) {
                writer.write(CSVTaskFormatUtils.toString(task) + "\n");
            }
            for (Epic epic : epics.values()) {
                writer.write(CSVTaskFormatUtils.toString(epic) + "\n");
            }
            for (SubTask subTask : subTasks.values()) {
                writer.write(CSVTaskFormatUtils.toString(subTask) + "\n");
            }
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
                    } else {
                        taskManager.tasks.put(task.getId(), task);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Не удалось прочитать данные из файла");
        }

        for (SubTask subTask : taskManager.subTasks.values()) {
            int epicId = subTask.getEpicId();
            int subTaskId = subTask.getId();
            taskManager.epics.get(epicId).addSubTaskId(subTaskId);
        }

        if (!taskIds.isEmpty()) {
            taskManager.idCounter = Collections.max(taskIds);
        }

        return taskManager;
    }
}