import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TaskManager {

    private int idCounter = 0;
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Epic> epics;
    private final HashMap<Integer, SubTask> subTasks;

    TaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subTasks = new HashMap<>();
    }

    public final int createTask(Task task) {
        task.setId(generateId());
        tasks.put(task.getId(), task);

        return task.getId();
    }

    public final int createEpic(Epic epic) {
        epic.setId(generateId());
        updateEpicStatus(epic);
        epics.put(epic.getId(), epic);

        return epic.getId();
    }

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

    public final Task getTask(int id) {
        return tasks.get(id);
    }

    public final Epic getEpic(int id) {
        return epics.get(id);
    }

    public final SubTask getSubTask(int id) {
        return subTasks.get(id);
    }

    public final ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public final ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    public final ArrayList<SubTask> getSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    public final void deleteTask(int id) {
        tasks.remove(id);
    }

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

    public final void deleteAllEntities() {
        tasks.clear();
        epics.clear();
        subTasks.clear();
    }

    public final void deleteAllTasks() {
        tasks.clear();
    }

    public final void deleteAllEpics() {
        epics.clear();
        subTasks.clear();
    }

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
