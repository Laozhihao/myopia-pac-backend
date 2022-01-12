package com.wupol.myopia.business.core.screening.flow.util;

import com.wupol.myopia.business.common.utils.constant.WearingGlassesSituation;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningStudentDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;

import java.math.BigDecimal;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 钓猫的小鱼
 * @Date: 2022/01/12/11:05
 * @Description:
 */
public class EyeDataUtil {

    public static String leftReScreenSph(VisionScreeningResult visionScreeningResult){
        if (visionScreeningResult==null
                ||visionScreeningResult.getComputerOptometry()==null
                ||visionScreeningResult.getComputerOptometry().getLeftEyeData()==null){

            return "--";
        }
        if (visionScreeningResult.getComputerOptometry().getLeftEyeData().getSph()==null){

            return "球镜为null";
        }
        if (visionScreeningResult.getComputerOptometry().getLeftEyeData().getCyl()==null){

            return "柱镜为null";
        }

        BigDecimal cyl = visionScreeningResult.getComputerOptometry().getLeftEyeData().getCyl().divide(new BigDecimal(2),2,BigDecimal.ROUND_HALF_UP);

        BigDecimal resulr = visionScreeningResult.getComputerOptometry().getLeftEyeData().getSph().add(cyl);

        return resulr.toString();
    }

    /**
     * 等效球镜 = 球镜+柱镜/2
     * @param visionScreeningResult
     * @return
     */
    public static String rightReScreenSph(VisionScreeningResult visionScreeningResult){
        if (visionScreeningResult==null
                ||visionScreeningResult.getComputerOptometry()==null
                ||visionScreeningResult.getComputerOptometry().getRightEyeData()==null){

            return "--";
        }
        if (visionScreeningResult.getComputerOptometry().getRightEyeData().getSph()==null){

            return "球镜为null";
        }
        if (visionScreeningResult.getComputerOptometry().getRightEyeData().getCyl()==null){

            return "柱镜为null";
        }

        BigDecimal cyl = visionScreeningResult.getComputerOptometry().getRightEyeData().getCyl().divide(new BigDecimal(2),2,BigDecimal.ROUND_HALF_UP);

        BigDecimal resulr = visionScreeningResult.getComputerOptometry().getRightEyeData().getSph().add(cyl);

        return resulr.toString();
    }

    public static String className(ScreeningStudentDTO screeningStudentDTO){
        if (screeningStudentDTO!=null
                &&screeningStudentDTO.getClassName()!=null){

            return screeningStudentDTO.getClassName();
        }

        return "--";
    }

    public static String gradeName(ScreeningStudentDTO screeningStudentDTO){
        if (screeningStudentDTO!=null
                &&screeningStudentDTO.getGradeName()!=null){

            return screeningStudentDTO.getGradeName();
        }

        return "--";
    }

    public static String name(ScreeningStudentDTO screeningStudentDTO){
        if (screeningStudentDTO!=null
                &&screeningStudentDTO.getName()!=null){

            return screeningStudentDTO.getName();
        }

        return "--";
    }

    public static String sno(ScreeningStudentDTO screeningStudentDTO){
        if (screeningStudentDTO!=null
                &&screeningStudentDTO.getSno()!=null){

            return screeningStudentDTO.getSno();
        }

        return "--";
    }

    public static String gender(ScreeningStudentDTO screeningStudentDTO){
        if (screeningStudentDTO!=null
                &&screeningStudentDTO.getGender()!=null
                &&screeningStudentDTO.getGender()==0){

            return "男";
        }else if (screeningStudentDTO!=null
                &&screeningStudentDTO.getGender()!=null
                &&screeningStudentDTO.getGender()==1){

            return "女";
        }

        return "--";
    }

    public static String phone(ScreeningStudentDTO screeningStudentDTO){
        if (screeningStudentDTO!=null
                &&screeningStudentDTO.getParentPhone()!=null){

            return screeningStudentDTO.getParentPhone();
        }

        return "--";
    }

    public static String address(ScreeningStudentDTO screeningStudentDTO){
        if (screeningStudentDTO!=null
                &&screeningStudentDTO.getAddress()!=null){

            return screeningStudentDTO.getAddress();
        }

        return "--";
    }
    public static String glassesType(VisionScreeningResult visionScreeningResult){
        if (visionScreeningResult!=null
                &&visionScreeningResult.getVisionData()!=null
                &&visionScreeningResult.getVisionData().getRightEyeData()!=null
                &&visionScreeningResult.getVisionData().getRightEyeData().getGlassesType()!=null){

            return WearingGlassesSituation.getType(visionScreeningResult.getVisionData().getRightEyeData().getGlassesType());
        }

        return "--";
    }


