package com.wupol.myopia.business.api.management.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.api.management.domain.dto.ScreeningTaskOrgInfoDTO;
import com.wupol.myopia.business.api.management.domain.vo.ScreeningNoticeVO;
import com.wupol.myopia.business.api.management.domain.vo.ScreeningTaskAndDistrictVO;
import com.wupol.myopia.business.api.management.service.ScreeningNoticeBizService;
import com.wupol.myopia.business.api.management.service.ScreeningNoticeDeptOrgBizService;
import com.wupol.myopia.business.api.management.service.ScreeningTaskBizService;
import com.wupol.myopia.business.api.management.service.ScreeningTaskOrgBizService;
import com.wupol.myopia.business.common.utils.constant.BizMsgConstant;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningTaskDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningTaskOrgDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningTaskPageDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningTaskQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNoticeDeptOrg;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTask;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTaskOrg;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeDeptOrgService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningTaskOrgService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Alix
 * date 2021-01-20
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/screeningTask")
public class ScreeningTaskController {

    @Autowired
    protected ScreeningTaskService screeningTaskService;
    @Autowired
    private ScreeningTaskOrgService screeningTaskOrgService;
    @Autowired
    private GovDeptService govDeptService;
    @Autowired
    private ScreeningTaskBizService screeningTaskBizService;
    @Autowired
    private ScreeningTaskOrgBizService screeningTaskOrgBizService;
    @Autowired
    protected ScreeningNoticeService screeningNoticeService;
    @Autowired
    private ScreeningNoticeDeptOrgService screeningNoticeDeptOrgService;
    @Autowired
    private ScreeningNoticeBizService screeningNoticeBizService;

