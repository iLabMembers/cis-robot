package jp.ac.doshisha.cis.utils;

import jp.atr.commu_ai.RobotBodyController;
import jp.atr.commu_ai.RobotExpressionController;

public class RobotHWControlller {

    /**
     * This is an util class.
     */
    private RobotHWControlller(){}

    /**
     * Use this method to introduce our robot.
     * @param rbc require an instance of RobotBodyController
     * @param rec require an instance of RobotExpressionController
     */
    public static void caseBeforeSelfIntroduction(RobotBodyController rbc, RobotExpressionController rec) {

        laugh(rec);

    }

    /**
     * Use this method to explain sightseeing area
     * @param rbc require an instance of RobotBodyController
     * @param rec require an instance of RobotExpressionController
     */
    public static void caseBeforeSightseeingAreasDescription(RobotBodyController rbc, RobotExpressionController rec) {

        laugh(rec);
        faceToMonitor(rbc);

    }



    /**
     * let a robot laugh
     * @param rec require an instance of RobotExpressionController
     */
    public static void laugh(RobotExpressionController rec) {

        rec.setExpression("fullsmile");

    }

    /**
     * set mood based vacs
     * @param rec require an instance of RobotExpressionController
     */
    public static void moodbase(RobotExpressionController rec) {

        rec.setExpression("MoodBasedFACS");

    }

    /**
     * @param rbc require an instance of RobotBodyController
     */
    public static void faceFront(RobotBodyController rbc) {

        rbc.setGaze(0.0, 1.2, 1.5);

    }

    /**
     * @param rbc require an instance of RobotBodyController
     */
    public static void faceRight(RobotBodyController rbc) {

        rbc.setGaze(1.0, 1.2, 1.5);

    }

    /**
     * @param rbc require an instance of RobotBodyController
     */
    public static void faceToFace(RobotBodyController rbc) {

        rbc.setGazeToObject("humanhead");

    }

    /**
     * @param rbc require an instance of RobotBodyController
     */
    public static void faceToMonitor(RobotBodyController rbc) {

        rbc.setGazeToObject("monitor");

    }

}
