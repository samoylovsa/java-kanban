import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class InMemoryTaskManager implements TaskManager {

    private int idCounter = 0;
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Epic> epics;
    private final HashMap<Integer, SubTask> subTasks;
    private final HistoryManager historyManager;

    InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subTasks = new HashMap<>();
        historyManager = Manager.getDefaultHistory();
    }

    @Override
    public final int createTask(Task task) {
        task.setId(generateId());
        tasks.put(task.getId(), task);

        return task.getId();
    }

    @Override
    public final int createEpic(Epic epic) {
        epic.setId(generateId());
        updateEpicStatus(epic);
        epics.put(epic.getId(), epic);

        return epic.getId();
    }

    @Override
    public final int createSubTask(SubTask subTask) {
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
    public final boolean updateTask(Task task) {
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
    public final boolean updateEpic(Epic epic) {
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
    public final boolean updateSubTask(SubTask newSubTask) {
        int newSubTaskId = newSubTask.getId();
        SubTask existingSubTask = subTasks.get(newSubTaskId);
        if (existingSubTask == null) {
            System.out.println("Не найдена подзадача с id: " + newSubTaskId);
            return false;
        }
        int newSubTaskEpicId = newSubTask.getEpicId();
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
        subTasks.put(newSubTaskId, newSubTask);
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
    public final ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public final ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public final ArrayList<SubTask> getSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    @Override
    public final void deleteTask(int id) {
        tasks.remove(id);
    }

    @Override
    public final void deleteEpic(int id) {
        if (!epics.containsKey(id)) {
            System.out.println("Не существует эпика с указанным id: " + id);
            return;
        }
        ArrayList<Integer> subTaskIdList = epics.get(id).getSubTaskIdList();
        for (Integer subTaskId : subTaskIdList) {
            subTasks.remove(subTaskId);
        }
        epics.remove(id);
    }

    @Override
    public final void deleteSubTask(int id) {
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
    }

    @Override
    public final void deleteAllEntities() {
        tasks.clear();
        epics.clear();
        subTasks.clear();
    }

    @Override
    public final void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public final void deleteAllEpics() {
        epics.clear();
        subTasks.clear();
    }

    @Override
    public final void deleteAllSubTasks() {
        Set<Epic> processedEpics = new HashSet<>();
        for (SubTask subTask : subTasks.values()) {
            int epicId = subTask.getEpicId();
            Epic epic = epics.get(epicId);
            if (epic != null) {
                processedEpics.add(epic);
            }
        }
        subTasks.clear();
        for (Epic processedEpic : processedEpics) {
            processedEpic.deleteAllSubTaskId();
            updateEpicStatus(processedEpic);
        }
    }

    @Override
    public final ArrayList<SubTask> getSubTasksByEpic(int id) {
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
