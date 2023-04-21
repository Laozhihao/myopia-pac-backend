package com.wupol.myopia.business.api.school.management.controller;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学校筛查计划
 *
 * @Author lzh
 * @Date 2023/4/18
 **/
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/school/plan")
public class SchoolPlanController {

    @Autowired
    private DistrictService districtService;
    @Autowired
    private SchoolService schoolService;

    /**
     * 判断是否属于新疆地区的
     *
     * @return boolean
     */
    @GetMapping("/isXinJiangDistrict")
    public boolean isXinJiangDistrict() {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        School school = schoolService.getById(currentUser.getOrgId());
        return districtService.isXinJiangDistrict(school.getDistrictId());
    }
}
