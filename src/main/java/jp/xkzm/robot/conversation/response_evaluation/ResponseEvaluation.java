package jp.xkzm.robot.conversation.response_evaluation;

public enum ResponseEvaluation {

    SIMILARITY, // ユーザ発話と key を類似度評価
    CONTAINS,   // ユーザ発話内に key が存在するか評価
    RANDOMLY,   // key 関係なくランダムに選択評価

}
