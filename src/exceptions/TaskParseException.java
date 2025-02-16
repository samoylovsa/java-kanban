package exceptions;

public class TaskParseException extends RuntimeException {
  public TaskParseException(String message) {
    super(message);
  }
}