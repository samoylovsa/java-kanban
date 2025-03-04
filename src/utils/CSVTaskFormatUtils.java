package utils;

import exceptions.TaskParseException;
import tasks.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class CSVTaskFormatUtils {

    private static final String TITLE = "id,type,name,status,description,startTime,endTime,duration,epic";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private CSVTaskFormatUtils() {
    }

    public static String getCSVTitle() {
        return TITLE;
    }

    public static String toString(Task task) {
        String formattedStartTime = task.getStartTime() == null ? "null" : task.getStartTime().format(DATE_TIME_FORMATTER);
        String formattedEndTime = task.getEndTime() == null ? "null" : task.getEndTime().format(DATE_TIME_FORMATTER);
        String formattedDuration = task.getDuration() == null ? "null" : task.getDuration().toString();

        if (task instanceof SubTask subTask) {
            return "%d,%s,%s,%s,%s,%s,%s,%s,%d".formatted(
                    subTask.getId(),
                    subTask.getType(),
                    subTask.getName(),
                    subTask.getStatus(),
                    subTask.getDescription(),
                    formattedStartTime,
                    formattedEndTime,
                    formattedDuration,
                    subTask.getEpicId()
            );
        }
        return "%d,%s,%s,%s,%s,%s,%s,%s".formatted(
                task.getId(),
                task.getType(),
                task.getName(),
                task.getStatus(),
                task.getDescription(),
                formattedStartTime,
                formattedEndTime,
                formattedDuration
        );
    }

    public static Task fromString(String string) {
        String[] fields = string.split(",");
        int fieldsNumber = fields.length;
        if (fieldsNumber < 8) {
            throw new TaskParseException("Недостаточно данных в переданной строке: " + fieldsNumber +
                    " значений в строке");
        }

        int id = Integer.parseInt(fields[0]);
        Type taskType = Type.valueOf(fields[1]);
        String name = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];
        LocalDateTime startTime = fields[5].equals("null") ? null : LocalDateTime.parse(fields[5], DATE_TIME_FORMATTER);
        LocalDateTime endTime = fields[6].equals("null") ? null : LocalDateTime.parse(fields[6], DATE_TIME_FORMATTER);
        Duration duration = fields[7].equals("null") ? null : Duration.parse(fields[7]);

        switch (taskType) {
            case TASK:
                Task task = new Task(name, description, status, startTime, duration);
                task.setId(id);
                return task;
            case EPIC:
                Epic epic = new Epic(name, description);
                epic.setStatus(status);
                epic.setId(id);
                epic.setStartTime(startTime);
                epic.setEndTime(endTime);
                epic.setDuration(duration);
                return epic;
            case SUBTASK:
                if (fieldsNumber < 9) {
                    throw new TaskParseException("Недостаточно данных в переданной строке: " + fieldsNumber +
                            " значений в строке");
                }
                int epicId = Integer.parseInt(fields[8]);
                SubTask subTask = new SubTask(name, description, status, epicId, startTime, duration);
                subTask.setId(id);
                return subTask;
            default:
                throw new TaskParseException("Неизвестный тип задачи: " + taskType);
        }
    }
}
