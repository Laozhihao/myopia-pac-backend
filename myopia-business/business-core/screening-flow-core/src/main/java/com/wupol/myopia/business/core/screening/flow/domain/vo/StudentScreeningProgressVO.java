package com.wupol.myopia.business.core.screening.flow.domain.vo;

import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.core.screening.flow.domain.dos.AbstractDiagnosisResult;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 * @Author HaoHao
 * @Date 2021/8/22
 **/
@Accessors(chain = true)
@Data
public class StudentScreeningProgressVO {
    /** 已检查无异常 */
    public static final int NORMAL = 1;
    /** 已检查有异常 */
    public static final int ABNORMAL = 2;
    /** 未检查（必做项目） */
    public static final int UNCHECK_MUST = 3;
    /** 未检查（非必做项目） */
    public static final int UNCHECK = 0;

    /** 用于标记是否存在未完成的必查项 */
    private static ThreadLocal<Boolean> isAllMustCheckDone = new ThreadLocal<>();
    /** 用于标记初诊中是否存在异常 */
    private static ThreadLocal<Boolean> hasAbnormalFlag = new ThreadLocal<>();

    /** 学生ID */
    private String studentId;
    /** 学籍号 */
    private String studentNo;
    /** 用户名称 */
    private String studentName;
    /** 出生日期 */
    private String birthday;
    /** 性别 */
    private String sex;
    /** 学龄段 */
    private Integer gradeType;
    /** 学校ID */
    private Integer schoolId;

    /** 筛查结果 */
    private Boolean result;
    /** 视力检查 */
    private Integer visionStatus;
    /** 眼位 */
    private Integer eyePositionStatus;
    /** 裂隙灯 */
    private Integer sliLampStatus;
    /** 屈光度 */
    private Integer diopterStatus;
    /** 小瞳验光 */
    private Integer pupillaryOptometryStatus;
    /** 生物测量 */
    private Integer biometricsStatus;
    /** 眼压 */
    private Integer pressureStatus;
    /** 眼底检查 */
    private Integer fundusStatus;
    /** 其他眼病 */
    private Integer otherStatus;
    /** 是否有异常 */
    private Boolean hasAbnormal;

    /**
     * [注意！！！]下面前四个的赋值顺序不能改变：视力-眼位-裂隙灯-电脑验光
     * 1. 托幼机构
     *      初查项目：视力检查、眼位
     *      以上任意一个初查项目结果(疑似）异常，则：裂隙灯检查、小瞳验光、眼底检查项目必做
     * 2. 中小学生
     *      初查项目：视力检查、眼位、裂隙灯、屈光度
     *      以上任意一个初查项目结果(疑似）异常，则：小瞳验光、生物测量、眼压、眼底检查项目必做
     *
     * @param screeningResult 筛查结果
     * @param studentVO 学生信息
     * @return com.wupol.myopia.business.core.screening.flow.domain.vo.StudentScreeningProgressVO
     **/
    public static StudentScreeningProgressVO getInstanceWithDefault(VisionScreeningResult screeningResult, StudentVO studentVO) {
        Assert.notNull(studentVO, "学生信息不能为空");
        StudentScreeningProgressVO studentScreeningProgressVO = new StudentScreeningProgressVO();
        BeanUtils.copyProperties(studentVO, studentScreeningProgressVO);
        boolean isKindergarten = SchoolAge.KINDERGARTEN.code.equals(studentVO.getGradeType());
        // 筛查结果为空则返回默认值
        if (Objects.isNull(screeningResult)) {
            return studentScreeningProgressVO.setVisionStatus(UNCHECK_MUST).setEyePositionStatus(UNCHECK_MUST).setSliLampStatus(isKindergarten ? UNCHECK : UNCHECK_MUST).setDiopterStatus(isKindergarten ? UNCHECK : UNCHECK_MUST)
                    .setPupillaryOptometryStatus(UNCHECK).setBiometricsStatus(UNCHECK).setPressureStatus(UNCHECK).setFundusStatus(UNCHECK).setOtherStatus(UNCHECK).setResult(false).setHasAbnormal(false);
        }
        // 默认完成了所有必要检查
        isAllMustCheckDone.set(true);
        // 默认没有异常
        hasAbnormalFlag.set(false);
        // 判断各个检查型的进度状态
        studentScreeningProgressVO.setVisionStatus(getProgress(screeningResult.getVisionData(), true, true));
        studentScreeningProgressVO.setEyePositionStatus(getProgress(screeningResult.getOcularInspectionData(), true,true));
        studentScreeningProgressVO.setSliLampStatus(getProgress(screeningResult.getSlitLampData(), !isKindergarten, !isKindergarten || hasAbnormalFlag.get()));
        studentScreeningProgressVO.setDiopterStatus(getProgress(screeningResult.getComputerOptometry(), !isKindergarten, !isKindergarten));
        studentScreeningProgressVO.setPupillaryOptometryStatus(getProgress(screeningResult.getPupilOptometryData(), hasAbnormalFlag.get()));
        studentScreeningProgressVO.setBiometricsStatus(getProgress(screeningResult.getBiometricData(), !isKindergarten && hasAbnormalFlag.get()));
        studentScreeningProgressVO.setPressureStatus(getProgress(screeningResult.getEyePressureData(), !isKindergarten && hasAbnormalFlag.get()));
        studentScreeningProgressVO.setFundusStatus(getProgress(screeningResult.getFundusData(), hasAbnormalFlag.get()));
        studentScreeningProgressVO.setOtherStatus(getProgress(screeningResult.getOtherEyeDiseases(), false));
        studentScreeningProgressVO.setResult(isAllMustCheckDone.get());
        studentScreeningProgressVO.setHasAbnormal(hasAbnormalFlag.get());
        isAllMustCheckDone.remove();
        hasAbnormalFlag.remove();
        return studentScreeningProgressVO;
    }

    /**
     * 获取筛查项目完成进度
     *
     * @param diagnosisResult 诊断结果
     * @param isMustCheck 是否必做项目
     * @return int 1：已检查无异常，2：已检查有异常，3：未检查（非必做），4：未检查（必做）
     **/
    private static int getProgress(AbstractDiagnosisResult diagnosisResult, boolean isMustCheck) {
        return getProgress(diagnosisResult, false, isMustCheck);
    }

    /**
     * 获取筛查项目完成进度
     *
     * @param diagnosisResult 诊断结果
     * @param isEarlyDiagnosis 是否为初诊项目
     * @param isMustCheck 是否必做项目
     * @return int 1：已检查无异常，2：已检查有异常，3：未检查（非必做），4：未检查（必做）
     **/
    private static int getProgress(AbstractDiagnosisResult diagnosisResult, boolean isEarlyDiagnosis, boolean isMustCheck) {
        if (Objects.isNull(diagnosisResult)) {
            // 必查项未检查时，标记为未完成筛查
            if (isMustCheck) {
                isAllMustCheckDone.set(false);
                return UNCHECK_MUST;
            }
            return UNCHECK;
        }
        if (diagnosisResult.isNormal()) {
            return NORMAL;
        }
        // 初诊项目中出现异常时，记录标记，用于判断是否需要做进一步的筛查项目
        if (isEarlyDiagnosis) {
            hasAbnormalFlag.set(true);
        }
        return ABNORMAL;
    }
}
