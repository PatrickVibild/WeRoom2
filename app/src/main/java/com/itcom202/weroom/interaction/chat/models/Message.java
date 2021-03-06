package com.itcom202.weroom.interaction.chat.models;

/**
 * Model that represent a message.
 */
public class Message {

    private String content;
    private String userName;
    private String senderID;
    private String timeStamp;

    public Message( String content, String userName, String sender ) {
        this.userName = userName;
        this.content = content;
        this.senderID = sender;
        long tsLong = System.currentTimeMillis( ) / 1000;
        timeStamp = Long.toString( tsLong );
    }

    //public constructor to deserialize the object from the database.
    public Message( ) {
    }

    public String getContent( ) {
        return content;
    }

    public void setContent( String content ) {
        this.content = content;
    }

    public String getSenderID( ) {
        return senderID;
    }

    public void setSenderID( String senderID ) {
        this.senderID = senderID;
    }

    public String getTimeStamp( ) {
        return timeStamp;
    }

    public void setTimeStamp( String timeStamp ) {
        this.timeStamp = timeStamp;
    }

    public String getUserName( ) {
        return userName;
    }

    public void setUserName( String userName ) {
        this.userName = userName;
    }
}