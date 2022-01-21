package com.wupol.myopia.business.core.screening.flow.util;

import com.wupol.myopia.business.common.utils.constant.WearingGlassesSituation;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningStudentDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StudentVisionScreeningResultExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 钓猫的小鱼
 * @Date: 2022/01/12/11:05
 * @Description:
 */
@Slf4j
public class EyeDataUtil {

    public static StudentVisionScreeningResultExportDTO setStudentData(ScreeningStudentDTO studentDTO, VisionScreeningResult visionScreeningResult) {
        StudentVisionScreeningResultExportDTO studentVisionScreeningResultExportDTO = new StudentVisionScreeningResultExportDTO();
        studentVisionScreeningResultExportDTO.setId(studentDTO.getId());
        //姓名
        studentVisionScreeningResultExportDTO.setStudentName(EyeDataUtil.name(studentDTO));
        //学号
        studentVisionScreeningResultExportDTO.setStudentNo(EyeDataUtil.sno(studentDTO));
        //性别
        studentVisionScreeningResultExportDTO.setGenderDesc(EyeDataUtil.gender(studentDTO));

        //性别
        studentVisionScreeningResultExportDTO.setGradeName(EyeDataUtil.gradeName(studentDTO));
        //性别
        studentVisionScreeningResultExportDTO.setClassName(EyeDataUtil.className(studentDTO));
        //性别
        studentVisionScreeningResultExportDTO.setBirthday(studentDTO.getBirthday());

        //手机号码
        studentVisionScreeningResultExportDTO.setParentPhone(EyeDataUtil.phone(studentDTO));
        //地址
        studentVisionScreeningResultExportDTO.setAddress(EyeDataUtil.address(studentDTO));

        //戴镜情况
        studentVisionScreeningResultExportDTO.setGlassesType(EyeDataUtil.glassesType(visionScreeningResult));
        //右眼裸视力
        studentVisionScreeningResultExportDTO.setRightReScreenNakedVisions(EyeDataUtil.visionRightDataToStr(visionScreeningResult));
        //有眼矫正视力
        studentVisionScreeningResultExportDTO.setRightReScreenCorrectedVisions(EyeDataUtil.correcteRightDataToStr(visionScreeningResult));

        //左眼裸视力
        studentVisionScreeningResultExportDTO.setLeftReScreenNakedVisions(EyeDataUtil.visionLeftDataToStr(visionScreeningResult));
        //左眼矫正视力
        studentVisionScreeningResultExportDTO.setLeftReScreenCorrectedVisions(EyeDataUtil.correcteLeftDataToStr(visionScreeningResult));

        //右眼球镜
        studentVisionScreeningResultExportDTO.setRightReScreenSphs(EyeDataUtil.computerRightSph(visionScreeningResult));
        //右眼柱镜
        studentVisionScreeningResultExportDTO.setRightReScreenCyls(EyeDataUtil.computerRightCyl(visionScreeningResult));
        //右眼轴为
        studentVisionScreeningResultExportDTO.setRightReScreenAxials(EyeDataUtil.computerRightAxial(visionScreeningResult));
        studentVisionScreeningResultExportDTO.setRightReScreenSphericalEquivalents(EyeDataUtil.rightReScreenSph(visionScreeningResult));

        //左眼球镜
        studentVisionScreeningResultExportDTO.setLeftReScreenSphs(EyeDataUtil.computerLeftSph(visionScreeningResult));
        //左眼柱镜
        studentVisionScreeningResultExportDTO.setLeftReScreenCyls(EyeDataUtil.computerLeftCyl(visionScreeningResult));
        //左眼州为
        studentVisionScreeningResultExportDTO.setLeftReScreenAxials(EyeDataUtil.computerLeftAxial(visionScreeningResult));
        studentVisionScreeningResultExportDTO.setLeftReScreenSphericalEquivalents(EyeDataUtil.leftReScreenSph(visionScreeningResult));

        return studentVisionScreeningResultExportDTO;
    }

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

    public static String computerRightAxial(VisionScreeningResult visionScreeningResult) {
        if (visionScreeningResult != null
                && visionScreeningResult.getComputerOptometry() != null
                && visionScreeningResult.getComputerOptometry().getRightEyeData() != null
                && visionScreeningResult.getComputerOptometry().getRightEyeData().getAxial() != null) {

            return visionScreeningResult.getComputerOptometry().getRightEyeData().getAxial().setScale(0, RoundingMode.DOWN).toString();
        }

        return "--";
    }


    public static String computerLeftCyl(VisionScreeningResult visionScreeningResult){
        if (visionScreeningResult!=null
                &&visionScreeningResult.getComputerOptometry()!=null
                &&visionScreeningResult.getComputerOptometry().getLeftEyeData()!=null
                &&visionScreeningResult.getComputerOptometry().getLeftEyeData().getCyl()!=null){

            return setSphCyl(visionScreeningResult.getComputerOptometry().getLeftEyeData().getCyl());
        }

        return "--";
    }

