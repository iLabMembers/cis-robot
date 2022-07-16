package jp.xkzm.robot.conversation;

public interface ConversationContent {

    void                startConversation();
    Object              getKey();
    ConversationContent setKey(Object key);
    boolean             checkReady();
    ConversationContent setStart();

}
