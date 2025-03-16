package manager;

import tasks.Epic;
import tasks.SubTask;
import tasks.Task;

import java.util.List;

public interface TaskManager {

    int createTask(Task task);

    int createEpic(Epic epic);

    int createSubTask(SubTask subTask);

    boolean updateTask(Task task);

    boolean updateEpic(Epic epic);

    boolean updateSubTask(SubTask newSubTask);

    Task getTask(int id);

    Epic getEpic(int id);

    SubTask getSubTask(int id);

    List<Task> getTasks();

    List<Epic> getEpics();

    List<SubTask> getSubTasks();

    void deleteTask(int id);

    void deleteEpic(int id);

    void deleteSubTask(int id);

    void deleteAllTasks();

    void deleteAllEpics();

    void deleteAllSubTasks();

    List<SubTask> getSubTasksByEpic(int id);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();
}
