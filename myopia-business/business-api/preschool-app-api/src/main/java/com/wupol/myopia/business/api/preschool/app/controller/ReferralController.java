package com.wupol.myopia.business.api.preschool.app.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.core.hospital.domain.model.ReferralRecord;
import com.wupol.myopia.business.core.hospital.service.ReferralRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author wulizhou
 * @Date 2022/1/9 15:05
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/preschool/app/referral")
public class ReferralController {

    @Autowired
    private ReferralRecordService referralRecordService;

    /**
     * 获取学生所有转诊信息
     * @param studentId
     * @return
     */
    @GetMapping("/list")
    public List<ReferralRecord> getList(Integer studentId) {
        return referralRecordService.getByStudentId(studentId);
    }

}