    public static String computerLeftAxial(VisionScreeningResult visionScreeningResult){
        if (visionScreeningResult!=null
                &&visionScreeningResult.getComputerOptometry()!=null
                &&visionScreeningResult.getComputerOptometry().getLeftEyeData()!=null
                &&visionScreeningResult.getComputerOptometry().getLeftEyeData().getAxial()!=null){

            return visionScreeningResult.getComputerOptometry().getLeftEyeData().getAxial().toString();
        }

        return "--";
    }

    public static String computerRightAxial(VisionScreeningResult visionScreeningResult){
        if (visionScreeningResult!=null
                &&visionScreeningResult.getComputerOptometry()!=null
                &&visionScreeningResult.getComputerOptometry().getRightEyeData()!=null
                &&visionScreeningResult.getComputerOptometry().getRightEyeData().getAxial()!=null){

            return visionScreeningResult.getComputerOptometry().getRightEyeData().getAxial().toString();
        }

        return "--";
    }


    public static String computerLeftCyl(VisionScreeningResult visionScreeningResult){
        if (visionScreeningResult!=null
                &&visionScreeningResult.getComputerOptometry()!=null
                &&visionScreeningResult.getComputerOptometry().getLeftEyeData()!=null
                &&visionScreeningResult.getComputerOptometry().getLeftEyeData().getCyl()!=null){

            return visionScreeningResult.getComputerOptometry().getLeftEyeData().getCyl().toString();
        }

        return "--";
    }

    public static String computerRightCyl(VisionScreeningResult visionScreeningResult){
        if (visionScreeningResult!=null
                &&visionScreeningResult.getComputerOptometry()!=null
                &&visionScreeningResult.getComputerOptometry().getRightEyeData()!=null
                &&visionScreeningResult.getComputerOptometry().getRightEyeData().getCyl()!=null){

            return visionScreeningResult.getComputerOptometry().getRightEyeData().getCyl().toString();
        }

        return "--";
    }

    public static String computerLeftSph(VisionScreeningResult visionScreeningResult){
        if (visionScreeningResult!=null
                &&visionScreeningResult.getComputerOptometry()!=null
                &&visionScreeningResult.getComputerOptometry().getLeftEyeData()!=null
                &&visionScreeningResult.getComputerOptometry().getLeftEyeData().getSph()!=null){

            return visionScreeningResult.getComputerOptometry().getLeftEyeData().getSph().toString();
        }

        return "--";
    }


    public static String computerRightSph(VisionScreeningResult visionScreeningResult){
        if (visionScreeningResult!=null
                &&visionScreeningResult.getComputerOptometry()!=null
                &&visionScreeningResult.getComputerOptometry().getRightEyeData()!=null
                &&visionScreeningResult.getComputerOptometry().getRightEyeData().getSph()!=null){

            return visionScreeningResult.getComputerOptometry().getRightEyeData().getSph().toString();
        }

        return "--";
    }

    public static String correcteLeftDataToStr(VisionScreeningResult visionScreeningResult){
        if (visionScreeningResult!=null
                &&visionScreeningResult.getVisionData()!=null
                &&visionScreeningResult.getVisionData().getLeftEyeData()!=null
                &&visionScreeningResult.getVisionData().getLeftEyeData().getCorrectedVision()!=null){

            return visionScreeningResult.getVisionData().getLeftEyeData().getCorrectedVision().toString();
        }

        return "--";
    }

    public static String correcteRightDataToStr(VisionScreeningResult visionScreeningResult){
        if (visionScreeningResult!=null
                &&visionScreeningResult.getVisionData()!=null
                &&visionScreeningResult.getVisionData().getRightEyeData()!=null
                &&visionScreeningResult.getVisionData().getRightEyeData().getCorrectedVision()!=null){

            return visionScreeningResult.getVisionData().getRightEyeData().getCorrectedVision().toString();
        }

        return "--";
    }

    public static String visionRightDataToStr(VisionScreeningResult visionScreeningResult){
        if (visionScreeningResult!=null
                &&visionScreeningResult.getVisionData()!=null
                &&visionScreeningResult.getVisionData().getRightEyeData()!=null
                &&visionScreeningResult.getVisionData().getRightEyeData().getNakedVision()!=null){

            return visionScreeningResult.getVisionData().getRightEyeData().getNakedVision().toString();
        }

        return "--";
    }

    public static String visionLeftDataToStr(VisionScreeningResult visionScreeningResult){
        if (resultData(visionScreeningResult))
            return visionScreeningResult.getVisionData().getLeftEyeData().getNakedVision().toString();

        return "--";
    }

    public static boolean resultData(VisionScreeningResult visionScreeningResult) {
        if (visionScreeningResult!=null
                &&visionScreeningResult.getVisionData()!=null
                &&visionScreeningResult.getVisionData().getLeftEyeData()!=null
                &&visionScreeningResult.getVisionData().getLeftEyeData().getNakedVision()!=null){

            return true;
        }
        return false;
    }
}
