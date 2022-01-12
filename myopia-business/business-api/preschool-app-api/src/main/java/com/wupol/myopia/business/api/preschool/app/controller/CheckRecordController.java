package com.wupol.myopia.business.api.preschool.app.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.hospital.domain.dto.PreschoolCheckRecordDTO;
import com.wupol.myopia.business.core.hospital.domain.dto.StudentPreschoolCheckRecordDTO;
import com.wupol.myopia.business.core.hospital.domain.model.PreschoolCheckRecord;
import com.wupol.myopia.business.core.hospital.domain.query.PreschoolCheckRecordQuery;
import com.wupol.myopia.business.core.hospital.service.PreschoolCheckRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Objects;

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

    /**
     * 获取检查详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public PreschoolCheckRecordDTO getById(@PathVariable("id") Integer id) {
        return preschoolCheckRecordService.getDetails(id);
    }

    /**
     * 获取检查首页信息
     * @param studentId
     * @return
     */
    @GetMapping("/init")
    public StudentPreschoolCheckRecordDTO getInit(Integer studentId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        return preschoolCheckRecordService.getInit(user.getOrgId(), studentId);
    }
    /**
     * 根据 id 获取检查单
     *
     * @param id
     * @return
     */
    @GetMapping("/checkRecord/{id}")
    public PreschoolCheckRecord getCheckRecordById(@PathVariable("id") Integer id) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        Integer hospitalId = user.getOrgId();
        return preschoolCheckRecordService.getById(id, hospitalId);
    }

    /**
     * 根据 id 获取检查前转诊
     *
     * @param id
     * @return
     */
    @GetMapping("/checkRecord/fromReferral/{id}")
    public PreschoolCheckRecord getCheckRecordFromReferralById(@PathVariable("id") Integer id) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        Integer hospitalId = user.getOrgId();
        PreschoolCheckRecord checkRecord = preschoolCheckRecordService.getById(id, hospitalId);
        return Objects.nonNull(checkRecord) ? new PreschoolCheckRecord()
                .setIsReferral(checkRecord.getIsReferral()).setFromReferral(checkRecord.getFromReferral()) : null;
    }

    /**
     * 保存检查单
     *
     * @param checkRecord 检查单
     * @return
     */
    @PostMapping("/checkRecord")
    public void saveCheckRecord(@RequestBody PreschoolCheckRecord checkRecord) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        checkRecord.setHospitalId(user.getOrgId());
        preschoolCheckRecordService.saveCheckRecord(checkRecord);
    }

    /**
     * 保存检查单-检查前转诊信息
     *
     * @param checkRecord 检查单
     * @return
     */
    @PostMapping("/checkRecord/fromReferral")
    public void saveCheckRecordFromReferral(@RequestBody PreschoolCheckRecord checkRecord) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        checkRecord.setHospitalId(user.getOrgId());
        if (Objects.isNull(checkRecord.getFromReferral())) {
            checkRecord.setIsReferral(false);
        } else {
            checkRecord.setIsReferral(true);
        }
        preschoolCheckRecordService.saveCheckRecord(checkRecord);
    }

}
