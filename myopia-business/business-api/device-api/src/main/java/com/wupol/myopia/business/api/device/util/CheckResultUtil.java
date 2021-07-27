package com.wupol.myopia.business.api.device.util;

import com.wupol.framework.core.util.StringUtils;
import com.wupol.myopia.business.core.device.domain.dto.DeviceScreenDataDTO;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @Classname TypeUtil
 * @Description 检查结果类型的判断
 * @Date 2021/7/20 5:38 下午
 * @Author Jacob
 * @Version
 */
public class CheckResultUtil {

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

    /**
     * 中文分割符
     */
    private static String sep = "、";
    /**
     * 获取检查结果
     * @param patient
     * @return
     */
    public static String getCheckResult(DeviceScreenDataDTO patient){
        double leftSph = patient.getLeftSph();
        double rightSph = patient.getRightSph();
        double leftCyl = patient.getLeftCyl();
        double rightCyl = patient.getRightCyl();
        double leftPR = patient.getLeftPR();
        double rightPR = patient.getRightPR();
        double leftAxsiH = patient.getLeftAxsiH();
        double leftAxsiV = patient.getLeftAxsiV();
        double rightAxsiV = patient.getRightAxsiV();
        double rightAxsiH = patient.getRightAxsiH();
        int model = patient.getCheckMode();
        int age = patient.getPatientAge();
        String sep = "、";

        if (model == 0) {
            //right eye
            String rightEyeResult = getResult(rightSph, rightCyl, rightAxsiH, rightAxsiV, age, MODEL_NAME_RIGHT_EYE);
            //left eye
            String leftEyeResult = getResult(leftSph, leftCyl, leftAxsiH, leftAxsiV, age, MODEL_NAME_LEFT_EYE);
            //PR
            String prString = checkPR(leftPR,rightPR);
            //anisometropia
            String ropiaString = checkAnisometropia(age,Math.abs(leftSph - rightSph),Math.abs(leftCyl - rightCyl));
            //gaze ranging
            String gazeRanging = checkUnequalPupil(leftAxsiH,rightAxsiH,leftAxsiV,rightAxsiV);
            return Arrays.asList(rightEyeResult,leftEyeResult,prString,ropiaString,gazeRanging).stream().filter(StringUtils::isNotBlank).collect(Collectors.joining(sep));
        }
        //left eye
        if (model == 1) {
            return getResult(leftSph, leftCyl, leftAxsiH, leftAxsiV, age, MODEL_NAME_LEFT_EYE);
        }
        //right eye
        if (model == 2){
            return getResult(rightSph, rightCyl, rightAxsiH, rightAxsiV, age, MODEL_NAME_RIGHT_EYE);
        }
        return "";
    }

    /**
     * 获取结果
     * @param sph
     * @param cyl
     * @param axsiH
     * @param axsiV
     * @param age
     * @return
     */
    private static String getResult(double sph, double cyl, double axsiH, double axsiV, int age, String leftOrRight) {
        String leftsph = checkSph(age, sph);
        String leftCyl = checkCyl(age, cyl);
        String leftStrabism = checkStrabism(axsiH, axsiV);
        String params = Arrays.asList(leftsph, leftCyl, leftStrabism).stream().filter(StringUtils::isNotBlank).collect(Collectors.joining(sep));
        String result = String.join(sep, params);
        if (StringUtils.isNotBlank(result)) {
            return  leftOrRight + "(" + result + ")";
        } else {
            return StringUtils.EMPTY;
        }
    }

