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
        if (newTask.getStartTime() == null || newTask.getEndTime() == null) {
            System.out.println("У создаваемой задачи нет startTime или endTime");
            return -1;
        }

        boolean isIntersectByTime = prioritizedTasks.stream()
                .anyMatch(existingTask -> isIntersectByTime(existingTask, newTask));
        if (isIntersectByTime) {
            System.out.println("Создаваемая задача пересекается по времени с уже существующими задачами");
            return -1;
        }

        newTask.setId(generateId());
        tasks.put(newTask.getId(), newTask);
        prioritizedTasks.add(newTask);

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
        if (newSubTask.getStartTime() == null || newSubTask.getEndTime() == null) {
            System.out.println("У создаваемой подзадачи нет startTime или endTime");
            return -1;
        }

        boolean isIntersectByTime = prioritizedTasks.stream()
                .anyMatch(existingSubTask -> isIntersectByTime(existingSubTask, newSubTask));
        if (isIntersectByTime) {
            System.out.println("Создаваемая подзадача пересекается по времени с уже существующими подзадачами");
            return -1;
        }

        int epicId = newSubTask.getEpicId();
        if (epics.containsKey(epicId)) {
            newSubTask.setId(generateId());
            int subTaskId = newSubTask.getId();
            epics.get(epicId).addSubTaskId(subTaskId);
            subTasks.put(subTaskId, newSubTask);
            prioritizedTasks.add(newSubTask);
            updateEpicStatus(epics.get(epicId));
            updateEpicTime(epics.get(epicId));

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

        if (existingTask == null) {
            System.out.println("Не найдена задача с taskId: " + updatedTaskId);
            return false;
        }

        if (updatedTask.getStartTime() == null || updatedTask.getEndTime() == null) {
            System.out.println("У обновляемой задачи нет startTime или endTime");
            return false;
        }

        boolean isIntersectByTime = prioritizedTasks.stream()
                .filter(task -> !task.equals(existingTask))
                .anyMatch(task -> isIntersectByTime(task, updatedTask));

        if (isIntersectByTime) {
            System.out.println("Обновляемая задача пересекается по времени с уже существующими задачами");
            return false;
        }

        tasks.put(updatedTaskId, updatedTask);
        prioritizedTasks.remove(existingTask);
        prioritizedTasks.add(updatedTask);

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

        if (updatedSubTask.getStartTime() == null || updatedSubTask.getEndTime() == null) {
            System.out.println("У обновляемой подзадачи нет startTime или endTime");
            return false;
        }

        boolean isIntersectByTime = prioritizedTasks.stream()
                .filter(task -> !task.equals(existingSubTask))
                .anyMatch(task -> isIntersectByTime(task, updatedSubTask));

        if (isIntersectByTime) {
            System.out.println("Обновляемая подзадача пересекается по времени с уже существующими подзадачами");
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
        prioritizedTasks.remove(existingSubTask);
        prioritizedTasks.add(updatedSubTask);
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
            System.out.println("Не существует эпика с указанным epicId: " + epicId);
            return;
        }
        ArrayList<Integer> subTaskIdList = epics.get(epicId).getSubTaskIdList();
        subTaskIdList.forEach(subTaskId -> {
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
        subTasks.clear();
    }

    @Override
    public void deleteAllSubTasks() {
        Set<Epic> processedEpics = new HashSet<>();

        subTasks.values().forEach(subTask -> {
            int epicId = subTask.getEpicId();
            Epic epic = epics.get(epicId);
            if (epic != null) {
                processedEpics.add(epic);
            }
        });

        subTasks.keySet().forEach(id -> {
            historyManager.remove(id);
            prioritizedTasks.remove(subTasks.get(id));
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
            System.out.println("Не существует эпика с epicId: " + epicId);
            return null;
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

        return firstStartTime.isBefore(secondEndTime) && secondStartTime.isBefore(firstEndTime);
    }
}