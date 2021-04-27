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
    private VisionScreeningResult visionScreeningResult;
    private ScreeningResultBasicData screeningResultBasicData;
    private ScreeningPlanSchoolStudent screeningPlanSchoolStudent;
    private boolean isEnd;
    private boolean  isDoubleScreen;
    public VisionScreeningResult build() {
        synchronized (this) {
            if (isEnd) {
                throw new ManagementUncheckedException("ScreeningResultBuilder 已完成build，请新建builder构建另外的对象");
            }
            if (screeningPlanSchoolStudent == null || screeningPlanSchoolStudent == null) {
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

    public ScreeningResultBuilder setVisionScreeningResult(VisionScreeningResult visionScreeningResult) {
        this.visionScreeningResult = visionScreeningResult;
        return this;
    }

    public ScreeningResultBuilder setScreeningResultBasicData(ScreeningResultBasicData screeningResultBasicData) {
        this.screeningResultBasicData = screeningResultBasicData;
        return this;
    }

    public ScreeningResultBuilder setScreeningPlanSchoolStudent(ScreeningPlanSchoolStudent screeningPlanSchoolStudent) {
        this.screeningPlanSchoolStudent = screeningPlanSchoolStudent;
        return this;
    }

    public ScreeningResultBuilder setIsDoubleScreen(boolean isDoubleScreen) {
        this.isDoubleScreen = isDoubleScreen;
        return this;
    }
}
