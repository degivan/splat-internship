package ru.splat.tm.messages;

/**
 * Created by Дмитрий on 22.02.2017.
 */
public class RetryCommitMsg extends CommitTopicMsg
{


    public RetryCommitMsg(String topic) {
        super(topic);
    }
}
