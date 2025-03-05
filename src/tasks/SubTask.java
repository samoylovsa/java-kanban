package tasks;

import java.time.Duration;
import java.time.LocalDateTime;

public class SubTask extends Task {

    private int epicId;

    public SubTask(String name, String description, Status status, int epicId, LocalDateTime startTime, Duration duration) {
        super(name, description, status, startTime, duration);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public Type getType() {
        return Type.SUBTASK;
    }

    @Override
    public String toString() {
        return "SubTask{" +
                "id=" + this.getId() +
                ", name='" + this.getName() + '\'' +
                ", description='" + this.getDescription() + '\'' +
                ", status=" + this.getStatus() +
                ", startTime=" + this.getStartTime() +
                ", endTime=" + this.getEndTime() +
                ", duration=" + this.getDuration() +
                '}';
    }
}