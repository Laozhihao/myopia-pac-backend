package com.wupol.myopia.business.api.preschool.app.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.hospital.domain.dto.PreschoolCheckRecordDTO;
import com.wupol.myopia.business.core.hospital.domain.dto.StudentPreschoolCheckRecordDTO;
import com.wupol.myopia.business.core.hospital.domain.model.*;
import com.wupol.myopia.business.core.hospital.domain.query.PreschoolCheckRecordQuery;
import com.wupol.myopia.business.core.hospital.service.PreschoolCheckRecordService;
import com.wupol.myopia.business.core.screening.flow.domain.dos.VisionDataDO;
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
    @GetMapping("/checkRecord/outerEye/{id}")
    public OuterEye getCheckRecordOuterEyeById(@PathVariable("id") Integer id) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        Integer hospitalId = user.getOrgId();
        PreschoolCheckRecord checkRecord = preschoolCheckRecordService.getById(id, hospitalId);
        return Objects.nonNull(checkRecord) ? checkRecord.getOuterEye() : null;
    }

    /**
     * 根据 id 获取检查单
     *
     * @param id
     * @return
     */
    @GetMapping("/checkRecord/visionData/{id}")
    public VisionMedicalRecord getCheckRecordVisionDataById(@PathVariable("id") Integer id) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        Integer hospitalId = user.getOrgId();
        PreschoolCheckRecord checkRecord = preschoolCheckRecordService.getById(id, hospitalId);
        return Objects.nonNull(checkRecord) ? checkRecord.getVisionData() : null;
    }

    /**
     * 根据 id 获取检查单
     *
     * @param id
     * @return
     */
    @GetMapping("/checkRecord/refractionData/{id}")
    public DiopterMedicalRecord.Diopter getCheckRecordRefractionDataById(@PathVariable("id") Integer id) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        Integer hospitalId = user.getOrgId();
        PreschoolCheckRecord checkRecord = preschoolCheckRecordService.getById(id, hospitalId);
        return Objects.nonNull(checkRecord) ? checkRecord.getRefractionData() : null;
    }

    /**
     * 根据 id 获取检查单
     *
     * @param id
     * @return
     */
    @GetMapping("/checkRecord/eyeDiseaseFactor/{id}")
    public EyeDiseaseFactor getCheckRecordEyeDiseaseFactorById(@PathVariable("id") Integer id) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        Integer hospitalId = user.getOrgId();
        PreschoolCheckRecord checkRecord = preschoolCheckRecordService.getById(id, hospitalId);
        return Objects.nonNull(checkRecord) ? checkRecord.getEyeDiseaseFactor() : null;
    }

    /**
     * 根据 id 获取检查单
     *
     * @param id
     * @return
     */
    @GetMapping("/checkRecord/lightReaction/{id}")
    public BaseMedicalResult getCheckRecordLightReactionById(@PathVariable("id") Integer id) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        Integer hospitalId = user.getOrgId();
        PreschoolCheckRecord checkRecord = preschoolCheckRecordService.getById(id, hospitalId);
        return Objects.nonNull(checkRecord) ? checkRecord.getLightReaction() : null;
    }

    /**
     * 根据 id 获取检查单
     *
     * @param id
     * @return
     */
    @GetMapping("/checkRecord/blinkReflex/{id}")
    public BaseMedicalResult getCheckRecordBlinkReflexById(@PathVariable("id") Integer id) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        Integer hospitalId = user.getOrgId();
        PreschoolCheckRecord checkRecord = preschoolCheckRecordService.getById(id, hospitalId);
        return Objects.nonNull(checkRecord) ? checkRecord.getBlinkReflex() : null;
    }

    /**
     * 根据 id 获取检查单
     *
     * @param id
     * @return
     */
    @GetMapping("/checkRecord/redBallTest/{id}")
    public BaseMedicalResult getCheckRecordRedBallTestById(@PathVariable("id") Integer id) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        Integer hospitalId = user.getOrgId();
        PreschoolCheckRecord checkRecord = preschoolCheckRecordService.getById(id, hospitalId);
        return Objects.nonNull(checkRecord) ? checkRecord.getRedBallTest() : null;
    }

    /**
     * 根据 id 获取检查单
     *
     * @param id
     * @return
     */
    @GetMapping("/checkRecord/visualBehaviorObservation/{id}")
    public BaseMedicalResult getCheckRecordVisualBehaviorObservationById(@PathVariable("id") Integer id) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        Integer hospitalId = user.getOrgId();
        PreschoolCheckRecord checkRecord = preschoolCheckRecordService.getById(id, hospitalId);
        return Objects.nonNull(checkRecord) ? checkRecord.getVisualBehaviorObservation() : null;
    }

    /**
     * 根据 id 获取检查单
     *
     * @param id
     * @return
     */
    @GetMapping("/checkRecord/redReflex/{id}")
    public BaseMedicalResult getCheckRecordRedReflexById(@PathVariable("id") Integer id) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        Integer hospitalId = user.getOrgId();
        PreschoolCheckRecord checkRecord = preschoolCheckRecordService.getById(id, hospitalId);
        return Objects.nonNull(checkRecord) ? checkRecord.getRedReflex() : null;
    }

    /**
     * 根据 id 获取检查单
     *
     * @param id
     * @return
     */
    @GetMapping("/checkRecord/ocularInspection/{id}")
    public BaseMedicalResult getCheckRecordOcularInspectionById(@PathVariable("id") Integer id) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        Integer hospitalId = user.getOrgId();
        PreschoolCheckRecord checkRecord = preschoolCheckRecordService.getById(id, hospitalId);
        return Objects.nonNull(checkRecord) ? checkRecord.getOcularInspection() : null;
    }

    /**
     * 根据 id 获取检查单
     *
     * @param id
     * @return
     */
    @GetMapping("/checkRecord/monocularMaskingAversionTest/{id}")
    public BaseMedicalResult getCheckRecordMonocularMaskingAversionTestById(@PathVariable("id") Integer id) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        Integer hospitalId = user.getOrgId();
        PreschoolCheckRecord checkRecord = preschoolCheckRecordService.getById(id, hospitalId);
        return Objects.nonNull(checkRecord) ? checkRecord.getMonocularMaskingAversionTest() : null;
    }

    /**
     * 根据 id 获取检查单
     *
     * @param id
     * @return
     */
    @GetMapping("/checkRecord/guideContent/{id}")
    public String getCheckRecordGuideContentById(@PathVariable("id") Integer id) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        Integer hospitalId = user.getOrgId();
        PreschoolCheckRecord checkRecord = preschoolCheckRecordService.getById(id, hospitalId);
        return Objects.nonNull(checkRecord) ? checkRecord.getGuideContent() : null;
    }

    /**
     * 保存检查单-眼外观
     *
     * @param checkRecord 检查单
     * @return
     */
    @PostMapping("/checkRecord")
    public void saveOuterEyeCheckRecord(@RequestBody PreschoolCheckRecord checkRecord) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        checkRecord.setHospitalId(user.getOrgId());
        preschoolCheckRecordService.saveCheckRecord(checkRecord);
    }

}
