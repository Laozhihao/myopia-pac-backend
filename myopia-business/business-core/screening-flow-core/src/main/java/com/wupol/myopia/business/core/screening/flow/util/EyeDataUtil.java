package com.wupol.myopia.business.core.screening.flow.util;

import com.wupol.myopia.business.common.utils.constant.WearingGlassesSituation;
import com.wupol.myopia.business.common.utils.util.MaskUtil;
import com.wupol.myopia.business.core.screening.flow.domain.dos.DeviationDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningStudentDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StudentVisionScreeningResultExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        studentVisionScreeningResultExportDTO.setScreeningCode(Objects.nonNull(studentDTO.getScreeningCode()) ? studentDTO.getScreeningCode().toString() : StringUtils.EMPTY);
        //姓名
        studentVisionScreeningResultExportDTO.setStudentName(EyeDataUtil.name(studentDTO));
        //学号
        studentVisionScreeningResultExportDTO.setStudentNo(EyeDataUtil.sno(studentDTO));
        //性别
        studentVisionScreeningResultExportDTO.setGenderDesc(EyeDataUtil.gender(studentDTO));
        // 证件信息
        studentVisionScreeningResultExportDTO.setCredential(StringUtils.isNotBlank(studentDTO.getIdCard()) ? MaskUtil.maskIdCard(studentDTO.getIdCard()) : MaskUtil.maskPassport(studentDTO.getPassport()));

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
        studentVisionScreeningResultExportDTO.setGlassesType(EyeDataUtil.glassesTypeString(visionScreeningResult));
        //右眼裸视力
        studentVisionScreeningResultExportDTO.setRightReScreenNakedVisions(EyeDataUtil.visionRightDataToStr(visionScreeningResult));
        //右眼矫正视力
        studentVisionScreeningResultExportDTO.setRightReScreenCorrectedVisions(EyeDataUtil.correctedRightDataToStr(visionScreeningResult));

        //左眼裸视力
        studentVisionScreeningResultExportDTO.setLeftReScreenNakedVisions(EyeDataUtil.visionLeftDataToStr(visionScreeningResult));
        //左眼矫正视力
        studentVisionScreeningResultExportDTO.setLeftReScreenCorrectedVisions(EyeDataUtil.correctedLeftDataToStr(visionScreeningResult));

        //右眼球镜
        studentVisionScreeningResultExportDTO.setRightReScreenSphs(EyeDataUtil.computerRightSph(visionScreeningResult));
        //右眼柱镜
        studentVisionScreeningResultExportDTO.setRightReScreenCyls(EyeDataUtil.computerRightCyl(visionScreeningResult));
        //右眼轴向
        studentVisionScreeningResultExportDTO.setRightReScreenAxials(EyeDataUtil.computerRightAxial(visionScreeningResult));
        studentVisionScreeningResultExportDTO.setRightReScreenSphericalEquivalents(EyeDataUtil.rightReScreenSph(visionScreeningResult));

        //左眼球镜
        studentVisionScreeningResultExportDTO.setLeftReScreenSphs(EyeDataUtil.computerLeftSph(visionScreeningResult));
        //左眼柱镜
        studentVisionScreeningResultExportDTO.setLeftReScreenCyls(EyeDataUtil.computerLeftCyl(visionScreeningResult));
        //左眼轴向
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
        BigDecimal sph = visionScreeningResult.getComputerOptometry().getLeftEyeData().getSph();
        BigDecimal cyl = visionScreeningResult.getComputerOptometry().getLeftEyeData().getCyl();
        if (Objects.isNull(sph) || Objects.isNull(cyl)){
            return "--";
        }
        BigDecimal halfCyl = cyl.divide(new BigDecimal(2),2,BigDecimal.ROUND_HALF_UP);
        return sph.add(halfCyl).toString();
    }

    /**
     * 等效球镜 = 球镜+柱镜/2
     * @param visionScreeningResult
     * @return 等效球镜
     */
    public static String rightReScreenSph(VisionScreeningResult visionScreeningResult){
        if (visionScreeningResult==null
                ||visionScreeningResult.getComputerOptometry()==null
                ||visionScreeningResult.getComputerOptometry().getRightEyeData()==null){

            return "--";
        }
        BigDecimal sph = visionScreeningResult.getComputerOptometry().getRightEyeData().getSph();
        BigDecimal cyl = visionScreeningResult.getComputerOptometry().getRightEyeData().getCyl();
        if (Objects.isNull(sph) || Objects.isNull(cyl)){
            return "--";
        }
        BigDecimal halfCyl = cyl.divide(new BigDecimal(2),2,BigDecimal.ROUND_HALF_UP);
        return sph.add(halfCyl).toString();
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

    /**
     * 戴镜类型
     * @param visionScreeningResult 筛查结果
     * @return 戴镜类型
     */
    public static String glassesTypeString(VisionScreeningResult visionScreeningResult){
        if (visionScreeningResult!=null
                &&visionScreeningResult.getVisionData()!=null
                &&visionScreeningResult.getVisionData().getRightEyeData()!=null
                &&visionScreeningResult.getVisionData().getRightEyeData().getGlassesType()!=null){

            return WearingGlassesSituation.getType(visionScreeningResult.getVisionData().getRightEyeData().getGlassesType());
        }

        return "--";
    }

    /**
     * 戴镜类型
     * @param visionScreeningResult 筛查结果
     * @return 戴镜类型
     */
    public static Integer glassesType(VisionScreeningResult visionScreeningResult){
        if (visionScreeningResult!=null
                &&visionScreeningResult.getVisionData()!=null
                &&visionScreeningResult.getVisionData().getRightEyeData()!=null){

            return visionScreeningResult.getVisionData().getRightEyeData().getGlassesType();
        }
        return null;
    }


    public static String computerLeftAxial(VisionScreeningResult visionScreeningResult){
        if (visionScreeningResult!=null
                &&visionScreeningResult.getComputerOptometry()!=null
                &&visionScreeningResult.getComputerOptometry().getLeftEyeData()!=null
                &&visionScreeningResult.getComputerOptometry().getLeftEyeData().getAxial()!=null){

            return visionScreeningResult.getComputerOptometry().getLeftEyeData().getAxial().setScale(0, RoundingMode.DOWN).toString();
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


    public static String correctedLeftDataToStr(VisionScreeningResult visionScreeningResult){
        if (visionScreeningResult!=null
                &&visionScreeningResult.getVisionData()!=null
                &&visionScreeningResult.getVisionData().getLeftEyeData()!=null
                &&visionScreeningResult.getVisionData().getLeftEyeData().getCorrectedVision()!=null){

            return visionScreeningResult.getVisionData().getLeftEyeData().getCorrectedVision().setScale(1, RoundingMode.DOWN).toString();
        }

        return "--";
    }

    public static String correctedRightDataToStr(VisionScreeningResult visionScreeningResult){
        if (visionScreeningResult!=null
                &&visionScreeningResult.getVisionData()!=null
                &&visionScreeningResult.getVisionData().getRightEyeData()!=null
                &&visionScreeningResult.getVisionData().getRightEyeData().getCorrectedVision()!=null){

            return visionScreeningResult.getVisionData().getRightEyeData().getCorrectedVision().setScale(1, RoundingMode.DOWN).toString();
        }

        return "--";
    }

    public static String visionRightDataToStr(VisionScreeningResult visionScreeningResult){
        if (visionScreeningResult!=null
                &&visionScreeningResult.getVisionData()!=null
                &&visionScreeningResult.getVisionData().getRightEyeData()!=null
                &&visionScreeningResult.getVisionData().getRightEyeData().getNakedVision()!=null){

            return visionScreeningResult.getVisionData().getRightEyeData().getNakedVision().setScale(1, RoundingMode.DOWN).toString();
        }

        return "--";
    }

    public static String visionLeftDataToStr(VisionScreeningResult visionScreeningResult){
        if (resultData(visionScreeningResult)){
            return visionScreeningResult.getVisionData().getLeftEyeData().getNakedVision().setScale(1, RoundingMode.DOWN).toString();
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
        return visionScreeningResults.get(visionScreeningResults.size() - 1);
    }

    private static String setSphCyl(BigDecimal bigDecimal){
        if (Objects.isNull(bigDecimal)) {
            return "--";
        }
        int r = bigDecimal.compareTo(BigDecimal.ZERO);
        if (r >= 0){
            return "+" + bigDecimal.setScale(2, RoundingMode.DOWN).toString();
        }
        return bigDecimal.setScale(2, RoundingMode.DOWN).toString();
    }


    /**
     * 判断戴镜是否为空
     * @param visionScreeningResult 筛查结果
     * @return 类型描述
     */
    public static Integer glassTypeDesc(VisionScreeningResult visionScreeningResult) {
        if (visionScreeningResult != null
                && visionScreeningResult.getVisionData() != null
                && visionScreeningResult.getVisionData().getRightEyeData() != null) {
            return  visionScreeningResult.getVisionData().getRightEyeData().getGlassesType();
        }
        return null;
    }

    /**
     * 判断右眼裸视力
     * @param visionScreeningResult 筛查结果
     * @return 右眼裸视力
     */
    public static BigDecimal rightNakedVision(VisionScreeningResult visionScreeningResult) {
        if (visionScreeningResult != null
                && visionScreeningResult.getVisionData() != null
                && visionScreeningResult.getVisionData().getRightEyeData() != null) {
            return  visionScreeningResult.getVisionData().getRightEyeData().getNakedVision();
        }
        return null;
    }

    /**
     * 判断左眼裸视力
     * @param visionScreeningResult 筛查结果
     * @return 左眼裸视力
     */
    public static BigDecimal leftNakedVision(VisionScreeningResult visionScreeningResult) {
        if (visionScreeningResult != null
                && visionScreeningResult.getVisionData() != null
                && visionScreeningResult.getVisionData().getLeftEyeData() != null) {
            return  visionScreeningResult.getVisionData().getLeftEyeData().getNakedVision();
        }
        return null;
    }

    /**
     * 判断右眼裸视力
     * @param visionScreeningResult 筛查结果
     * @return 右眼戴镜视力
     */
    public static BigDecimal rightCorrectedVision(VisionScreeningResult visionScreeningResult) {
        if (visionScreeningResult != null
                && visionScreeningResult.getVisionData() != null
                && visionScreeningResult.getVisionData().getRightEyeData() != null) {
            return  visionScreeningResult.getVisionData().getRightEyeData().getCorrectedVision();
        }
        return null;
    }

    /**
     * 判断左眼裸视力
     * @param visionScreeningResult 筛查结果
     * @return 左眼戴镜视力
     */
    public static BigDecimal leftCorrectedVision(VisionScreeningResult visionScreeningResult) {
        if (visionScreeningResult != null
                && visionScreeningResult.getVisionData() != null
                && visionScreeningResult.getVisionData().getLeftEyeData() != null) {
            return  visionScreeningResult.getVisionData().getLeftEyeData().getCorrectedVision();
        }
        return null;
    }

    /**
     * 判断右眼球镜
     * @param visionScreeningResult 筛查结果
     * @return 右眼球镜
     */
    public static BigDecimal rightSph(VisionScreeningResult visionScreeningResult) {
        if (visionScreeningResult != null
                && visionScreeningResult.getComputerOptometry() != null
                && visionScreeningResult.getComputerOptometry().getRightEyeData() != null) {
            return  visionScreeningResult.getComputerOptometry().getRightEyeData().getSph();
        }
        return null;
    }
    /**
     * 判断右眼柱镜
     * @param visionScreeningResult 筛查结果
     * @return 右眼柱镜
     */
    public static BigDecimal rightCyl(VisionScreeningResult visionScreeningResult) {
        if (visionScreeningResult != null
                && visionScreeningResult.getComputerOptometry() != null
                && visionScreeningResult.getComputerOptometry().getRightEyeData() != null) {
            return  visionScreeningResult.getComputerOptometry().getRightEyeData().getCyl();
        }
        return null;
    }
    /**
     * 判断右眼轴位
     * @param visionScreeningResult 筛查结果
     * @return 筛查轴位
     */
    public static BigDecimal rightAxial(VisionScreeningResult visionScreeningResult) {
        if (visionScreeningResult != null
                && visionScreeningResult.getComputerOptometry() != null
                && visionScreeningResult.getComputerOptometry().getRightEyeData() != null) {
            return  visionScreeningResult.getComputerOptometry().getRightEyeData().getAxial();
        }
        return null;
    }

    /**
     * 判断左眼球镜
     * @param visionScreeningResult 筛查结果
     * @return 左眼球镜
     */
    public static BigDecimal leftSph(VisionScreeningResult visionScreeningResult) {
        if (visionScreeningResult != null
                && visionScreeningResult.getComputerOptometry() != null
                && visionScreeningResult.getComputerOptometry().getLeftEyeData() != null) {
            return  visionScreeningResult.getComputerOptometry().getLeftEyeData().getSph();
        }
        return null;
    }
    /**
     * 判断左眼柱镜
     * @param visionScreeningResult 筛查结果
     * @return 左眼柱镜
     */
    public static BigDecimal leftCyl(VisionScreeningResult visionScreeningResult) {
        if (visionScreeningResult != null
                && visionScreeningResult.getComputerOptometry() != null
                && visionScreeningResult.getComputerOptometry().getLeftEyeData() != null) {
            return  visionScreeningResult.getComputerOptometry().getLeftEyeData().getCyl();
        }
        return null;
    }
    /**
     * 判断左眼轴位
     * @param visionScreeningResult 筛查结果
     * @return 左眼轴位
     */
    public static BigDecimal leftAxial(VisionScreeningResult visionScreeningResult) {
        if (visionScreeningResult != null
                && visionScreeningResult.getComputerOptometry() != null
                && visionScreeningResult.getComputerOptometry().getLeftEyeData() != null) {
            return  visionScreeningResult.getComputerOptometry().getLeftEyeData().getAxial();
        }
        return null;
    }

    /**
     * 判断左眼轴位
     * @param visionScreeningResult 筛查结果
     * @return 身高
     */
    public static BigDecimal height(VisionScreeningResult visionScreeningResult) {
        if (visionScreeningResult != null
                && visionScreeningResult.getHeightAndWeightData() != null) {
            return  visionScreeningResult.getHeightAndWeightData().getHeight();
        }
        return null;
    }

    /**
     * 判断左眼轴位
     * @param visionScreeningResult 筛查结果
     * @return 体重
     */
    public static BigDecimal weight(VisionScreeningResult visionScreeningResult) {
        if (visionScreeningResult != null
                &&visionScreeningResult.getHeightAndWeightData() != null) {
            return  visionScreeningResult.getHeightAndWeightData().getWeight();
        }
        return null;
    }

    /**
     * 计算 等效球镜
     *
     * @param sph 球镜
     * @param cyl 柱镜
     * @return 等效球镜
     */
    public static BigDecimal calculationSE(BigDecimal sph, BigDecimal cyl) {
        if (Objects.isNull(sph) || Objects.isNull(cyl)) {
            return null;
        }
        return sph.add(cyl.multiply(new BigDecimal("0.5")))
                .setScale(2, RoundingMode.HALF_UP);
    }
    /**
     * 电脑验光误差
     * @param visionScreeningResult 筛查结果
     * @return 电脑验光误差
     */
    public static DeviationDO.VisionOrOptometryDeviation optometryDeviation(VisionScreeningResult visionScreeningResult) {
        if (visionScreeningResult != null
                &&visionScreeningResult.getDeviationData() != null) {
            return  visionScreeningResult.getDeviationData().getVisionOrOptometryDeviation();
        }
        return null;
    }

    /**
     * 身高/体重误差说明
     * @param visionScreeningResult 筛查结果
     * @return 身高/体重误差说明
     */
    public static String heightWeightDeviationRemark(VisionScreeningResult visionScreeningResult) {
        if (visionScreeningResult != null
                &&visionScreeningResult.getDeviationData() != null
                &&visionScreeningResult.getDeviationData().getHeightWeightDeviation() != null) {
            return  visionScreeningResult.getDeviationData().getHeightWeightDeviation().getRemark();
        }
        return null;
    }

    /**
     * 误差结果
     * @param visionScreeningResult 筛查结果
     * @return 误差结果
     */
    public static DeviationDO deviationData(VisionScreeningResult visionScreeningResult) {
        if (visionScreeningResult != null
                &&visionScreeningResult.getDeviationData() != null
        ) {
            return  visionScreeningResult.getDeviationData();
        }
        return null;
    }

    /**
     * 创建时间
     * @param visionScreeningResult 筛查结果
     * @return 创建时间
     */
    public static Date createTime(VisionScreeningResult visionScreeningResult) {
        if (visionScreeningResult != null) {
            return  visionScreeningResult.getCreateTime();
        }
        return null;
    }





}
