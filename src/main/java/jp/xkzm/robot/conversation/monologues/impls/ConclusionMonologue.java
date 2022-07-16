package jp.xkzm.robot.conversation.monologues.impls;

import jp.ac.doshisha.cis.CISRobot;
import jp.xkzm.robot.conversation.monologues.Monologue;

public class ConclusionMonologue extends Monologue {

    /**
     * 締めの発話内容，ここでおすすめする観光地を喋らせるなど
     */
    @Override
    public void startConversation() {

        CISRobot.say("Thank you for your time.");
        CISRobot.say("This is the end of our conversation ... .");

    }

}
