package manager;

import exceptions.ManagerSaveException;
import tasks.Epic;
import tasks.SubTask;
import tasks.Task;
import utils.CSVTaskFormatUtils;

import java.io.*;
import java.util.*;

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
        List<Integer> taskIds = new ArrayList<>();
        List<Integer> subTaskIds = new ArrayList<>();
        Map<Integer, List<Integer>> epicToSubTaskIds = new HashMap<>();

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
                        int subTaskId = subTask.getId();
                        int epicId = subTask.getEpicId();
                        taskManager.subTasks.put(subTaskId, subTask);
                        subTaskIds.add(subTaskId);
                        epicToSubTaskIds.put(epicId, subTaskIds);
                    } else {
                        taskManager.tasks.put(task.getId(), task);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Не удалось прочитать файл, возвращаю менеджер без данных");
            return taskManager;
        }

        taskManager.idCounter = Collections.max(taskIds);
        for (Map.Entry<Integer, List<Integer>> entry : epicToSubTaskIds.entrySet()) {
            for (Integer subTaskId : entry.getValue()) {
                taskManager.epics.get(entry.getKey()).addSubTaskId(subTaskId);
            }
        }

        return taskManager;
    }
}