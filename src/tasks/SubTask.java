package tasks;

public class SubTask extends Task {

    private int epicId;

    public SubTask(String name, String description, Status status, int epicId) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "%d,%s,%s,%s,%s,%d".formatted(this.id, Type.SUBTASK, this.name, this.status, this.description, this.epicId);
    }

    @Override
    public SubTask fromString(String string) {
        String[] fields = string.split(",");

        int id = Integer.getInteger(fields[0]);
        String name = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];
        int epicId = Integer.getInteger(fields[5]);

        SubTask subTask = new SubTask(name, description, status, epicId);
        subTask.setId(id);

        return subTask;
    }
}