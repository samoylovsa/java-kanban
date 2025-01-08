public class Manager {

    private Manager() {}

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }
}