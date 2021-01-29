package com.wupol.myopia.business.management.domain.builder;

import com.myopia.common.exceptions.ManagementUncheckedException;
import com.wupol.myopia.business.management.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.management.domain.model.ScreeningPlan;
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
    private ScreeningPlan screeningPlan;
    private boolean isEnd;

    public VisionScreeningResult build() {
        synchronized (this) {
            if (isEnd) {
                throw new ManagementUncheckedException("ScreeningResultBuilder 已完成build，请新建builder构建另外的对象");
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
        return visionScreeningResult.setTaskId(screeningPlan.getScreeningTaskId())
                .setDistrictId(screeningPlan.getDistrictId())
                .setPlanId(screeningPlan.getId())
                .setSchoolId(screeningResultBasicData.getSchoolId())
                .setStudentId(screeningResultBasicData.getStudentId())
                .setScreeningOrgId(screeningPlan.getScreeningOrgId())
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

    public ScreeningResultBuilder setScreeningPlan(ScreeningPlan screeningPlan) {
        this.screeningPlan = screeningPlan;
        return this;
    }
}
