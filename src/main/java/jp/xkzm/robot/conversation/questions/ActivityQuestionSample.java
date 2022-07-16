package jp.xkzm.robot.conversation.questions;

import jp.ac.doshisha.cis.CISRobot;
import jp.xkzm.robot.conversation.ConversationContent;
import jp.xkzm.robot.conversation.monologues.MonologueFactory;
import jp.xkzm.robot.conversation.response_evaluation.ResponseEvaluation;

import java.util.List;


public class ActivityQuestionSample extends Question {

    /**
     *  クラス名と同じ名前のメソッドはコンストラクタ：そのクラスのインスタンスを作成
     *  基本的に super(** 中身は真似る **) を呼び出せばよい
     */
    public ActivityQuestionSample() {

        super();

    }

    public ActivityQuestionSample(Object key) {

        super(key);

    }

    public ActivityQuestionSample(String key, List<ConversationContent> nextConversations, ResponseEvaluation resEval) {

        super(key, nextConversations, resEval);

    }

    /**
     * この質問の次に行う会話を登録
     * @param nextConversations 会話の候補，ConversationContentを継承している必要がある
     * @param resEval           ユーザ返答に対する評価方法
     */
    @Override
    public void setNextConversations(List<ConversationContent> nextConversations, ResponseEvaluation resEval) {

        super.setNextConversations(nextConversations, resEval);

    }

    /**
     * 会話を開始
     * 会話の内容を定義するのはこのメソッド内で記述する．
     */
    @Override
    public void startConversation() {

        CISRobot.say("Do you have an active personality?");

        String speech = CISRobot.listen();

        decideNextConversation(speech);

    }

    /**
     * ユーザの入力と次の会話の key を比較評価し次の会話を選択する
     * @param key Object 型だが文字列か類似度計算可能なオブジェクト（未実装）を登録する．評価方法がResoponseEvaluation.CONTAINS なら文字列を引数とする．
     * @return 比較評価の結果採用された次の会話
     */
    @Override
    protected ConversationContent evaluate(Object key) {

        for (ConversationContent next : getNextConversations()) {

            String speech = (String) key;
            if (speech.toLowerCase().contains(((String) next.getKey()).toLowerCase())) {

                return next;

            }

        }

        return MonologueFactory.createSimpleMonologue("", "Could you say that again ... ?");

    }

}
