package com.wupol.myopia.business.api.device.util;

import com.wupol.myopia.business.core.device.domain.dto.DeviceScreenDataDTO;
import org.apache.http.util.TextUtils;

/**
 * @Classname TypeUtil
 * @Description 检查结果类型的判断
 * @Date 2021/7/20 5:38 下午
 * @Author Jacob todo 需要调整代码
 * @Version
 */
public class CheckTypeUtil {

    /**
     * 获取检查结果
     * @param patient
     * @return
     */
    public static String getCheckResult(DeviceScreenDataDTO patient){
        String msg = "";
        double LeftSph = patient.getLeftSph();
        double RightSph = patient.getRightSph();
        double LeftCyl = patient.getLeftCyl();
        double RightCyl = patient.getRightCyl();
        double LeftPR = patient.getLeftPR();
        double RightPR = patient.getRightPR();
        double LeftRR = patient.getRedReflectLeft();
        double RightRR = patient.getRedReflectRight();
        double LeftAxsiH = patient.getLeftAxsiH();
        double LeftAxsiV = patient.getLeftAxsiV();
        double RightAxsiV = patient.getRightAxsiV();
        double RightAxsiH = patient.getRightAxsiH();
        int model = patient.getCheckMode();
        int age = patient.getPatientAge();
        String sep = "、";

        if (model == 0) {
            //right eye
            String rightSph = checkSph(age, RightSph);
            String rightCyl = checkCyl(age, RightCyl);
            String rightStrabism = checkStrabism(RightAxsiH,RightAxsiV);

            String separator1 = (!"".equals(rightSph) && !"".equals(rightCyl)) ? sep : "";
            String separator2 = (!"".equals(rightSph) || !"".equals(rightCyl)) && !TextUtils.isEmpty(rightStrabism) ? sep : "";
            String rightResult = rightSph+separator1+rightCyl + separator2 + rightStrabism;
            msg +="".equals(rightResult.trim()) ? "" : MODEL_NAME_RIGHT_EYE +"(" + rightResult + ")";
            //MODEL_NAME_RIGHT_EYE
            //left eye
            String leftsph = checkSph(age, LeftSph);
            String leftCyl = checkCyl(age, LeftCyl);
            String leftStrabism = checkStrabism(LeftAxsiH,LeftAxsiV);
            separator1 = (!"".equals(leftsph) && !"".equals(leftCyl)) ? sep : "";
            separator2 = (!"".equals(leftsph) || !"".equals(leftCyl)) && !TextUtils.isEmpty(leftStrabism) ? sep : "";
            String leftResult = "".equals(leftsph + separator1 + leftCyl + separator2 + leftStrabism) ? "" : MODEL_NAME_LEFT_EYE + "(" + leftsph + separator1+ leftCyl + separator2 + leftStrabism +  ")";
            msg += "".equals(msg.trim()) ? leftResult : "".equals(leftResult) ? "" + leftResult : sep + leftResult;
            //PR
            double eyePR =  LeftPR - RightPR;
            String prString = Math.abs(eyePR) > 1 ? MSG_RESULT_TEXT_UNEQUAL_PUPIL : "";
            msg += (!"".equals(msg.trim()) && !"".equals(prString)) ? sep + prString : prString;
            //anisometropia
            double shpdest = Math.abs(LeftSph - RightSph);
            double cyldest = Math.abs(LeftCyl - RightCyl);
            String ropiaString = age <= 12 ? shpdest > 1.5 ? MSG_RESULT_TEXT_ANISOMETROPIA : "" : shpdest > 1 ? MSG_RESULT_TEXT_ANISOMETROPIA : "";
            String cylropia = age <= 12 ? cyldest > 1.5 ? MSG_RESULT_TEXT_ANISOMETROPIA : "" : cyldest > 1 ? MSG_RESULT_TEXT_ANISOMETROPIA : "";
            ropiaString = "".equals(ropiaString) ? cylropia : ropiaString;
            msg +=  (!"".equals(msg.trim()) && !"".equals(ropiaString)) ? sep + ropiaString : ropiaString;
            //gaze ranging
            String gazeRanging = (Math.abs(LeftAxsiH - RightAxsiH) > 8
                    || Math.abs(LeftAxsiV - RightAxsiV) > 8) ? MSG_RESULT_TEXT_GAZE_RANGING : "";
            msg +=  (!"".equals(msg.trim()) && !"".equals(gazeRanging)) ? sep + gazeRanging : gazeRanging;

        } else if (model == 1) {
            //left eye
            String leftsph = checkSph(age, LeftSph);
            String leftCyl = checkCyl(age, LeftCyl);
            String leftStrabism = checkStrabism(LeftAxsiH, LeftAxsiV);
            String separator1 = (!"".equals(leftsph) && !"".equals(leftCyl)) ? sep : "";
            String separator2 = (!"".equals(leftsph) || !"".equals(leftCyl)) && !TextUtils.isEmpty(leftStrabism) ? sep : "";
            msg += "".equals(leftsph + separator1 + leftCyl + separator2 + leftStrabism) ? "" : MODEL_NAME_LEFT_EYE + "(" + leftsph + separator1 + leftCyl + separator2 + leftStrabism + ")";
        } else {
            //right eye
            String leftsph = checkSph(age, LeftSph);
            String leftCyl = checkCyl(age, LeftCyl);
            String leftStrabism = checkStrabism(LeftAxsiH, LeftAxsiV);
            String separator1 = (!"".equals(leftsph) && !"".equals(leftCyl)) ? sep : "";
            String separator2 = (!"".equals(leftsph) || !"".equals(leftCyl)) && !TextUtils.isEmpty(leftStrabism) ? sep : "";
            msg += "".equals(leftsph + separator1 + leftCyl + separator2 + leftStrabism) ? "" : MODEL_NAME_RIGHT_EYE + "(" + leftsph + separator1 + leftCyl + separator2 + leftStrabism + ")";
        }
        return msg;
    }

