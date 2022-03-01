package com.example.chitchat.utilities;

import java.util.HashMap;

public class Constants {
    public static final String KEY_NAME = "name";
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "fcm_token";
    public static final String KEY_PREFERENCE_NAME = "chitChatPreference";
    public static final String KEY_USER = "user";
    public static final String KEY_COLLECTION_CHATS = "chats";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_COLLECTION_CONVERSATIONS = "conversations";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_NAME = "receiverName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_LAST_MESSAGE = "lastMessage";
    public static final String KEY_AVAILABILITY = "availability";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

    public static HashMap<String,String> remoteMsgHeaders = null;
    public static HashMap<String,String> getRemoteMsgHeaders()
    {
        if(remoteMsgHeaders == null)
        {
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(
                    Constants.REMOTE_MSG_AUTHORIZATION,
                    "key=AAAAW1ibcv8:APA91bEVPjuHIgaH_5bSeZbDEdcqwgVaxS9WgIzquqFCYp6CNBC0GVl4nucvB6KLg0ELhSwIe4BEXODc4JihbN8saoeUAwhJG0B44slODfNuunGpFBPgSllqbxLEzQBWNlfq6yjcuepa"
                    );
            remoteMsgHeaders.put(
                    Constants.REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteMsgHeaders;
    }

}
