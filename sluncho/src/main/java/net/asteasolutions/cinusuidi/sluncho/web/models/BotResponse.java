package net.asteasolutions.cinusuidi.sluncho.web.models;

public class BotResponse {

    private final long id;
    private final String content;

    public BotResponse(long id, String content) {
        this.id = id;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
}