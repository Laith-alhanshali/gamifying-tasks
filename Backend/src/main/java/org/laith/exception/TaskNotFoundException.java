package org.laith.exception;

public class TaskNotFoundException extends Exception {

    public TaskNotFoundException(int taskId) {
        super("Task with id " + taskId + " not found.");
    }

    public TaskNotFoundException(String message) {
        super(message);
    }
}