    public static String checkSph(int age, double value) {
        String msg = "";
        if (age >= 6 && age < 12) {
            msg = value > 3.5 ? MSG_RESULT_TEXT_FARSIGHTEDNESS :
                    value < -1 ? MSG_RESULT_TEXT_MYOPIA : "";
        } else if (age >= 12 && age < 36) {
            msg = value > 3 ? MSG_RESULT_TEXT_FARSIGHTEDNESS :
                    value < -1 ? MSG_RESULT_TEXT_MYOPIA : "";
        } else if (age >= 36 && age < 240) {
            msg = value > 2.5 ? MSG_RESULT_TEXT_FARSIGHTEDNESS :
                    value < -1 ? MSG_RESULT_TEXT_MYOPIA : "";
        } else if (age >= 240) {
            msg = value > 2 ? MSG_RESULT_TEXT_FARSIGHTEDNESS :
                    value < -1.5 ? MSG_RESULT_TEXT_MYOPIA : "";
        }
        return msg;
    }

    public static String checkCyl(int age, double value) {
        String msg = "";
        if (age >= 6 && age < 12) {
            msg = value < -2.25 ? MSG_RESULT_TEXT_ASTIGMATISM : "";
        } else if (age >= 12 && age < 36) {
            msg = value < -2 ? MSG_RESULT_TEXT_ASTIGMATISM : "";
        } else if (age >= 36 && age < 72) {
            msg = value < -2 ? MSG_RESULT_TEXT_ASTIGMATISM : "";
        } else if (age >= 72 && age < 240) {
            msg = value < -1.5 ? MSG_RESULT_TEXT_ASTIGMATISM : "";
        } else if (age >= 240) {
            msg = value < -1.5 ? MSG_RESULT_TEXT_ASTIGMATISM : "";
        }

        return msg;
    }

    /**
     * 检查是否散光
      * @param greedH
     * @param greedV
     * @return
     */                            
    public static String checkStrabism(double greedH,double greedV){
        String msg = "";
        if (Math.abs(greedH) > 8 || Math.abs(greedV)>8){
            msg = MSG_RESULT_TEXT_STRABISM;
        }
        return msg;
    }



    private static final String TV_RESULT_TEXT_GOOD = "屈光正常";      
    private static final String MSG_RESULT_TEXT_MYOPIA = "近视";
    private static final String MSG_RESULT_TEXT_FARSIGHTEDNESS = "远视";
    private static final String MSG_RESULT_TEXT_ASTIGMATISM = "散光";
    private static final String MSG_RESULT_TEXT_GAZE_RANGING = "凝视不等";
    private static final String MSG_RESULT_TEXT_ANISOMETROPIA = "屈光参差";
    private static final String MSG_RESULT_TEXT_UNEQUAL_PUPIL = "瞳孔大小不等";
    private static final String MSG_RESULT_TEXT_STRABISM = "斜视";          
    private static final String MODEL_NAME_RIGHT_EYE = "右眼";
    private static final String MODEL_NAME_LEFT_EYE = "左眼";
}
