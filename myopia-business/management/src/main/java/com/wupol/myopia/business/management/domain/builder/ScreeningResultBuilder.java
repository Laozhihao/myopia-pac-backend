package com.wupol.myopia.business.management.domain.builder;

import com.myopia.common.exceptions.ManagementUncheckedException;
import com.wupol.myopia.business.management.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.management.domain.model.VisionScreeningResult;
import lombok.Getter;
import lombok.experimental.Accessors;

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
                .setDistrictId(screeningPlanSchoolStudent.getDistrictId())
                .setStudentId(screeningPlanSchoolStudent.getStudentId())
                .setPlanId(screeningPlanSchoolStudent.getScreeningPlanId())
                .setSchoolId(screeningPlanSchoolStudent.getSchoolId())
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
}
