package jp.xkzm.robot.conversation.monologues.impls;

import jp.ac.doshisha.cis.CISRobot;
import jp.xkzm.robot.conversation.monologues.Monologue;

public class IntroductionMonologue extends Monologue {

    /**
     * 冒頭の挨拶や自己紹介など始まりの発話
     */
    @Override
    public void startConversation() {

        CISRobot.say("Hello! Nice to meet you!! Let's start a conversation.");

    }
}
