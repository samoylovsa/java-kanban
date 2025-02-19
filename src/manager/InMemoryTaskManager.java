package manager;

import tasks.Epic;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    protected int idCounter = 0;
    protected final HashMap<Integer, Task> tasks;
    protected final HashMap<Integer, Epic> epics;
    protected final HashMap<Integer, SubTask> subTasks;
    protected final HistoryManager historyManager;

    InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subTasks = new HashMap<>();
        historyManager = Manager.getDefaultHistory();
    }

    @Override
    public int createTask(Task task) {
        task.setId(generateId());
        tasks.put(task.getId(), task);

        return task.getId();
    }

    @Override
    public int createEpic(Epic epic) {
        epic.setId(generateId());
        updateEpicStatus(epic);
        epics.put(epic.getId(), epic);

        return epic.getId();
    }

    @Override
    public int createSubTask(SubTask subTask) {
        int epicId = subTask.getEpicId();
        if (!epics.isEmpty() && epics.containsKey(epicId)) {
            subTask.setId(generateId());
            int subTaskId = subTask.getId();
            epics.get(epicId).addSubTaskId(subTaskId);
            subTasks.put(subTaskId, subTask);
            updateEpicStatus(epics.get(epicId));

            return subTaskId;
        } else {
            System.out.println("Пустой список эпиков или не найден эпик с epicId: " + epicId);
            return -1;
        }
    }

    @Override
    public boolean updateTask(Task task) {
        int taskId = task.getId();
        Task existingTask = tasks.get(taskId);
        if (existingTask != null) {
            tasks.put(taskId, task);
            return true;
        } else {
            System.out.println("Не найдена задача с taskId: " + taskId);
            return false;
        }
    }

    @Override
    public boolean updateEpic(Epic epic) {
        int epicId = epic.getId();
        Epic existingEpic = epics.get(epicId);
        if (existingEpic != null) {
            existingEpic.setName(epic.getName());
            existingEpic.setDescription(epic.getDescription());
            return true;
        } else {
            System.out.println("Не найден эпик с epicId: " + epicId);
            return false;
        }
    }

    @Override
    public boolean updateSubTask(SubTask subTask) {
        int newSubTaskId = subTask.getId();
        SubTask existingSubTask = subTasks.get(newSubTaskId);
        if (existingSubTask == null) {
            System.out.println("Не найдена подзадача с id: " + newSubTaskId);
            return false;
        }
        int newSubTaskEpicId = subTask.getEpicId();
        Epic existingEpic = epics.get(newSubTaskEpicId);
        if (existingEpic == null) {
            System.out.println("Не найден эпик с таким epicId: " + newSubTaskEpicId);
            return false;
        }
        int existingSubTaskEpicId = existingSubTask.getEpicId();
        if (newSubTaskEpicId != existingSubTaskEpicId) {
            System.out.println("epicId новой подзадачи не равен epicId существующей подзадачи");
            return false;
        }
        subTasks.put(newSubTaskId, subTask);
        updateEpicStatus(existingEpic);

        return true;
    }

    @Override
    public final Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }

        return task;
    }

    @Override
    public final Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }

        return epic;
    }

    @Override
    public final SubTask getSubTask(int id) {
        SubTask subTask = subTasks.get(id);
        if (subTask != null) {
            historyManager.add(subTask);
        }
        return subTask;
    }

    @Override
    public final List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public final List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public final List<SubTask> getSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    @Override
    public void deleteTask(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteEpic(int id) {
        if (!epics.containsKey(id)) {
            System.out.println("Не существует эпика с указанным id: " + id);
            return;
        }
        ArrayList<Integer> subTaskIdList = epics.get(id).getSubTaskIdList();
        for (Integer subTaskId : subTaskIdList) {
            subTasks.remove(subTaskId);
            historyManager.remove(subTaskId);
        }
        epics.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteSubTask(int id) {
        SubTask subTask = subTasks.get(id);
        if (subTask == null) {
            System.out.println("Не существует подзадачи с указанным id: " + id);
            return;
        }
        int epicId = subTask.getEpicId();
        Epic epic = epics.get(epicId);
        if (epic == null) {
            System.out.println("Для подзадачи с id: " + id + " не найден эпик с id: " + epicId);
            subTasks.remove(id);
            return;
        }
        epic.deleteSubTaskId(id);
        subTasks.remove(id);
        updateEpicStatus(epic);
        historyManager.remove(id);
    }

    @Override
    public void deleteAllTasks() {
        for (Integer id : tasks.keySet()) {
            historyManager.remove(id);
        }
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        for (Integer id : epics.keySet()) {
            historyManager.remove(id);
        }
        epics.clear();

        for (Integer id : subTasks.keySet()) {
            historyManager.remove(id);
        }
        subTasks.clear();
    }

    @Override
    public void deleteAllSubTasks() {
        Set<Epic> processedEpics = new HashSet<>();
        for (SubTask subTask : subTasks.values()) {
            int epicId = subTask.getEpicId();
            Epic epic = epics.get(epicId);
            if (epic != null) {
                processedEpics.add(epic);
            }
        }
        for (Integer id : subTasks.keySet()) {
            historyManager.remove(id);
        }
        subTasks.clear();
        for (Epic processedEpic : processedEpics) {
            processedEpic.deleteAllSubTaskId();
            updateEpicStatus(processedEpic);
        }
    }

    @Override
    public final List<SubTask> getSubTasksByEpic(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            System.out.println("Не существует эпика с id: " + id);
            return null;
        }
        ArrayList<Integer> subTaskIdList = epic.getSubTaskIdList();
        ArrayList<SubTask> subTasksByEpic = new ArrayList<>();
        for (Integer subTaskId : subTaskIdList) {
            subTasksByEpic.add(subTasks.get(subTaskId));
        }

        return subTasksByEpic;
    }

    @Override
    public final void printAllTasks() {
        for (Task task : tasks.values()) {
            System.out.println(task);
        }
        for (Task epic : epics.values()) {
            System.out.println(epic);
        }
        for (Task subTask : subTasks.values()) {
            System.out.println(subTask);
        }
    }

    @Override
    public final List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private void updateEpicStatus(Epic epic) {
        ArrayList<Integer> subTasksList = epic.getSubTaskIdList();
        if (subTasksList.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }
        int subTasksQuantity = subTasksList.size();
        int newStatusCounter = 0;
        int doneStatusCounter = 0;
        for (Integer id : subTasksList) {
            Status subTaskStatus = subTasks.get(id).getStatus();
            if (subTaskStatus.equals(Status.NEW)) {
                newStatusCounter++;
            } else if (subTaskStatus.equals(Status.DONE)) {
                doneStatusCounter++;
            }
        }
        if (newStatusCounter == subTasksQuantity) {
            epic.setStatus(Status.NEW);
        } else if (doneStatusCounter == subTasksQuantity) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    private int generateId() {
        return ++idCounter;
    }
}
