package com.wupol.myopia.business.core.screening.flow.domain.builder;

import com.wupol.myopia.business.common.utils.exception.ManagementUncheckedException;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @Description
 * @Date 2021/1/25 23:22
 * @Author by Jacob
 */
@Getter
@Accessors(chain = true)
public class ScreeningResultBuilder {
    /**
     * 筛查结果
     */
    private VisionScreeningResult visionScreeningResult;
    /**
     * 筛查结果的基础数据
     */
    private ScreeningResultBasicData screeningResultBasicData;
    /**
     * 计划筛查的学生
     */
    private ScreeningPlanSchoolStudent screeningPlanSchoolStudent;
    /**
     * 是否完成构建
     */
    private boolean isEnd;
    /**
     * 是否复查
     */
    private boolean  isDoubleScreen;
    public VisionScreeningResult build() {
        synchronized (this) {
            if (isEnd) {
                throw new ManagementUncheckedException("ScreeningResultBuilder 已完成build，请新建builder构建另外的对象");
            }
            if (screeningPlanSchoolStudent == null) {
                throw new ManagementUncheckedException("缺少参数，无法创建对象");
            }
            //校验参数
            if (visionScreeningResult == null || visionScreeningResult.getId() == null) {
                visionScreeningResult = screeningResultBasicData.buildScreeningResultData(new VisionScreeningResult());
                this.setOtherInfo();
            } else {
                screeningResultBasicData.buildScreeningResultData(visionScreeningResult);
                visionScreeningResult.setUpdateTime(new Date());
            }
            isEnd = true;
            return visionScreeningResult;
        }
    }

    /**
     * 设置其他信息
     */
    private VisionScreeningResult setOtherInfo() {
        return visionScreeningResult
                .setTaskId(screeningPlanSchoolStudent.getScreeningTaskId())
                .setDistrictId(screeningPlanSchoolStudent.getSchoolDistrictId())
                .setStudentId(screeningPlanSchoolStudent.getStudentId())
                .setPlanId(screeningPlanSchoolStudent.getScreeningPlanId())
                .setSchoolId(screeningPlanSchoolStudent.getSchoolId())
                .setIsDoubleScreen(isDoubleScreen)
                .setScreeningPlanSchoolStudentId(screeningPlanSchoolStudent.getId())
                .setScreeningOrgId(screeningPlanSchoolStudent.getScreeningOrgId())
                .setCreateUserId(screeningResultBasicData.getCreateUserId());
    }

    /**
     * 设置数据筛查的结果
     * @param visionScreeningResult
     * @return
     */
    public ScreeningResultBuilder setVisionScreeningResult(VisionScreeningResult visionScreeningResult) {
        this.visionScreeningResult = visionScreeningResult;
        return this;
    }

    /**
     * 设置筛查结果的基本数据
     * @param screeningResultBasicData
     * @return
     */
    public ScreeningResultBuilder setScreeningResultBasicData(ScreeningResultBasicData screeningResultBasicData) {
        this.screeningResultBasicData = screeningResultBasicData;
        return this;
    }

    /**
     * 设置计划筛查的学生数据
     * @param screeningPlanSchoolStudent
     * @return
     */
    public ScreeningResultBuilder setScreeningPlanSchoolStudent(ScreeningPlanSchoolStudent screeningPlanSchoolStudent) {
        this.screeningPlanSchoolStudent = screeningPlanSchoolStudent;
        return this;
    }

    /**
     * 设置是否复诊
     * @param isDoubleScreen
     * @return
     */
    public ScreeningResultBuilder setIsDoubleScreen(boolean isDoubleScreen) {
        this.isDoubleScreen = isDoubleScreen;
        return this;
    }
}
