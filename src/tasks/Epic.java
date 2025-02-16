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
        return "%d,%s,%s,%s,%s".formatted(this.id, Type.EPIC, this.name, this.status, this.description);
    }

    @Override
    public Epic fromString(String string) {
        String[] fields = string.split(",");

        int id = Integer.getInteger(fields[0]);
        String name = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];

        Epic epic = new Epic(name, description);
        epic.setId(id);
        epic.setStatus(status);

        return epic;
    }
}