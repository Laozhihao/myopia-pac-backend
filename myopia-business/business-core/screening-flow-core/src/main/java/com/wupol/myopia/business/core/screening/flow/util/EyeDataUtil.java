package com.wupol.myopia.business.core.screening.flow.util;

import cn.hutool.core.util.StrUtil;
import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.common.utils.util.MaskUtil;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.screening.flow.domain.dos.ComputerOptometryDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.DeviationDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.HeightAndWeightDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.VisionDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningStudentDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StudentVisionScreeningResultExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 钓猫的小鱼
 * @Date: 2022/01/12/11:05
 * @Description:
 */
@Slf4j
@UtilityClass
public class EyeDataUtil {

    private static final String EMPTY_DATA = "--";

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
     * @param visionScreeningResult 筛查数据
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
     * 左眼轴位
     * @param visionScreeningResult 筛查数据
     * @return 左眼轴位
     */
    public static String computerLeftAxial(VisionScreeningResult visionScreeningResult){
        if (visionScreeningResult!=null
                &&visionScreeningResult.getComputerOptometry()!=null
                &&visionScreeningResult.getComputerOptometry().getLeftEyeData()!=null
                &&visionScreeningResult.getComputerOptometry().getLeftEyeData().getAxial()!=null){

            return visionScreeningResult.getComputerOptometry().getLeftEyeData().getAxial().setScale(0, RoundingMode.DOWN).toString();
        }

        return "--";
    }

    /**
     * 右眼轴位
     * @param visionScreeningResult 筛查数据
     * @return 右眼轴位
     */
    public static String computerRightAxial(VisionScreeningResult visionScreeningResult) {
        if (visionScreeningResult != null
                && visionScreeningResult.getComputerOptometry() != null
                && visionScreeningResult.getComputerOptometry().getRightEyeData() != null
                && visionScreeningResult.getComputerOptometry().getRightEyeData().getAxial() != null) {

            return visionScreeningResult.getComputerOptometry().getRightEyeData().getAxial().setScale(0, RoundingMode.DOWN).toString();
        }

        return "--";
    }

    /**
     * 左眼柱镜
     * @param visionScreeningResult 筛查数据
     * @return 左眼柱镜
     */
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

    /**
     * 右眼柱镜
     * @param visionScreeningResult 筛查数据
     * @return 右眼柱镜
     */
    public static String computerRightCyl(VisionScreeningResult visionScreeningResult){
        if (visionScreeningResult!=null
                &&visionScreeningResult.getComputerOptometry()!=null
                &&visionScreeningResult.getComputerOptometry().getRightEyeData()!=null
                &&visionScreeningResult.getComputerOptometry().getRightEyeData().getCyl()!=null){

            return setSphCyl(visionScreeningResult.getComputerOptometry().getRightEyeData().getCyl());
        }

        return "--";
    }

    /**
     * 右眼柱镜非空验证
     * @param visionScreeningResult 筛查数据
     * @return 右眼柱镜非空验证
     */
    public static String computerRightCylNull(VisionScreeningResult visionScreeningResult){
        if (visionScreeningResult!=null
                &&visionScreeningResult.getComputerOptometry()!=null
                &&visionScreeningResult.getComputerOptometry().getRightEyeData()!=null
                &&visionScreeningResult.getComputerOptometry().getRightEyeData().getCyl()!=null){

            return visionScreeningResult.getComputerOptometry().getRightEyeData().getCyl().toString();
        }

        return null;
    }

    /**
     * 左眼球镜
     * @param visionScreeningResult 筛查数据
     * @return 左眼球镜
     */
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

