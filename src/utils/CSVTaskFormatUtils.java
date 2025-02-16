package utils;

import exceptions.TaskParseException;
import tasks.*;

public final class CSVTaskFormatUtils {

    private static String TITLE = "id,type,name,status,description,epic";

    private CSVTaskFormatUtils() {
    }

    public static String getCSVTitle() {
        return TITLE;
    }

    public static String toString(Task task) {
        if (task instanceof SubTask subTask) {
            return "%d,%s,%s,%s,%s,%d".formatted(
                    subTask.getId(),
                    subTask.getType(),
                    subTask.getName(),
                    subTask.getStatus(),
                    subTask.getDescription(),
                    subTask.getEpicId()
            );
        }
        return "%d,%s,%s,%s,%s".formatted(
                task.getId(),
                task.getType(),
                task.getName(),
                task.getStatus(),
                task.getDescription()
        );
    }

    public static Task fromString(String string) {
        String[] fields = string.split(",");
        int fieldsNumber = fields.length;
        if (fieldsNumber < 5) {
            throw new TaskParseException("Недостаточно данных в переданной строке: " + fieldsNumber +
                    " значений в строке");
        }

        int id = Integer.parseInt(fields[0]);
        Type taskType = Type.valueOf(fields[1]);
        String name = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];

        switch (taskType) {
            case TASK:
                Task task = new Task(name, description, status);
                task.setId(id);
                return task;
            case EPIC:
                Epic epic = new Epic(name, description);
                epic.setStatus(status);
                epic.setId(id);
                return epic;
            case SUBTASK:
                if (fieldsNumber < 6) {
                    throw new TaskParseException("Недостаточно данных в переданной строке: " + fieldsNumber +
                            " значений в строке");
                }
                int epicId = Integer.parseInt(fields[5]);
                SubTask subTask = new SubTask(name, description, status, epicId);
                subTask.setId(id);
                return subTask;
            default:
                throw new TaskParseException("Неизвестный тип задачи: " + taskType);
        }
    }
}
