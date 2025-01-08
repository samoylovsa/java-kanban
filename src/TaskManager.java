import java.util.ArrayList;

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

    ArrayList<Task> getTasks();

    ArrayList<Epic> getEpics();

    ArrayList<SubTask> getSubTasks();

    void deleteTask(int id);

    void deleteEpic(int id);

    void deleteSubTask(int id);

    void deleteAllEntities();

    void deleteAllTasks();

    void deleteAllEpics();

    void deleteAllSubTasks();

    ArrayList<SubTask> getSubTasksByEpic(int id);

    void printAllTasks();
}
