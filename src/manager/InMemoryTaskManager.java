package manager;

import tasks.Epic;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    protected int idCounter = 0;
    protected final Map<Integer, Task> tasks;
    protected final Map<Integer, Epic> epics;
    protected final Map<Integer, SubTask> subTasks;
    protected final HistoryManager historyManager;
    protected final Set<Task> prioritizedTasks;

    InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subTasks = new HashMap<>();
        historyManager = Manager.getDefaultHistory();
        prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));
    }

    @Override
    public int createTask(Task newTask) {
        newTask.setId(generateId());
        tasks.put(newTask.getId(), newTask);
        addToPrioritizedTasks(newTask);

        return newTask.getId();
    }

    @Override
    public int createEpic(Epic newEpic) {
        newEpic.setId(generateId());
        updateEpicStatus(newEpic);
        epics.put(newEpic.getId(), newEpic);

        return newEpic.getId();
    }

    @Override
    public int createSubTask(SubTask newSubTask) {
        int epicId = newSubTask.getEpicId();
        if (epics.containsKey(epicId)) {
            newSubTask.setId(generateId());
            int subTaskId = newSubTask.getId();
            epics.get(epicId).addSubTaskId(subTaskId);
            subTasks.put(subTaskId, newSubTask);
            updateEpicStatus(epics.get(epicId));
            updateEpicTime(epics.get(epicId));
            addToPrioritizedTasks(newSubTask);

            return subTaskId;
        } else {
            System.out.println("Не найден эпик с epicId: " + epicId);
            return -1;
        }
    }

    @Override
    public boolean updateTask(Task updatedTask) {
        int updatedTaskId = updatedTask.getId();
        Task existingTask = tasks.get(updatedTaskId);
        if (existingTask != null) {
            tasks.put(updatedTaskId, updatedTask);
            prioritizedTasks.remove(existingTask);
            addToPrioritizedTasks(updatedTask);

            return true;
        } else {
            System.out.println("Не найдена задача с updatedTaskId: " + updatedTaskId);
            return false;
        }
    }

    @Override
    public boolean updateEpic(Epic updatedEpic) {
        int updatedEpicId = updatedEpic.getId();
        Epic existingEpic = epics.get(updatedEpicId);
        if (existingEpic != null) {
            existingEpic.setName(updatedEpic.getName());
            existingEpic.setDescription(updatedEpic.getDescription());
            return true;
        } else {
            System.out.println("Не найден эпик с updatedEpicId: " + updatedEpicId);
            return false;
        }
    }

    @Override
    public boolean updateSubTask(SubTask updatedSubTask) {
        int updatedSubTaskId = updatedSubTask.getId();
        SubTask existingSubTask = subTasks.get(updatedSubTaskId);
        if (existingSubTask == null) {
            System.out.println("Не найдена подзадача с id: " + updatedSubTaskId);
            return false;
        }
        int updatedSubTaskEpicId = updatedSubTask.getEpicId();
        Epic existingEpic = epics.get(updatedSubTaskEpicId);
        if (existingEpic == null) {
            System.out.println("Не найден эпик с таким epicId: " + updatedSubTaskEpicId);
            return false;
        }
        int existingSubTaskEpicId = existingSubTask.getEpicId();
        if (updatedSubTaskEpicId != existingSubTaskEpicId) {
            System.out.println("epicId новой подзадачи не равен epicId существующей подзадачи");
            return false;
        }
        subTasks.put(updatedSubTaskId, updatedSubTask);
        updateEpicStatus(existingEpic);
        updateEpicTime(existingEpic);
        prioritizedTasks.remove(existingSubTask);
        addToPrioritizedTasks(updatedSubTask);

        return true;
    }

    @Override
    public final Task getTask(int taskId) {
        Task task = tasks.get(taskId);
        if (task != null) {
            historyManager.add(task);
        }

        return task;
    }

    @Override
    public final Epic getEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic != null) {
            historyManager.add(epic);
        }

        return epic;
    }

    @Override
    public final SubTask getSubTask(int subTaskId) {
        SubTask subTask = subTasks.get(subTaskId);
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
    public void deleteTask(int taskId) {
        Task task = tasks.get(taskId);
        tasks.remove(taskId);
        historyManager.remove(taskId);
        prioritizedTasks.remove(task);
    }

    @Override
    public void deleteEpic(int epicId) {
        if (!epics.containsKey(epicId)) {
            System.out.println("Не существует эпика с указанным epicId: " + epicId);
            return;
        }
        ArrayList<Integer> subTaskIdList = epics.get(epicId).getSubTaskIdList();
        for (Integer subTaskId : subTaskIdList) {
            subTasks.remove(subTaskId);
            historyManager.remove(subTaskId);
        }
        epics.remove(epicId);
        historyManager.remove(epicId);
    }

    @Override
    public void deleteSubTask(int subTaskId) {
        SubTask subTask = subTasks.get(subTaskId);
        if (subTask == null) {
            System.out.println("Не существует подзадачи с указанным subTaskId: " + subTaskId);
            return;
        }
        int epicId = subTask.getEpicId();
        Epic epic = epics.get(epicId);
        if (epic == null) {
            System.out.println("Для подзадачи с subTaskId: " + subTaskId + " не найден эпик с subTaskId: " + epicId);
            subTasks.remove(subTaskId);
            return;
        }
        epic.deleteSubTaskId(subTaskId);
        subTasks.remove(subTaskId);
        updateEpicStatus(epic);
        updateEpicTime(epic);
        historyManager.remove(subTaskId);
        prioritizedTasks.remove(subTask);
    }

    @Override
    public void deleteAllTasks() {
        for (Integer id : tasks.keySet()) {
            historyManager.remove(id);
            prioritizedTasks.remove(tasks.get(id));
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
            prioritizedTasks.remove(subTasks.get(id));
        }
        subTasks.clear();
        for (Epic processedEpic : processedEpics) {
            processedEpic.deleteAllSubTaskId();
            updateEpicStatus(processedEpic);
            updateEpicTime(processedEpic);
        }
    }

    @Override
    public final List<SubTask> getSubTasksByEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            System.out.println("Не существует эпика с epicId: " + epicId);
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

    @Override
    public final List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private void addToPrioritizedTasks(Task task) {
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    private void updateEpicStatus(Epic epic) {
        ArrayList<Integer> subTasksIdList = epic.getSubTaskIdList();
        if (subTasksIdList.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }
        int subTasksQuantity = subTasksIdList.size();
        int newStatusCounter = 0;
        int doneStatusCounter = 0;
        for (Integer id : subTasksIdList) {
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

    private void updateEpicTime(Epic epic) {
        ArrayList<Integer> subTasksIdList = epic.getSubTaskIdList();
        if (subTasksIdList.isEmpty()) {
            epic.resetTime();
            return;
        }

        Optional<LocalDateTime> minStartTime = subTasksIdList.stream()
                .map(subTasks::get)
                .filter(subTask -> subTask.getStartTime() != null)
                .map(SubTask::getStartTime)
                .min(LocalDateTime::compareTo);

        Optional<LocalDateTime> maxEndTime = subTasksIdList.stream()
                .map(subTasks::get)
                .filter(subTask -> subTask.getEndTime() != null)
                .map(SubTask::getEndTime)
                .max(LocalDateTime::compareTo);

        Duration totalDuration = subTasksIdList.stream()
                .map(subTasks::get)
                .filter(subTask -> subTask.getStartTime() != null && subTask.getEndTime() != null)
                .map(SubTask::getDuration)
                .reduce(Duration.ZERO, Duration::plus);

        if (minStartTime.isPresent() && maxEndTime.isPresent()) {
            LocalDateTime startTime = minStartTime.get();
            LocalDateTime endTime = maxEndTime.get();
            epic.setStartTime(startTime);
            epic.setEndTime(endTime);
            epic.setDuration(totalDuration);
        } else {
            epic.resetTime();
        }
    }

    private int generateId() {
        return ++idCounter;
    }
}