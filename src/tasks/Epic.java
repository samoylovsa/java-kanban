package tasks;

import java.util.ArrayList;

public class Epic extends Task {

    private ArrayList<Integer> subTaskIdList;

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
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
    public String toString() {
        return "Epic{" +
                "id=" + this.getId() +
                ", name='" + this.getName() + '\'' +
                ", description='" + this.getDescription() + '\'' +
                ", status=" + this.getStatus() +
                ", subTaskIdList=" + subTaskIdList +
                '}';
    }
}
