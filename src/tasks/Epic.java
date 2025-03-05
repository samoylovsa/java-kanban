package tasks;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Epic extends Task {

    private ArrayList<Integer> subTaskIdList;
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description, Status.NEW, null, null);
        subTaskIdList = new ArrayList<>();
    }

    public ArrayList<Integer> getSubTaskIdList() {
        return new ArrayList<>(subTaskIdList);
    }

    public void deleteSubTaskId(Integer id) {
        subTaskIdList.remove(id);
    }

    public void deleteAllSubTaskId() {
        subTaskIdList.clear();
    }

    public void addSubTaskId(int id) {
        subTaskIdList.add(id);
    }

    @Override
    public Type getType() {
        return Type.EPIC;
    }

    @Override
    public LocalDateTime getEndTime() {
        if (this.getStartTime() == null || this.getDuration() == null) {
            return null;
        }
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void resetTime() {
        this.setStartTime(null);
        this.setDuration(null);
        this.endTime = null;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + this.getId() +
                ", name='" + this.getName() + '\'' +
                ", description='" + this.getDescription() + '\'' +
                ", status=" + this.getStatus() +
                ", startTime=" + this.getStartTime() +
                ", endTime=" + this.getEndTime() +
                ", duration=" + this.getDuration() +
                ", subTaskIdList=" + subTaskIdList +
                '}';
    }
}