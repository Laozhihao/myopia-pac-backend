package com.wupol.myopia.business.api.preschool.app.controller;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.core.hospital.domain.dos.ReferralDO;
import com.wupol.myopia.business.core.hospital.domain.dto.ReferralDTO;
import com.wupol.myopia.business.core.hospital.domain.model.ReferralRecord;
import com.wupol.myopia.business.core.hospital.service.PreschoolCheckRecordService;
import com.wupol.myopia.business.core.hospital.service.ReferralRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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

    @Autowired
    private PreschoolCheckRecordService preschoolCheckRecordService;

    /**
     * 获取学生所有转诊信息
     * @param studentId
     * @return
     */
    @GetMapping("/list")
    public List<ReferralDO> getList(@RequestParam Integer studentId) {
        return referralRecordService.getByStudentId(studentId);
    }

    @GetMapping("/details")
    public ReferralDTO getDetails(@RequestParam Integer preshcoolCheckRecordId) {
        return referralRecordService.getDetailsByHospitalAndPreschoolCheckRecordId(CurrentUserUtil.getCurrentUser().getOrgId(), preshcoolCheckRecordId);
    }

    @PostMapping
    public void saveOrUpdate(@RequestBody @Valid ReferralRecord record) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        preschoolCheckRecordService.checkOrgOperation(user.getOrgId(), record.getPreschoolCheckRecordId(), record.getStudentId());
        referralRecordService.saveOrUpdateReferral(record, user);
    }

}