    public static String computerLeftCylNull(VisionScreeningResult visionScreeningResult){
        if (visionScreeningResult!=null
                &&visionScreeningResult.getComputerOptometry()!=null
                &&visionScreeningResult.getComputerOptometry().getLeftEyeData()!=null
                &&visionScreeningResult.getComputerOptometry().getLeftEyeData().getCyl()!=null){

            return visionScreeningResult.getComputerOptometry().getLeftEyeData().getCyl().toString();
        }

        return null;
    }

    public static String computerRightCyl(VisionScreeningResult visionScreeningResult){
        if (visionScreeningResult!=null
                &&visionScreeningResult.getComputerOptometry()!=null
                &&visionScreeningResult.getComputerOptometry().getRightEyeData()!=null
                &&visionScreeningResult.getComputerOptometry().getRightEyeData().getCyl()!=null){

            return setSphCyl(visionScreeningResult.getComputerOptometry().getRightEyeData().getCyl());
        }

        return "--";
    }
    public static String computerRightCylNull(VisionScreeningResult visionScreeningResult){
        if (visionScreeningResult!=null
                &&visionScreeningResult.getComputerOptometry()!=null
                &&visionScreeningResult.getComputerOptometry().getRightEyeData()!=null
                &&visionScreeningResult.getComputerOptometry().getRightEyeData().getCyl()!=null){

            return visionScreeningResult.getComputerOptometry().getRightEyeData().getCyl().toString();
        }

        return null;
    }


    public static String computerLeftSph(VisionScreeningResult visionScreeningResult){
        if (visionScreeningResult!=null
                &&visionScreeningResult.getComputerOptometry()!=null
                &&visionScreeningResult.getComputerOptometry().getLeftEyeData()!=null
                &&visionScreeningResult.getComputerOptometry().getLeftEyeData().getSph()!=null){

            return setSphCyl(visionScreeningResult.getComputerOptometry().getLeftEyeData().getSph());
        }

        return "--";
    }
    public static String computerLeftSphNull(VisionScreeningResult visionScreeningResult){
        if (visionScreeningResult!=null
                &&visionScreeningResult.getComputerOptometry()!=null
                &&visionScreeningResult.getComputerOptometry().getLeftEyeData()!=null
                &&visionScreeningResult.getComputerOptometry().getLeftEyeData().getSph()!=null){

            return visionScreeningResult.getComputerOptometry().getLeftEyeData().getSph().toString();
        }

        return null;
    }



    public static String computerRightSph(VisionScreeningResult visionScreeningResult){
        if (visionScreeningResult!=null
                &&visionScreeningResult.getComputerOptometry()!=null
                &&visionScreeningResult.getComputerOptometry().getRightEyeData()!=null
                &&visionScreeningResult.getComputerOptometry().getRightEyeData().getSph()!=null){

            return setSphCyl(visionScreeningResult.getComputerOptometry().getRightEyeData().getSph());
        }

        return "--";
    }

    public static String computerRightSphNULL(VisionScreeningResult visionScreeningResult){
        if (visionScreeningResult!=null
                &&visionScreeningResult.getComputerOptometry()!=null
                &&visionScreeningResult.getComputerOptometry().getRightEyeData()!=null
                &&visionScreeningResult.getComputerOptometry().getRightEyeData().getSph()!=null){

            return visionScreeningResult.getComputerOptometry().getRightEyeData().getSph().toString();
        }
        return null;
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

            return visionScreeningResult.getVisionData().getRightEyeData().getCorrectedVision().setScale(0, RoundingMode.DOWN).toString();
        }

        return "--";
    }

    public static String visionRightDataToStr(VisionScreeningResult visionScreeningResult){
        if (visionScreeningResult!=null
                &&visionScreeningResult.getVisionData()!=null
                &&visionScreeningResult.getVisionData().getRightEyeData()!=null
                &&visionScreeningResult.getVisionData().getRightEyeData().getNakedVision()!=null){


            return visionScreeningResult.getVisionData().getRightEyeData().getNakedVision().setScale(0, RoundingMode.DOWN).toString();
        }

        return "--";
    }

    public static String visionLeftDataToStr(VisionScreeningResult visionScreeningResult){
        if (resultData(visionScreeningResult)){
            return visionScreeningResult.getVisionData().getLeftEyeData().getNakedVision().toString();
        }
        return "--";
    }

    public static boolean resultData(VisionScreeningResult visionScreeningResult) {
        return visionScreeningResult != null
                && visionScreeningResult.getVisionData() != null
                && visionScreeningResult.getVisionData().getLeftEyeData() != null
                && visionScreeningResult.getVisionData().getLeftEyeData().getNakedVision() != null;
    }
    public static VisionScreeningResult getVisionScreeningResult(ScreeningStudentDTO studentEyeInfor, Map<Integer, List<VisionScreeningResult>> visionScreeningResultsGroup) {
        Integer id = studentEyeInfor.getId();
        List<VisionScreeningResult>  visionScreeningResults =  visionScreeningResultsGroup.get(id);
        if(visionScreeningResults==null||visionScreeningResults.isEmpty()){
            return null;
        }
        return visionScreeningResults.get(0);
    }

    private static String setSphCyl(BigDecimal bigDecimal){
        int r=bigDecimal.compareTo(BigDecimal.ZERO);
        if (r>0||r==0){
            return "+"+bigDecimal.toString();
        }
        return bigDecimal.toString();
    }

}
