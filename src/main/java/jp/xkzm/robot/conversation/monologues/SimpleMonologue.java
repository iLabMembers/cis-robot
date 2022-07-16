package jp.xkzm.robot.conversation.monologues;

import jp.ac.doshisha.cis.CISRobot;

public class SimpleMonologue extends Monologue {

    private final String line;

    public SimpleMonologue(Object key, String line) {

        this.line = line;

        setKey(key);


    }

    @Override
    public void startConversation() {

        CISRobot.say(line);

    }

}