    /**
     * 右眼球镜
     * @param visionScreeningResult 筛查数据
     * @return 右眼球镜
     */
    public static String computerRightSph(VisionScreeningResult visionScreeningResult){
        if (visionScreeningResult!=null
                &&visionScreeningResult.getComputerOptometry()!=null
                &&visionScreeningResult.getComputerOptometry().getRightEyeData()!=null
                &&visionScreeningResult.getComputerOptometry().getRightEyeData().getSph()!=null){

            return setSphCyl(visionScreeningResult.getComputerOptometry().getRightEyeData().getSph());
        }
        return "--";
    }
    /**
     * 右眼球镜非空验证
     * @param visionScreeningResult 筛查数据
     * @return 右眼球镜非空验证
     */
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
     * 获取右眼裸视力
     * @param visionScreeningResult 筛查结果
     * @return 右眼裸视力
     */
    public static BigDecimal rightNakedVision(VisionScreeningResult visionScreeningResult) {
        return Optional.ofNullable(visionScreeningResult) .map(VisionScreeningResult::getVisionData) .map(VisionDataDO::getRightEyeData)
                .map(VisionDataDO.VisionData::getNakedVision) .orElse(null);
    }

    /**
     * 获取左眼裸视力
     * @param visionScreeningResult 筛查结果
     * @return 左眼裸视力
     */
    public static BigDecimal leftNakedVision(VisionScreeningResult visionScreeningResult) {
        return Optional.ofNullable(visionScreeningResult) .map(VisionScreeningResult::getVisionData) .map(VisionDataDO::getLeftEyeData)
                .map(VisionDataDO.VisionData::getNakedVision) .orElse(null);
    }

    /**
     * 获取右眼裸视力
     * @param visionScreeningResult 筛查结果
     * @return 右眼戴镜视力
     */
    public static BigDecimal rightCorrectedVision(VisionScreeningResult visionScreeningResult) {
        return Optional.ofNullable(visionScreeningResult) .map(VisionScreeningResult::getVisionData) .map(VisionDataDO::getRightEyeData)
                .map(VisionDataDO.VisionData::getCorrectedVision) .orElse(null);
    }

    /**
     * 获取左眼裸视力
     * @param visionScreenResult 筛查结果
     * @return 左眼戴镜视力
     */
    public static BigDecimal leftCorrectedVision(VisionScreeningResult visionScreenResult) {
        return Optional.ofNullable(visionScreenResult) .map(VisionScreeningResult::getVisionData) .map(VisionDataDO::getLeftEyeData)
                .map(VisionDataDO.VisionData::getCorrectedVision) .orElse(null);
    }

    /**
     * 获取右眼球镜
     * @param visionScreenResult 筛查结果
     * @return 右眼球镜
     */
    public static BigDecimal rightSph(VisionScreeningResult visionScreenResult) {
        return Optional.ofNullable(visionScreenResult) .map(VisionScreeningResult::getComputerOptometry) .map(ComputerOptometryDO::getRightEyeData)
                .map(ComputerOptometryDO.ComputerOptometry::getSph) .orElse(null);
    }
    /**
     * 获取右眼柱镜
     * @param visionScreenResult 筛查结果
     * @return 右眼柱镜
     */
    public static BigDecimal rightCyl(VisionScreeningResult visionScreenResult) {
        return Optional.ofNullable(visionScreenResult) .map(VisionScreeningResult::getComputerOptometry) .map(ComputerOptometryDO::getRightEyeData)
                .map(ComputerOptometryDO.ComputerOptometry::getCyl) .orElse(null);
    }
    /**
     * 获取右眼轴位
     * @param visionScreenResult 筛查结果
     * @return 筛查轴位
     */
    public static BigDecimal rightAxial(VisionScreeningResult visionScreenResult) {
        return Optional.ofNullable(visionScreenResult) .map(VisionScreeningResult::getComputerOptometry) .map(ComputerOptometryDO::getRightEyeData)
                .map(ComputerOptometryDO.ComputerOptometry::getAxial) .orElse(null);
    }

