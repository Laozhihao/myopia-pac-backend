package com.wupol.myopia.business.api.management.domain.builder;

import com.wupol.myopia.business.api.management.domain.vo.ScreeningSchoolOrgVO;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * 筛查机构业务
 *
 * @author hang.yuan 2022/9/29 17:01
 */
@UtilityClass
public class ScreeningOrgBizBuilder {

    /**
     * 获取筛查机构（机构/学校）
     * @param haveTaskOrgIds 存在筛查任务的机构ID
     * @param id
     * @param name
     * @param phone
     */
    public ScreeningSchoolOrgVO getScreeningSchoolOrgVO(List<Integer> haveTaskOrgIds,
                                                        Integer id ,String name,String phone) {
        return new ScreeningSchoolOrgVO()
                .setId(id).setName(name).setPhone(phone)
                .setAlreadyHaveTask(haveTaskOrgIds.contains(id));
    }
}
