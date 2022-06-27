package com.wupol.myopia.business.core.screening.flow.domain.vo;

import com.wupol.myopia.business.core.screening.flow.domain.dos.DeviationDO;
import lombok.Data;

import java.util.Date;

/**
 * @Author 钓猫的小鱼
 * @Date 2022/4/17 17:39
 * @Email: shuailong.wu@vistel.cn
 * @Des: 视力扩展类
 */
@Data
public class VisionVO {
    /**
     * 佩戴眼镜的类型： {@link com.wupol.myopia.business.common.utils.constant.WearingGlassesSituation}
     */
    private Integer glassesType;
    /**
     * 视力检查结果
     */
    private VisionResultVO visionResult;
    /**
     * 自动电脑验光检查结果
     */
    private ComputerOptometryResultVO computerOptometryResult;
    /**
     * 视力或屈光检查误差误差
     */
    private DeviationDO.VisionOrOptometryDeviation visionOrOptometryDeviation;
    /**
     * 质控人员
     */
    private String qualityControlName;
    /**
     * 更新时间
     */
    private Date updateTime;

}
