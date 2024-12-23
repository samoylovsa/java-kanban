import java.util.ArrayList;
import java.util.HashMap;

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
            epics.get(epicId)
                    .getSubTaskIdList()
                    .add(subTask.getId());
            subTasks.put(subTask.getId(), subTask);
            updateEpicStatus(epics.get(epicId));

            return subTask.getId();
        } else {
            System.out.println("Пустой список эпиков или не найден эпик с epicId: " + epicId);
            return -1;
        }
    }

    public final Task updateTask(int id, Task task) {
        Task existingTask = tasks.get(id);
        if (existingTask != null) {
            task.setId(id);
            tasks.put(id, task);
            return tasks.get(task.getId());
        } else {
            System.out.println("Не найдена задача с id: " + id);
            return null;
        }
    }

    public final int updateEpic(int id, Epic epic) {
        Epic existingEpic = epics.get(id);
        if (existingEpic != null) {
            epic.setId(id);
            epic.setStatus(existingEpic.getStatus());
            epic.setSubTaskIdList(existingEpic.getSubTaskIdList());
            epics.put(id, epic);
            return epic.getId();
        } else {
            System.out.println("Не найден эпик с id: " + id);
            return -1;
        }
    }

    public final int updateSubTask(int subTaskId, SubTask subTask) {
        SubTask existingSubTask = subTasks.get(subTaskId);
        if (existingSubTask != null) {
            int originalEpicId = existingSubTask.getEpicId();
            subTask.setId(subTaskId);
            subTask.setEpicId(originalEpicId);
            subTasks.put(subTaskId, subTask);
            updateEpicStatus(epics.get(originalEpicId));
            return subTask.getId();
        } else {
            System.out.println("Не найдена подзадача с id: " + subTaskId);
            return -1;
        }
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
        ArrayList<Task> taskList = new ArrayList<>(tasks.values());

        return taskList;
    }

    public final ArrayList<Epic> getEpics() {
        ArrayList<Epic> epicList = new ArrayList<>(epics.values());

        return epicList;
    }

    public final ArrayList<SubTask> getSubTasks() {
        ArrayList<SubTask> subTaskList = new ArrayList<>(subTasks.values());

        return subTaskList;
    }

    public final void deleteTask(int id) {
        tasks.remove(id);
    }

    public final void deleteEpic(int id) {
        ArrayList<Integer> subTaskIdList = epics.get(id).getSubTaskIdList();
        for (Integer subTaskId : subTaskIdList) {
            subTasks.remove(subTaskId);
        }
        epics.remove(id);
    }

    public final void deleteSubTask(int id) {
        int epicId = subTasks.get(id).getEpicId();
        Epic epic = epics.get(epicId);
        epic.getSubTaskIdList().remove((Integer) id);
        subTasks.remove(id);
        updateEpicStatus(epic);
    }

    public final ArrayList<SubTask> getSubTasksByEpic(Epic epic) {
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
        return idCounter++;
    }
}
