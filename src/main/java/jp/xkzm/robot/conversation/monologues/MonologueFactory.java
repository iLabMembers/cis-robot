package jp.xkzm.robot.conversation.monologues;

public class MonologueFactory {

    private MonologueFactory() {}

    /**
     * 簡易に発話内容インスタンスを生成するファクトリメソッド
     * 次の会話を登録せず，終端の発話の時に利用する
     * @param key  評価時に利用される．評価が CONTAINS なら文字列，SIMILARITY なら SimilarityMeasure を継承したオブジェクト（未実装）
     * @param line 発話内容
     * @return 発話内容インスタンス
     */
    public static Monologue createSimpleMonologue(Object key, String line) {

        return new SimpleMonologue(key, line);

    }
}
