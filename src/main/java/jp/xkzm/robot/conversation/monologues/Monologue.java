package jp.xkzm.robot.conversation.monologues;

import jp.xkzm.robot.conversation.ConversationContent;
import jp.xkzm.robot.conversation.monologues.impls.ConclusionMonologue;
import jp.xkzm.robot.conversation.monologues.impls.IntroductionMonologue;
import jp.xkzm.robot.conversation.response_evaluation.ResponseEvaluation;

public abstract class Monologue implements ConversationContent {

    private Object             key;
    private boolean            isStart;
    private ResponseEvaluation resEval;

    public Monologue() {

        this.key = "";

        if (this instanceof IntroductionMonologue || this instanceof ConclusionMonologue) {

            this.setStart();

        }

    }

    @Override
    public Object getKey() {

        return key;

    }

    @Override
    public Monologue setKey(Object key) {

        this.key = key;

        return this;

    }

    @Override
    public ConversationContent setStart() {

        this.isStart = true;

        return this;
    }

    @Override
    public boolean checkReady() {

        return this. key != null;

    }

}
