package ru.splat.tm.messages;

/**
 * Created by Дмитрий on 26.02.2017.
 */
public class CommitTopicMsg {
    private final String topic;

    public String getTopic() {
        return topic;
    }

    public CommitTopicMsg(String topic) {
        this.topic = topic;

    }
}
