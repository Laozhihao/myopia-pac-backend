package com.wupol.myopia.business.api.screening.app.domain.vo;

import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.screening.flow.domain.dos.ComputerOptometryDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.HeightAndWeightDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.VisionDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @Description 复测学生详情
 * @Author xz
 * @Date 2022/4/27
 **/
@Accessors(chain = true)
@Data
public class RetestStudentVO {
    /**
     * 学生ID
     */
    private Integer studentId;
    /**
     * 用户名称
     */
    private String studentName;
    /**
     * 出生日期
     */
    private String birthday;
    /**
     * 性别
     */
    private String sex;
    /**
     * 复测项次
     */
    private Integer retestItemCount;
    /**
     * 错误次数
     */
    private Integer errorItemCount;
    /**
     * 检查详情
     */
    private TwoTuple<VisionScreeningResult, VisionScreeningResult> visionScreeningResult;
    /**
     * 检查详情
     */
    private TwoTuple<ScreeningResultDataVO, ScreeningResultDataVO> studentVisionScreeningResult;

    /**
     * 矫正视力误差
     */
    private boolean leftCorrectedVision;

    /**
     * 裸眼视力
     */
    private boolean leftNakedVision;
    /**
     * 球镜
     */
    private boolean leftSph;
    /**
     * 矫正视力误差
     */
    private boolean rightCorrectedVision;

    /**
     * 裸眼视力
     */
    private boolean rightNakedVision;
    /**
     * 球镜
     */
    private boolean rightSph;
    /**
     * 身高
     */
    private boolean height;
    /**
     * 体重
     */
    private boolean weight;

    /**
     * 存在检查的次数 视力检查
     *
     * @param computerOptometry
     * @return
     */
    public int existCheckComputerOptometry(ComputerOptometryDO.ComputerOptometry computerOptometry) {
        int count = 0;
        if (Objects.isNull(computerOptometry)) {
            return count;
        }
        if (Objects.nonNull(computerOptometry.getSph())) {
            count += 1;
        }
        return count;
    }

    /**
     * 存在检查的次数 电脑验光
     *
     * @param visionData
     * @return
     */
    public int existCheckVision(VisionDataDO.VisionData visionData) {
        int count = 0;
        if (Objects.isNull(visionData)) {
            return count;
        }
        if (Objects.nonNull(visionData.getCorrectedVision())) {
            count += 1;
        }
        if (Objects.nonNull(visionData.getNakedVision())) {
            count += 1;
        }
        return count;
    }

    /**
     * 比较差异 视力检查
     *
     * @return
     */
    public int checkVision(VisionDataDO.VisionData first, VisionDataDO.VisionData second, RetestStudentVO retestStudentVO) {
        int count = 0;
        if (Objects.nonNull(first.getCorrectedVision()) && Objects.nonNull(second.getCorrectedVision())) {
            if (first.getCorrectedVision().subtract(second.getCorrectedVision()).abs().compareTo(BigDecimal.valueOf(0.1)) >= 0) {
                if (first.getLateriality() == 0) {
                    retestStudentVO.setLeftCorrectedVision(true);
                } else {
                    retestStudentVO.setRightCorrectedVision(true);
                }
                count += 1;
            }
        }
        if (Objects.nonNull(first.getNakedVision()) && Objects.nonNull(second.getNakedVision())) {
            if (first.getNakedVision().subtract(second.getNakedVision()).abs().compareTo(BigDecimal.valueOf(0.1)) >= 0) {
                if (first.getLateriality() == 0) {
                    retestStudentVO.setLeftNakedVision(true);
                } else {
                    retestStudentVO.setRightNakedVision(true);
                }
                count += 1;
            }
        }
        return count;
    }

    /**
     * 比较差异 电脑验光
     *
     * @return
     */
    public int checkComputerOptometry(ComputerOptometryDO.ComputerOptometry first, ComputerOptometryDO.ComputerOptometry second, RetestStudentVO retestStudentVO) {
        int count = 0;
        if (Objects.nonNull(first.getSph()) && Objects.nonNull(second.getSph())) {
            if (first.getSph().subtract(second.getSph()).abs().compareTo(BigDecimal.valueOf(0.5)) >= 0) {
                if (first.getLateriality() == 0) {
                    retestStudentVO.setLeftSph(true);
                } else {
                    retestStudentVO.setRightSph(true);
                }
                count += 1;
            }
        }
        return count;
    }

    /**
     * 存在检查的次数 电脑验光
     *
     * @param visionData
     * @return
     */
    public int existCheckHeightAndWeight(HeightAndWeightDataDO visionData) {
        int count = 0;
        if (Objects.isNull(visionData)) {
            return count;
        }
        if (Objects.nonNull(visionData.getWeight())) {
            count += 1;
        }
        if (Objects.nonNull(visionData.getHeight())) {
            count += 1;
        }
        return count;
    }

    /**
     * 比较差异 身高体重
     *
     * @return
     */
    public int checkHeightAndWeight(HeightAndWeightDataDO first, HeightAndWeightDataDO second, RetestStudentVO retestStudentVO) {
        int count = 0;
        if (Objects.nonNull(first) && Objects.nonNull(second) && Objects.nonNull(first.getHeight()) && Objects.nonNull(second.getHeight())) {
            if (first.getHeight().subtract(second.getHeight()).abs().compareTo(BigDecimal.valueOf(0.5)) >= 0) {
                retestStudentVO.setHeight(true);
                count += 1;
            }
        }
        if (Objects.nonNull(first) && Objects.nonNull(second) && Objects.nonNull(first.getWeight()) && Objects.nonNull(second.getWeight())) {
            if (first.getWeight().subtract(second.getWeight()).abs().compareTo(BigDecimal.valueOf(0.1)) >= 0) {
                retestStudentVO.setWeight(true);
                count += 1;
            }
        }
        return count;
    }
}
