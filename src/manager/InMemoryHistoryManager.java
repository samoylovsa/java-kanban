package manager;

import tasks.Task;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private static final int HISTORY_SIZE = 10;
    private final LinkedList<Task> taskHistory = new LinkedList<>();

    @Override
    public final void add(Task task) {
        if (taskHistory.size() >= HISTORY_SIZE) {
            taskHistory.removeFirst();
        }
        taskHistory.addLast(task);
    }

    @Override
    public final List<Task> getHistory() {
        return new ArrayList<>(taskHistory);
    }

    @Override
    public final void remove(int id) {
        for (int i = 0; i < taskHistory.size(); i++) {
            Task task = taskHistory.get(i);
            if (task.getId() == id) {
                taskHistory.remove(i);
                return;
            }
        }
    }
}