    /**
     * 检查视力是近视还是远视
     * @param age
     * @param value
     * @return
     */
    public static String checkSph(int age, double value) {

        if (age >= 6 && age < 12) {
          return  value > 3.5 ? MSG_RESULT_TEXT_FARSIGHTEDNESS :
                    value < -1 ? MSG_RESULT_TEXT_MYOPIA : "";
        }

        if (age >= 12 && age < 36) {
          return value > 3 ? MSG_RESULT_TEXT_FARSIGHTEDNESS :
                    value < -1 ? MSG_RESULT_TEXT_MYOPIA : "";
        } 
        
        if (age >= 36 && age < 240) {
            return  value > 2.5 ? MSG_RESULT_TEXT_FARSIGHTEDNESS :
                    value < -1 ? MSG_RESULT_TEXT_MYOPIA : "";
        }

        if (age >= 240) {
            return  value > 2 ? MSG_RESULT_TEXT_FARSIGHTEDNESS :
                    value < -1.5 ? MSG_RESULT_TEXT_MYOPIA : "";
        }
        return  StringUtils.EMPTY;
    }

    /**
     * 散光
     * @param age
     * @param value
     * @return
     */
    public static String checkCyl(int age, double value) {
        boolean flag = false;
        if (age >= 6 && age < 12 && value <= -2.5 ) {
            flag = true;
        } else if (age >= 12 && age < 72 && value < -2) {
            flag = true;
        } else if (age >= 72 && value < -1.5) {
            flag = true;
        }
        return flag ? MSG_RESULT_TEXT_ASTIGMATISM : StringUtils.EMPTY;
    }

    /**
     * 检查是否散光
     * 水平、垂直斜视度数的绝对值大于8，则显示有斜视。
      * @param greedH
     * @param greedV
     * @return
     */                            
    public static String checkStrabism(double greedH,double greedV){
        if (Math.abs(greedH) > 8 || Math.abs(greedV)>8){
            return MSG_RESULT_TEXT_STRABISM;
        }
        return StringUtils.EMPTY;
    }

    /**
     * 检查是否瞳孔大小不等
     * 标准: 左右瞳孔直径相差超过1
     * @param oneEyePR
     * @param anotherEyePR
     * @return
     */
    public static String checkPR(double oneEyePR,double anotherEyePR) {
        double eyePR =  oneEyePR - anotherEyePR;
        return Math.abs(eyePR) > 1 ? MSG_RESULT_TEXT_UNEQUAL_PUPIL : "";
    }


    /**
     * 检查是否 凝视不等
     * 标准: 左右眼水平或者垂直斜视度数差的绝对值大于8
     * @param oneEyeAxsiH
     * @param anotherEyeAxsiH
     * @param oneEyeAxsiV
     * @param anotherEyeAxsiV
     * @return
     */
    public static String checkUnequalPupil(double oneEyeAxsiH,double anotherEyeAxsiH,double oneEyeAxsiV,double anotherEyeAxsiV) {
       boolean isUnequalPupil = (Math.abs(oneEyeAxsiH - anotherEyeAxsiH) > 8 || Math.abs(oneEyeAxsiV - anotherEyeAxsiV) > 8);
       return isUnequalPupil ? MSG_RESULT_TEXT_GAZE_RANGING : "";
    }

    /**
     * 检查是否 屈光参差
     * 标准:
     * 1岁以下：左右眼的sph(球镜度) 或者cyl(柱镜度) 相差超过1.5D
     * 1岁以上：左右眼的sph(球镜度) 或者cyl(柱镜度) 相差超过1D
     * @param age
     * @param shpdest
     * @param cyldest
     * @return
     */
    public static String checkAnisometropia(int age,double shpdest,double cyldest) {
        String ropiaString = age <= 12 ? shpdest > 1.5 ? MSG_RESULT_TEXT_ANISOMETROPIA : "" : shpdest > 1 ? MSG_RESULT_TEXT_ANISOMETROPIA : "";
        String cylropia = age <= 12 ? cyldest > 1.5 ? MSG_RESULT_TEXT_ANISOMETROPIA : "" : cyldest > 1 ? MSG_RESULT_TEXT_ANISOMETROPIA : "";
        return "".equals(ropiaString) ? cylropia : ropiaString;
    }
}