    /**
     * 获取左眼球镜
     * @param visionScreenResult 筛查结果
     * @return 左眼球镜
     */
    public static BigDecimal leftSph(VisionScreeningResult visionScreenResult) {
        return Optional.ofNullable(visionScreenResult) .map(VisionScreeningResult::getComputerOptometry) .map(ComputerOptometryDO::getLeftEyeData)
                .map(ComputerOptometryDO.ComputerOptometry::getSph) .orElse(null);
    }
    /**
     * 获取左眼柱镜
     * @param visionScreenResult 筛查结果
     * @return 左眼柱镜
     */
    public static BigDecimal leftCyl(VisionScreeningResult visionScreenResult) {
        return Optional.ofNullable(visionScreenResult) .map(VisionScreeningResult::getComputerOptometry) .map(ComputerOptometryDO::getLeftEyeData)
                .map(ComputerOptometryDO.ComputerOptometry::getCyl) .orElse(null);
    }
    /**
     * 获取左眼轴位
     * @param visionScreenResult 筛查结果
     * @return 左眼轴位
     */
    public static BigDecimal leftAxial(VisionScreeningResult visionScreenResult) {
        return Optional.ofNullable(visionScreenResult) .map(VisionScreeningResult::getComputerOptometry) .map(ComputerOptometryDO::getLeftEyeData)
                .map(ComputerOptometryDO.ComputerOptometry::getAxial) .orElse(null);
    }

    /**
     * 获取身高
     * @param visionScreenResult 筛查结果
     * @return 身高
     */
    public static BigDecimal height(VisionScreeningResult visionScreenResult) {
        return Optional.ofNullable(visionScreenResult) .map(VisionScreeningResult::getHeightAndWeightData)
                .map(HeightAndWeightDataDO::getHeight).orElse(null);
    }

    /**
     * 获取体重
     * @param visionScreenResult 筛查结果
     * @return 体重
     */
    public static BigDecimal weight(VisionScreeningResult visionScreenResult) {
        return Optional.ofNullable(visionScreenResult) .map(VisionScreeningResult::getHeightAndWeightData)
                .map(HeightAndWeightDataDO::getWeight).orElse(null);
    }

    /**
     * 计算 等效球镜（右眼）
     * @param visionScreenResult 筛查数据
     * @return 计算 等效球镜（右眼）
     */
    public static BigDecimal rightSE(VisionScreeningResult visionScreenResult) {
        BigDecimal sph = rightSph(visionScreenResult);
        BigDecimal cyl = rightCyl(visionScreenResult);
        return StatUtil.getSphericalEquivalent(sph, cyl);
    }

    /**
     * 计算 等效球镜（左眼）
     * @param visionScreenResult 筛查数据
     * @return 计算 等效球镜（右眼）
     */
    public static BigDecimal leftSE(VisionScreeningResult visionScreenResult) {
        BigDecimal sph = leftSph(visionScreenResult);
        BigDecimal cyl = leftCyl(visionScreenResult);
        return StatUtil.getSphericalEquivalent(sph, cyl);
    }

    /**
     * 电脑验光误差
     * @param visionScreenResult 筛查结果
     * @return 电脑验光误差
     */
    public static DeviationDO.VisionOrOptometryDeviation optometryDeviation(VisionScreeningResult visionScreenResult) {
        return Optional.ofNullable(visionScreenResult) .map(VisionScreeningResult::getDeviationData)
                .map(DeviationDO::getVisionOrOptometryDeviation).orElse(null);
    }

    /**
     * 身高/体重误差说明
     * @param visionScreenResult 筛查结果
     * @return 身高/体重误差说明
     */
    public static DeviationDO.HeightWeightDeviation heightWeightDeviationRemark(VisionScreeningResult visionScreenResult) {
        return Optional.ofNullable(visionScreenResult) .map(VisionScreeningResult::getDeviationData)
                .map(DeviationDO::getHeightWeightDeviation).orElse(null);
    }

    /**
     * 误差结果
     * @param visionScreenResult 筛查结果
     * @return 误差结果
     */
    public static DeviationDO deviationData(VisionScreeningResult visionScreenResult) {
        return Optional.ofNullable(visionScreenResult) .map(VisionScreeningResult::getDeviationData) .orElse(null);
    }

    /**
     * 更新时间
     * @param visionScreenResult 筛查结果
     * @return 更新时间
     */
    public static Date updateTime(VisionScreeningResult visionScreenResult) {
        return Optional.ofNullable(visionScreenResult) .map(VisionScreeningResult::getUpdateTime) .orElse(null);
    }

