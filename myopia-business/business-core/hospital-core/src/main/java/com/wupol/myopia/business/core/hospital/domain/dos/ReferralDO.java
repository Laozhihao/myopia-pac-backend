package com.wupol.myopia.business.core.hospital.domain.dos;

import com.wupol.myopia.business.core.hospital.domain.model.ReferralRecord;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @Author wulizhou
 * @Date 2022/1/12 14:15
 */
@Data
@Accessors(chain = true)
public class ReferralDO extends ReferralRecord {

    /**
     * 申请医院名称
     */
    private String fromHospital;

    /**
     * 申请医师名
     */
    private String fromDoctor;

    /**
     * 申请时间
     */
    private Date applyTime;

}
