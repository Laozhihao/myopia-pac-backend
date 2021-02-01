package com.wupol.myopia.business.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.domain.dto.ScreeningPlanDTO;
import com.wupol.myopia.business.management.domain.dto.ScreeningTaskDTO;
import com.wupol.myopia.business.management.domain.model.ScreeningPlan;
import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.management.domain.model.ScreeningTask;
import com.wupol.myopia.business.management.domain.model.ScreeningTaskOrg;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.ScreeningPlanQuery;
import com.wupol.myopia.business.management.domain.query.ScreeningTaskQuery;
import com.wupol.myopia.business.management.domain.vo.ScreeningPlanSchoolVo;
import com.wupol.myopia.business.management.domain.vo.ScreeningTaskOrgVo;
import com.wupol.myopia.business.management.service.ScreeningPlanSchoolService;
import com.wupol.myopia.business.management.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.management.service.ScreeningPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.List;
import java.util.Objects;

/**
 * 筛查计划相关接口
 * @author Alix
 * @Date 2021-01-20
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/screeningPlan")
public class ScreeningPlanController {
    @Autowired
    protected ScreeningPlanService screeningPlanService;
    @Autowired
    private ScreeningPlanSchoolService screeningPlanSchoolService;

    /**
     * 新增
     *
     * @param screeningPlanDTO 新增参数
     * @return Object
     */
    @PostMapping()
    public void createInfo(@RequestBody @Valid ScreeningPlanDTO screeningPlanDTO) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        // TODO 已创建校验-调整
        if (screeningPlanService.checkIsCreated(screeningPlanDTO.getScreeningTaskId(), screeningPlanDTO.getScreeningOrgId())) {
            throw new ValidationException("筛查计划已创建");
        }
        // TODO 校验部门
        // TODO 看前端是否能拿到用户的层级与部门再做处理
//        if (user.isPlatformAdminUser()) {
//            Assert.notNull(screeningTask.getDistrictId());
//            Assert.notNull(screeningTask.getGovDeptId());
//        }
        screeningPlanService.saveOrUpdateWithSchools(user, screeningPlanDTO, true);
    }

    /**
     * 查看筛查计划
     *
     * @param id ID
     * @return Object
     */
    @GetMapping("{id}")
    public Object getInfo(@PathVariable Integer id) {
        return screeningPlanService.getById(id);
    }

    /**
     * 更新筛查计划
     *
     * @param screeningPlanDTO 更新参数
     * @return Object
     */
    @PutMapping()
    public void updateInfo(@RequestBody @Valid ScreeningPlanDTO screeningPlanDTO) {
        validateExistWithReleaseStatus(screeningPlanDTO.getId(), CommonConst.STATUS_RELEASE);
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        //TODO 校验部门
        screeningPlanService.saveOrUpdateWithSchools(user, screeningPlanDTO, false);
    }

    /**
     * 校验筛查任务是否存在且校验发布状态
     *
     * @param id
     */
    private void validateExistWithReleaseStatus(Integer id, Integer releaseStatus) {
        ScreeningPlan screeningPlan = validateExist(id);
        Integer taskStatus = screeningPlan.getReleaseStatus();
        if (releaseStatus.equals(taskStatus)) {
            throw new BusinessException(String.format("该计划%s", CommonConst.STATUS_RELEASE.equals(taskStatus) ? "已发布" : "未发布"));
        }
    }

    /**
     * 校验筛查计划是否存在
     *
     * @param id
     * @return
     */
    private ScreeningPlan validateExist(Integer id) {
        if (Objects.isNull(id)) {
            throw new BusinessException("参数ID不存在");
        }
        ScreeningPlan screeningPlan = screeningPlanService.getById(id);
        if (Objects.isNull(screeningPlan)) {
            throw new BusinessException("查无该计划");
        }
        return screeningPlan;
    }

    /**
     * 分页查询计划列表
     *
     * @param query   查询参数
     * @param page    分页数据
     * @return Object
     */
    @GetMapping("page")
    public IPage queryInfo(PageRequest page, ScreeningPlanQuery query) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (!user.isPlatformAdminUser()) {
            query.setGovDeptId(user.getOrgId());
        }
        return screeningPlanService.getPage(query, page);
    }

    /**
     * 获取计划学校
     *
     * @param screeningPlanId 计划ID
     * @return Object
     */
    @GetMapping("schools/{screeningPlanId}")
    public List<ScreeningPlanSchoolVo> querySchoolsInfo(@PathVariable Integer screeningPlanId) {
        // 任务状态判断
        validateExist(screeningPlanId);
        return screeningPlanSchoolService.getSchoolVoListsByPlanId(screeningPlanId);
    }

    /**
     * 新增筛查学校
     *
     * @param screeningPlanSchool 新增参数
     * @return Object
     */
    @PostMapping("schools")
    public void addSchoolsInfo(@RequestBody @Valid ScreeningPlanSchool screeningPlanSchool) {
        // 任务状态判断
        validateExistWithReleaseStatus(screeningPlanSchool.getScreeningPlanId(), CommonConst.STATUS_RELEASE);
        // 是否已存在
        ScreeningPlanSchool planSchool = screeningPlanSchoolService.getOne(screeningPlanSchool.getScreeningPlanId(), screeningPlanSchool.getSchoolId());
        if (Objects.nonNull(planSchool)) {
            screeningPlanSchool.setId(planSchool.getId());
        }
        if (!screeningPlanSchoolService.saveOrUpdate(screeningPlanSchool)) {
            throw new BusinessException("新增失败");
        }
    }

//    /**
//     * 筛查机构相同时间段内是否已有已发布的任务
//     *
//     * @param orgId 机构ID
//     * @param screeningTaskQuery 查询参数，必须有govDeptId、startCreateTime、endCreateTime，如果是已有任务，需有id
//     * @return boolean 已有任务true，没有任务false
//     */
//    @PostMapping("orgs/period/{orgId}")
//    public boolean checkOrgHasTaskInPeriod(@PathVariable Integer orgId, @RequestBody ScreeningTaskQuery screeningTaskQuery) {
//        return screeningTaskOrgService.checkHasTaskInPeriod(orgId, screeningTaskQuery);
//    }

    /**
     * 根据ID删除（这里默认所有表的主键字段都为“id”,且自增）
     *
     * @param id ID
     * @return void
     */
    @DeleteMapping("{id}")
    public void deleteInfo(@PathVariable Integer id) {
        // 判断是否已发布
        validateExistWithReleaseStatus(id, CommonConst.STATUS_RELEASE);
        screeningPlanService.removeWithSchools(id);
    }

    /**
     * 发布
     *
     * @param id ID
     * @return void
     */
    @PostMapping("{id}")
    public void release(@PathVariable Integer id) {
        // 已发布，直接返回
        validateExistWithReleaseStatus(id, CommonConst.STATUS_RELEASE);
        //TODO 非筛查机构，直接报错
        // 没有学校，直接报错
        if (CollectionUtils.isEmpty(screeningPlanSchoolService.getSchoolListsByPlanId(id))){
            throw new ValidationException("无筛查的学校");
        }
        screeningPlanService.release(id, CurrentUserUtil.getCurrentUser());
    }
}
