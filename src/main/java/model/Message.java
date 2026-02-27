package model;

import java.time.LocalDateTime;

public class Message {

    private int id;
    private int interviewId;
    private String senderType;
    private String content;
    private LocalDateTime sentAt;

    public Message(int id, int interviewId, String senderType, String content, LocalDateTime sentAt) {
        this.id = id;
        this.interviewId = interviewId;
        this.senderType = senderType;
        this.content = content;
        this.sentAt = sentAt;
    }

    public Message(int interviewId, String senderType, String content) {
        this.interviewId = interviewId;
        this.senderType = senderType;
        this.content = content;
    }

    public int getId() { return id; }
    public int getInterviewId() { return interviewId; }
    public String getSenderType() { return senderType; }
    public String getContent() { return content; }
    public LocalDateTime getSentAt() { return sentAt; }
}