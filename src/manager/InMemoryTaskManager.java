package manager;

import tasks.Epic;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

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
        boolean isIntersectByTime = prioritizedTasks.stream()
                .anyMatch(existingTask -> isIntersectByTime(existingTask, newTask));
        if (isIntersectByTime) {
            throw new IllegalArgumentException("Создаваемая задача пересекается по времени с уже существующими задачами");
        }

        newTask.setId(generateId());
        tasks.put(newTask.getId(), newTask);
        if (newTask.getStartTime() != null && newTask.getEndTime() != null) {
            prioritizedTasks.add(newTask);
        }

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
        boolean isIntersectByTime = prioritizedTasks.stream()
                .anyMatch(existingSubTask -> isIntersectByTime(existingSubTask, newSubTask));
        if (isIntersectByTime) {
            throw new IllegalArgumentException("Создаваемая подзадача пересекается по времени с уже существующими подзадачами");
        }

        int epicId = newSubTask.getEpicId();
        if (epics.containsKey(epicId)) {
            newSubTask.setId(generateId());
            int subTaskId = newSubTask.getId();
            epics.get(epicId).addSubTaskId(subTaskId);
            subTasks.put(subTaskId, newSubTask);
            if (newSubTask.getStartTime() != null && newSubTask.getEndTime() != null) {
                prioritizedTasks.add(newSubTask);
            }
            updateEpicStatus(epics.get(epicId));
            updateEpicTime(epics.get(epicId));

            return subTaskId;
        } else {
            throw new IllegalArgumentException("Не найден эпик с epicId: " + epicId);
        }
    }

    @Override
    public boolean updateTask(Task updatedTask) {
        int updatedTaskId = updatedTask.getId();
        final Task existingTask = tasks.get(updatedTaskId);

        if (existingTask == null) {
            throw new IllegalArgumentException("Не найдена задача с taskId: " + updatedTaskId);
        }

        boolean isIntersectByTime = prioritizedTasks.stream()
                .filter(task -> !task.equals(existingTask))
                .anyMatch(task -> isIntersectByTime(task, updatedTask));

        if (isIntersectByTime) {
            throw new IllegalArgumentException("Обновляемая задача пересекается по времени с уже существующими задачами");
        }

        tasks.put(updatedTaskId, updatedTask);
        prioritizedTasks.remove(existingTask);
        if (updatedTask.getStartTime() != null && updatedTask.getEndTime() != null) {
            prioritizedTasks.add(updatedTask);
        }

        return true;
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
            throw new IllegalArgumentException("Не найден эпик с updatedEpicId: " + updatedEpicId);
        }
    }

    @Override
    public boolean updateSubTask(SubTask updatedSubTask) {
        int updatedSubTaskId = updatedSubTask.getId();
        final SubTask existingSubTask = subTasks.get(updatedSubTaskId);
        if (existingSubTask == null) {
            throw new IllegalArgumentException("Не найдена подзадача с id: " + updatedSubTaskId);
        }

        boolean isIntersectByTime = prioritizedTasks.stream()
                .filter(task -> !task.equals(existingSubTask))
                .anyMatch(task -> isIntersectByTime(task, updatedSubTask));

        if (isIntersectByTime) {
            throw new IllegalArgumentException("Обновляемая подзадача пересекается по времени с уже существующими подзадачами");
        }

        int updatedSubTaskEpicId = updatedSubTask.getEpicId();
        Epic existingEpic = epics.get(updatedSubTaskEpicId);
        if (existingEpic == null) {
            throw new IllegalArgumentException("Не найден эпик с таким epicId: " + updatedSubTaskEpicId);
        }

        int existingSubTaskEpicId = existingSubTask.getEpicId();
        if (updatedSubTaskEpicId != existingSubTaskEpicId) {
            throw new IllegalArgumentException("epicId новой подзадачи не равен epicId существующей подзадачи");
        }

        subTasks.put(updatedSubTaskId, updatedSubTask);
        prioritizedTasks.remove(existingSubTask);
        if (updatedSubTask.getStartTime() != null && updatedSubTask.getEndTime() != null) {
            prioritizedTasks.add(updatedSubTask);
        }
        updateEpicStatus(existingEpic);
        updateEpicTime(existingEpic);

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
            throw new IllegalArgumentException("Не существует эпика с указанным epicId: " + epicId);
        }
        ArrayList<Integer> subTaskIdList = epics.get(epicId).getSubTaskIdList();
        subTaskIdList.forEach(subTaskId -> {
            SubTask subTask = subTasks.get(subTaskId);
            if (subTask != null) {
                prioritizedTasks.remove(subTask);
            }
            subTasks.remove(subTaskId);
            historyManager.remove(subTaskId);
        });
        epics.remove(epicId);
        historyManager.remove(epicId);
    }

    @Override
    public void deleteSubTask(int subTaskId) {
        SubTask subTask = subTasks.get(subTaskId);
        if (subTask == null) {
            throw new IllegalArgumentException("Не существует подзадачи с указанным subTaskId: " + subTaskId);
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
        tasks.keySet().forEach(id -> {
            historyManager.remove(id);
            prioritizedTasks.remove(tasks.get(id));
        });
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        epics.keySet().forEach(historyManager::remove);
        epics.clear();

        subTasks.keySet().forEach(historyManager::remove);
        subTasks.values().forEach(prioritizedTasks::remove);
        subTasks.clear();
    }

    @Override
    public void deleteAllSubTasks() {
        Set<Epic> processedEpics = new HashSet<>();

        subTasks.forEach((id, subTask) -> {
            int epicId = subTask.getEpicId();
            Epic epic = epics.get(epicId);
            if (epic != null) {
                processedEpics.add(epic);
            }
            historyManager.remove(id);
            prioritizedTasks.remove(subTask);
        });

        subTasks.clear();

        processedEpics.forEach(processedEpic -> {
            processedEpic.deleteAllSubTaskId();
            updateEpicStatus(processedEpic);
            updateEpicTime(processedEpic);
        });
    }

    @Override
    public final List<SubTask> getSubTasksByEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            throw new IllegalArgumentException("Не существует эпика с epicId: " + epicId);
        }
        ArrayList<Integer> subTaskIdList = epic.getSubTaskIdList();
        ArrayList<SubTask> subTasksByEpic = new ArrayList<>();
        subTaskIdList.forEach(subTaskId -> subTasksByEpic.add(subTasks.get(subTaskId)));

        return subTasksByEpic;
    }

    @Override
    public final void printAllTasks() {
        Stream.concat(
                Stream.concat(tasks.values().stream(), epics.values().stream()),
                subTasks.values().stream()
        ).forEach(System.out::println);
    }

    @Override
    public final List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public final List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
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

    private boolean isIntersectByTime(Task firstTask, Task secondTask) {
        LocalDateTime firstStartTime = firstTask.getStartTime();
        LocalDateTime firstEndTime = firstTask.getEndTime();
        LocalDateTime secondStartTime = secondTask.getStartTime();
        LocalDateTime secondEndTime = secondTask.getEndTime();

        if (firstStartTime == null || firstEndTime == null || secondStartTime == null || secondEndTime == null) {
            return false;
        }

        return !firstEndTime.isBefore(secondStartTime) && !secondEndTime.isBefore(firstStartTime);
    }
}