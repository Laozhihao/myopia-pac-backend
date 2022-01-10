package com.wupol.myopia.business.api.preschool.app.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.hospital.domain.dto.PreschoolCheckRecordDTO;
import com.wupol.myopia.business.core.hospital.domain.query.PreschoolCheckRecordQuery;
import com.wupol.myopia.business.core.hospital.service.PreschoolCheckRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @Author wulizhou
 * @Date 2022/1/7 16:10
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/preschool/app/check")
public class CheckRecordController {

    @Autowired
    private PreschoolCheckRecordService preschoolCheckRecordService;

    /**
     * 眼保健列表
     *
     * @param pageRequest 分页请求
     * @return 眼保健列表
     */
    @GetMapping("/listByToday")
    public IPage<PreschoolCheckRecordDTO> getList(PageRequest pageRequest) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        PreschoolCheckRecordQuery query = new PreschoolCheckRecordQuery();
        // 限定医院，时间
        query.setHospitalId(user.getOrgId());
        query.setCheckDateStart(new Date());
        query.setCheckDateEnd(new Date());
        return preschoolCheckRecordService.getList(pageRequest, query);
    }

}