    /**
     * 新增
     *
     * @param screeningTaskDTO 新增参数
     */
    @PostMapping()
    public void createInfo(@RequestBody @Valid ScreeningTaskDTO screeningTaskDTO) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        //校验部门
        if (user.isPlatformAdminUser()) {
            Assert.notNull(screeningTaskDTO.getDistrictId(), "请选择行政区域");
            Assert.notNull(screeningTaskDTO.getGovDeptId(), "请选择所处部门");
        }
        if (user.isScreeningUser() || user.isHospitalUser()) {
            throw new ValidationException("无权限");
        }
        if (CollectionUtils.isEmpty(screeningTaskDTO.getScreeningOrgs()) || screeningTaskDTO.getScreeningOrgs().stream().map(ScreeningTaskOrg::getScreeningOrgId).distinct().count() != screeningTaskDTO.getScreeningOrgs().size()) {
            throw new ValidationException("无筛查机构或筛查机构重复");
        }
        if (user.isGovDeptUser()) {
            // 政府部门，设置为用户自身所在的部门层级
            GovDept govDept = govDeptService.getById(user.getOrgId());
            screeningTaskDTO.setDistrictId(govDept.getDistrictId()).setGovDeptId(user.getOrgId());
        }
        // 开始时间只能在今天或以后
        if (DateUtil.isDateBeforeToday(screeningTaskDTO.getStartTime())) {
            throw new ValidationException(BizMsgConstant.VALIDATION_START_TIME_ERROR);
        }
        // 已创建校验
        if (screeningTaskService.checkIsCreated(screeningTaskDTO.getScreeningNoticeId(), screeningTaskDTO.getGovDeptId())) {
            throw new ValidationException("该部门任务已创建");
        }
        screeningTaskDTO.setCreateUserId(user.getId());
        screeningTaskBizService.saveOrUpdateWithScreeningOrgs(user, screeningTaskDTO, true);
    }

    /**
     * 查看筛查任务
     *
     * @param id 筛查通知ID
     * @return Object
     */
    @GetMapping("{id}")
    public ScreeningTaskAndDistrictVO getInfo(@PathVariable Integer id) {
        return screeningTaskBizService.getScreeningTaskAndDistrictById(id);
    }

    /**
     * 更新筛查通知
     *
     * @param screeningTaskDTO 更新参数
     */
    @PutMapping()
    public void updateInfo(@RequestBody @Valid ScreeningTaskDTO screeningTaskDTO) {
        validateExistAndAuthorize(screeningTaskDTO.getId());
        // 开始时间只能在今天或以后
        if (DateUtil.isDateBeforeToday(screeningTaskDTO.getStartTime())) {
            throw new ValidationException(BizMsgConstant.VALIDATION_START_TIME_ERROR);
        }
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (CollectionUtils.isEmpty(screeningTaskDTO.getScreeningOrgs()) || getOrgCount(screeningTaskDTO.getScreeningOrgs()) != screeningTaskDTO.getScreeningOrgs().size()) {
            throw new ValidationException("无筛查机构或筛查机构重复");
        }
        screeningTaskBizService.saveOrUpdateWithScreeningOrgs(user, screeningTaskDTO, false);
    }

    /**
     * 获取筛查机构数量
     * @param screeningOrgList
     */
    private Integer getOrgCount(List<ScreeningTaskOrg> screeningOrgList){
        if (CollUtil.isEmpty(screeningOrgList)){
            return 0;
        }
        return screeningOrgList.stream()
                .map(screeningTaskOrg -> screeningTaskOrg.getScreeningOrgType()+ StrUtil.UNDERLINE+screeningTaskOrg.getScreeningOrgId())
                .collect(Collectors.toSet())
                .size();
    }

    /**
     * 校验任务是否存在与发布状态
     * 同时校验权限
     *
     * @param screeningTaskId 筛查通知ID
     * @return
     * 筛查通知
     */
    private ScreeningTask validateExistAndAuthorize(Integer screeningTaskId) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        // 校验用户机构
        if (user.isScreeningUser() || user.isHospitalUser()) {
            // 筛查机构，无权限处理
            throw new ValidationException("无权限");
        }
        ScreeningTask screeningTask = validateExistWithReleaseStatus(screeningTaskId, CommonConst.STATUS_RELEASE);
        if (user.isGovDeptUser()) {
            // 政府部门人员，需校验是否同部门
            Assert.isTrue(user.getOrgId().equals(screeningTask.getGovDeptId()), "无该部门权限");
        }
        return screeningTask;
    }

    /**
     * 校验筛查任务是否存在且校验发布状态
     *
     * @param id 筛查通知id
     * @return 筛查通知
     */
    private ScreeningTask validateExistWithReleaseStatus(Integer id, Integer releaseStatus) {
        ScreeningTask screeningTask = validateExist(id);
        Integer taskStatus = screeningTask.getReleaseStatus();
        if (releaseStatus.equals(taskStatus)) {
            throw new BusinessException(String.format("该任务%s", CommonConst.STATUS_RELEASE.equals(taskStatus) ? "已发布" : "未发布"));
        }
        return screeningTask;
    }

    /**
     * 校验筛查通知是否存在
     *
     * @param id 筛查通知ID
     * @return 筛查通知
     */
    private ScreeningTask validateExist(Integer id) {
        if (Objects.isNull(id)) {
            throw new BusinessException("参数ID不存在");
        }
        ScreeningTask screeningTask = screeningTaskService.getById(id);
        if (Objects.isNull(screeningTask)) {
            throw new BusinessException("查无该任务");
        }
        return screeningTask;
    }

    /**
     * 分页查询任务列表
     *
     * @param query   查询参数
     * @param page    分页数据
     * @return Object
     */
    @GetMapping("page")
    public IPage<ScreeningTaskPageDTO> queryInfo(PageRequest page, ScreeningTaskQueryDTO query) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (!user.isPlatformAdminUser()) {
            query.setGovDeptId(user.getOrgId());
        }
        return screeningTaskBizService.getPage(query, page);
    }

    /**
     * 获取任务筛查机构
     *
     * @param screeningTaskId 任务ID
     * @return Object
     */
    @GetMapping("orgs/{screeningTaskId}")
    public List<ScreeningTaskOrgDTO> queryOrgsInfo(@PathVariable Integer screeningTaskId,String orgNameOrSchoolName) {
        // 任务状态判断
        validateExist(screeningTaskId);
        return screeningTaskOrgBizService.getOrgVoListsByTaskId(screeningTaskId,orgNameOrSchoolName);
    }

    /**
     * 获取指定任务下的机构信息
     * @param screeningTaskId
     * @param orgId
     * @return
     */
    @GetMapping("orgs/{screeningTaskId}/{orgId}")
    public ScreeningTaskOrg getTaskOrg(@PathVariable Integer screeningTaskId, @PathVariable Integer orgId , Integer orgType) {
        return screeningTaskOrgService.getOne(screeningTaskId, orgId,orgType);
    }

    /**
     * 获取指定任务下学校筛查详情
     * @param screeningTaskId
     * @return
     */
    @GetMapping("screeningSchoolDetails/{screeningTaskId}")
    public List<ScreeningTaskOrgDTO> screeningSchoolDetails(@PathVariable Integer screeningTaskId) {
        return screeningTaskOrgBizService.getScreeningSchoolDetails(screeningTaskId);
    }

    /**
     * 新增筛查机构
     *
     * @param screeningTaskOrgInfoDTO
     */
    @PostMapping("orgs/{screeningTaskId}")
    public void addOrgsInfo( @RequestBody @Valid ScreeningTaskOrgInfoDTO screeningTaskOrgInfoDTO) {
        if (CollectionUtils.isEmpty(screeningTaskOrgInfoDTO.getScreeningTaskOrgs())) {
            return;
        }
        // 任务状态判断
        validateExistWithReleaseStatus(screeningTaskOrgInfoDTO.getScreeningTaskId(), CommonConst.STATUS_NOT_RELEASE);
        // 新增
        screeningTaskOrgBizService.saveOrUpdateBatchByTaskId(CurrentUserUtil.getCurrentUser(),screeningTaskOrgInfoDTO.getScreeningTaskId(),screeningTaskOrgInfoDTO.getScreeningTaskOrgs(), true);
    }

    /**
     * 获取筛查机构相同时间段内已有已发布的任务（相同起始时间只取第一个）
     *
     * @param orgId 机构ID
     * @param screeningTaskQuery 查询参数，必须有govDeptId、startCreateTime、endCreateTime
     * @return List
     */
    @PostMapping("orgs/period/{orgId}")
    public List<ScreeningTaskOrgDTO> hasTaskOrgVoInPeriod(@PathVariable Integer orgId, @RequestBody ScreeningTaskQueryDTO screeningTaskQuery) {
        List<ScreeningTaskOrgDTO> periodList = new ArrayList<>();
        List<String> existStartTimeEndTimeList = new ArrayList<>();
        List<ScreeningTaskOrgDTO> hasTaskOrgVoInPeriod = screeningTaskOrgService.getHasTaskOrgVoInPeriod(orgId,screeningTaskQuery.getScreeningOrgType(), screeningTaskQuery);
        hasTaskOrgVoInPeriod.forEach(vo -> {
            String startTimeEndTime = String.format("%s--%s", DateFormatUtil.format(vo.getStartTime(), DateFormatUtil.FORMAT_ONLY_DATE), DateFormatUtil.format(vo.getEndTime(), DateFormatUtil.FORMAT_ONLY_DATE));
            if (!existStartTimeEndTimeList.contains(startTimeEndTime)) {
                periodList.add(vo);
                existStartTimeEndTimeList.add(startTimeEndTime);
            }
        });
        return periodList;
    }

    /**
     * 根据ID删除（这里默认所有表的主键字段都为“id”,且自增）
     *
     * @param id 筛查通知ID
     */
    @DeleteMapping("{id}")
    public void deleteInfo(@PathVariable Integer id) {
        // 判断是否已发布
        validateExistAndAuthorize(id);
        screeningTaskService.removeWithOrgs(id, CurrentUserUtil.getCurrentUser());
    }

    /**
     * 发布
     *
     * @param id ID
     */
    @PostMapping("{id}")
    public void release(@PathVariable Integer id) {
        // 已发布，直接返回
        ScreeningTask screeningTask = validateExistAndAuthorize(id);
        // 开始时间只能在今天或以后
        if (DateUtil.isDateBeforeToday(screeningTask.getStartTime())) {
            throw new ValidationException(BizMsgConstant.VALIDATION_START_TIME_ERROR);
        }
        //没有筛查机构，直接报错
        if (CollectionUtils.isEmpty(screeningTaskOrgService.getOrgListsByTaskId(id))){
            throw new ValidationException("无筛查机构");
        }
        screeningTaskBizService.release(id, CurrentUserUtil.getCurrentUser());
    }

    @PostMapping("/d")
    @Transactional(rollbackFor = Exception.class)
    public void d(@RequestBody ScreeningTaskDTO screeningTaskDTO) {

        ScreeningNotice screeningNotice = new ScreeningNotice();

        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (user.isPlatformAdminUser()) {
            Assert.notNull(screeningTaskDTO.getDistrictId(), "请选择行政区域");
            Assert.notNull(screeningTaskDTO.getGovDeptId(), "请选择所处部门");
        }

        if (user.isGovDeptUser()) {
            // 政府部门，设置为用户自身所在的部门层级
            GovDept govDept = govDeptService.getById(user.getOrgId());
            screeningNotice.setDistrictId(govDept.getDistrictId()).setGovDeptId(user.getOrgId());
            screeningTaskDTO.setDistrictId(govDept.getDistrictId()).setGovDeptId(user.getOrgId());
        }

        if (CollectionUtils.isEmpty(screeningTaskDTO.getScreeningOrgs()) || screeningTaskDTO.getScreeningOrgs().stream().map(ScreeningTaskOrg::getScreeningOrgId).distinct().count() != screeningTaskDTO.getScreeningOrgs().size()) {
            throw new ValidationException("无筛查机构或筛查机构重复");
        }

        // 开始时间只能在今天或以后
        if (DateUtil.isDateBeforeToday(screeningTaskDTO.getStartTime())) {
            throw new ValidationException(BizMsgConstant.VALIDATION_START_TIME_ERROR);
        }

        screeningNotice.setTitle(screeningTaskDTO.getTitle());
        screeningNotice.setContent(screeningTaskDTO.getContent());
        screeningNotice.setStartTime(screeningTaskDTO.getStartTime());
        screeningNotice.setEndTime(screeningTaskDTO.getEndTime());
        screeningNotice.setReleaseTime(new Date());
        screeningNotice.setCreateUserId(user.getId());
        screeningNotice.setCreateTime(new Date());
        screeningNotice.setOperatorId(user.getId());
        screeningNotice.setOperateTime(new Date());
        screeningNotice.setScreeningType(screeningTaskDTO.getScreeningType());

        screeningNotice.setCreateUserId(user.getId())
                .setOperatorId(user.getId());
        if (!screeningNoticeService.save(screeningNotice)) {
            throw new BusinessException("创建失败");
        }
        // 常见病版本，“发布筛查通知”和“筛查通知”菜单合并，则创建通知时也给自己发一个通知
        screeningNoticeDeptOrgService.save(new ScreeningNoticeDeptOrg().setScreeningNoticeId(screeningNotice.getId()).setDistrictId(screeningNotice.getDistrictId()).setAcceptOrgId(screeningNotice.getGovDeptId()).setOperatorId(screeningNotice.getCreateUserId()));

        // 已发布，直接返回
        validateExistWithReleaseStatus(screeningNotice.getId(), CommonConst.STATUS_RELEASE);
        ScreeningNotice notice = screeningNoticeService.getById(screeningNotice.getId());
        screeningNoticeService.createOrReleaseValidate(notice);
        if (user.isPlatformAdminUser() || user.isGovDeptUser() && user.getOrgId().equals(notice.getGovDeptId())) {
            screeningNoticeBizService.release(screeningNotice.getId(), user);
        } else {
            throw new ValidationException("无权限");
        }

        // 已创建校验
        if (screeningTaskService.checkIsCreated(screeningNotice.getId(), screeningTaskDTO.getGovDeptId())) {
            throw new ValidationException("该部门任务已创建");
        }
        screeningTaskDTO.setCreateUserId(user.getId());
        screeningTaskBizService.saveOrUpdateWithScreeningOrgs(user, screeningTaskDTO, true);
    }

}
