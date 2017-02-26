package ru.splat.tm.messages;

/**
 * Created by Дмитрий on 26.02.2017.
 */
public class CommitTrackerMsg {
    private final long newOffset;
    private final String topic;

    public long getNewOffset() {
        return newOffset;
    }
    public String getTopic() {
        return topic;
    }

    public CommitTrackerMsg(long newOffset, String topic) {
        this.newOffset = newOffset;
        this.topic = topic;

    }
}