    /**
     * 戴镜类型
     * @param visionScreenResult 筛查结果
     * @return 戴镜类型
     */
    public static Integer glassesType(VisionScreeningResult visionScreenResult){
        return Optional.ofNullable(visionScreenResult) .map(VisionScreeningResult::getVisionData).map(VisionDataDO::getRightEyeData)
                .map(VisionDataDO.VisionData::getGlassesType) .orElse(null);
    }

    /**
     * 合并左右眼数据
     *
     * @param right 右眼
     * @param left  左眼
     *
     * @return 右眼/左眼
     */
    public static String mergeEyeData(String right, String left) {
        return right + StrUtil.SLASH + left;
    }

    /**
     * 课桌椅建议
     *
     * @param heightStr 身高
     * @param schoolAge 学龄
     *
     * @return TwoTuple<String, String>
     */
    public static TwoTuple<String, String> getDeskChairSuggest(String heightStr, Integer schoolAge) {
        if (StringUtils.isEmpty(heightStr) || Objects.isNull(schoolAge)) {
            return new TwoTuple<>(EMPTY_DATA, EMPTY_DATA);
        }
        float height = new BigDecimal(heightStr).floatValue();
        List<Integer> deskAndChairType = SchoolAge.KINDERGARTEN.code.equals(schoolAge) ? DeskChairTypeEnum.getKindergartenTypeByHeight(height) : DeskChairTypeEnum.getPrimarySecondaryTypeByHeight(height);
        String deskAndChairTypeDesc = deskAndChairType.stream().map(x -> x + "号").collect(Collectors.joining("或"));
        return new TwoTuple<>(com.wupol.myopia.base.util.StrUtil.spliceChar("，建议桌面高", deskAndChairTypeDesc, BigDecimal.valueOf(height * 0.43).setScale( 0, RoundingMode.DOWN).toString()),
                com.wupol.myopia.base.util.StrUtil.spliceChar("，建议座面高", deskAndChairTypeDesc, BigDecimal.valueOf(height * 0.24).setScale( 0, RoundingMode.DOWN).toString()));
    }

    /**
     * 获取屈光情况描述
     *
     * @param statConclusion 结论
     * @param isKindergarten 是否幼儿园
     *
     * @return 屈光情况
     */
    public static String getRefractiveResultDesc(StatConclusion statConclusion, boolean isKindergarten) {
        if (Objects.isNull(statConclusion)) {
            return StringUtils.EMPTY;
        }

        if (isKindergarten) {
            List<String> result = new ArrayList<>();
            if (Objects.equals(statConclusion.getWarningLevel(), WarningLevel.ZERO_SP.code)) {
                result.add(WarningLevel.ZERO_SP.desc);
            }
            if (Objects.equals(statConclusion.getIsAnisometropia(), Boolean.TRUE)) {
                result.add("屈光参差");
            }
            if (Objects.equals(statConclusion.getIsRefractiveError(), Boolean.TRUE)) {
                result.add("屈光不正");
            }
            return String.join(",", result);
        }

        List<String> result = new ArrayList<>();
        result.add(MyopiaLevelEnum.getDesc(statConclusion.getMyopiaLevel()));
        result.add(HyperopiaLevelEnum.getDesc(statConclusion.getHyperopiaLevel()));
        result.add(AstigmatismLevelEnum.getDesc(statConclusion.getAstigmatismLevel()));
        return result.stream().filter(StringUtils::isNotBlank).distinct().collect(Collectors.joining(","));
    }

    /**
     * 获取身高
     * @param visionScreenResult 筛查结果
     * @return 身高
     */
    public static String heightToStr(VisionScreeningResult visionScreenResult) {
        BigDecimal bigDecimal = Optional.ofNullable(visionScreenResult).map(VisionScreeningResult::getHeightAndWeightData)
                .map(HeightAndWeightDataDO::getHeight).orElse(null);
        if (Objects.isNull(bigDecimal)) {
            return StringUtils.EMPTY;
        }
        return bigDecimal.toString();
    }
}
