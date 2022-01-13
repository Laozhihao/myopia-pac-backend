package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.domain.ResultCode;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.management.domain.dto.StudentPreschoolCheckRecordDTO;
import com.wupol.myopia.business.api.management.service.PreschoolCheckRecordBizService;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.hospital.domain.dto.PreschoolCheckRecordDTO;
import com.wupol.myopia.business.core.hospital.domain.query.PreschoolCheckRecordQuery;
import com.wupol.myopia.business.core.hospital.service.PreschoolCheckRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 眼保健检查记录
 * @Author wulizhou
 * @Date 2022/1/4 20:20
 */
@Validated
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/preschool/check")
@Slf4j
public class PreschoolCheckRecordController {

    @Autowired
    private PreschoolCheckRecordService preschoolCheckRecordService;

    @Autowired
    private PreschoolCheckRecordBizService preschoolCheckRecordBizService;

    /**
     * 眼保健列表
     *
     * @param pageRequest 分页请求
     * @param query       分页条件
     * @return 眼保健列表
     */
    @GetMapping("/list")
    public IPage<PreschoolCheckRecordDTO> getList(PageRequest pageRequest, PreschoolCheckRecordQuery query) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (user.isHospitalUser()) {
            query.setHospitalId(user.getOrgId());
        }
        return preschoolCheckRecordService.getList(pageRequest, query);
    }

    /**
     * 获取眼保健纪录详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public PreschoolCheckRecordDTO getDetail(@PathVariable("id") Integer id) {
        PreschoolCheckRecordDTO details = preschoolCheckRecordService.getDetail(id);
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (user.isHospitalUser() && !user.getOrgId().equals(details.getHospitalId())) {
            throw new BusinessException("非法请求", ResultCode.USER_ACCESS_UNAUTHORIZED.getCode());
        }
        return details;
    }

    /**
     * 获取学生检查总体信息
     * @param studentId
     * @return
     */
    @GetMapping("/student/totality")
    public StudentPreschoolCheckRecordDTO getStudentTotalityInfo(Integer hospitalId, @RequestParam Integer studentId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (user.isHospitalUser()) {
            hospitalId = user.getOrgId();
        }
        return preschoolCheckRecordBizService.getInit(hospitalId, studentId);
    }

}
