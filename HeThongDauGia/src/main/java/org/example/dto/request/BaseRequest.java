package org.example.dto.request;

public abstract class BaseRequest {
    private String command;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}