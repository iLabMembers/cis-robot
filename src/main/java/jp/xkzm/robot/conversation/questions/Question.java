package jp.xkzm.robot.conversation.questions;

import jp.ac.doshisha.cis.CISRobot;
import jp.xkzm.robot.conversation.ConversationContent;
import jp.xkzm.robot.conversation.ConversationFlow;
import jp.xkzm.robot.conversation.response_evaluation.ResponseEvaluation;
import jp.xkzm.robot.conversation.response_evaluation.SimilarityMeasurable;

import java.util.List;
import java.util.Random;

public abstract class Question implements ConversationContent {

    private Object                    key;
    private boolean                   isStart;
    private ResponseEvaluation        resEval;
    private List<ConversationContent> nextConversations;

    public Question() {

        if (! ConversationFlow.createdConversationFlow()) {

            System.err.println("ERROR: No ConversationFlow");
            System.exit(-1);

        }

        this.key         = "";
        this.isStart     = false;

    }

    public Question(Object key) {

        if (! ConversationFlow.createdConversationFlow()) {

            System.err.println("ERROR: No ConversationFlow");
            System.exit(-1);

        }

        this.key         = key;
        this.isStart     = false;

    }

    public Question(Object key, List<ConversationContent> nextConversations, ResponseEvaluation resEval) {

        if (! ConversationFlow.createdConversationFlow()) {

            System.err.println("ERROR: No ConversationFlow");
            System.exit(-1);

        }

        this.isStart     = false;

        setKey(key);
        setNextConversations(nextConversations, resEval);

    }

    public boolean checkReady() {

        return key != null && resEval != null && nextConversations != null;

    }

    @Override
    public Object getKey() {

        return key;

    }

    @Override
    public Question setKey(Object key) {

        this.key = key;

        return this;

    }

    @Override
    public ConversationContent setStart() {

        this.isStart = true;

        return this;
    }

    public void setNextConversations(List<ConversationContent> nextConversations, ResponseEvaluation resEval) {

        switch (resEval) {

            case CONTAINS:

                for (ConversationContent content : nextConversations) {

                    if (content.getKey() == null) {

                        System.err.printf("[%s] Key is null.%n", content.getClass().getName());
                        System.exit(-1);

                    } else if (! (content.getKey() instanceof String)) {

                        System.err.printf("[%s] Key should be String.%n", content.getClass().getName());
                        System.exit(-1);

                    }

                }
                break;

            case SIMILARITY:

                for (ConversationContent content : nextConversations) {

                    if (content.getKey() == null) {

                        System.err.printf("[%s] Key is null.%n", content.getClass().getName());
                        System.exit(-1);

                    } else if (! (content.getKey() instanceof SimilarityMeasurable)) {

                        System.err.printf("[%s] Key should be instance of SimirarityMeasurable.%n", content.getClass().getName());
                        System.exit(-1);

                    }

                }
                break;

            case RANDOMLY:
                break;

        }

        this.nextConversations = nextConversations;
        this.resEval           = resEval;

    }

    protected List<ConversationContent> getNextConversations() {

        return this.nextConversations;

    }

    protected void decideNextConversation(Object key) {

        switch (resEval) {

            case CONTAINS:
            case SIMILARITY:
                evaluate(key).startConversation();
                break;

            case RANDOMLY:
                randomly().startConversation();
                break;

            default:

        }

    }

    protected abstract ConversationContent evaluate(Object key);

    private ConversationContent randomly() {

        int index = new Random(nextConversations.size()).nextInt();

        return nextConversations.get(index);

    }

}
