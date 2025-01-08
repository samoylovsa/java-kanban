import java.util.ArrayList;
import java.util.LinkedList;

public class InMemoryHistoryManager implements HistoryManager {

    private static final int HISTORY_SIZE = 10;
    private static final LinkedList<Task> taskHistory = new LinkedList<>();

    @Override
    public final void add(Task task) {
        if (taskHistory.size() >= HISTORY_SIZE) {
            taskHistory.removeFirst();
        }
        taskHistory.addLast(task);
    }

    @Override
    public final ArrayList<Task> getHistory() {
        return new ArrayList<>(taskHistory);
    }
}