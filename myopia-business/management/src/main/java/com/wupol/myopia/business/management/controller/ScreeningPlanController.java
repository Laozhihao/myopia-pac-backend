package com.wupol.myopia.business.management.controller;

import cn.hutool.core.exceptions.ValidateException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.domain.dto.ScreeningPlanDTO;
import com.wupol.myopia.business.management.domain.dto.ScreeningTaskDTO;
import com.wupol.myopia.business.management.domain.model.*;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.ScreeningPlanQuery;
import com.wupol.myopia.business.management.domain.query.ScreeningTaskQuery;
import com.wupol.myopia.business.management.domain.query.StudentQuery;
import com.wupol.myopia.business.management.domain.vo.SchoolGradeVo;
import com.wupol.myopia.business.management.domain.vo.ScreeningPlanSchoolVo;
import com.wupol.myopia.business.management.domain.vo.ScreeningTaskOrgVo;
import com.wupol.myopia.business.management.facade.ExcelFacade;
import com.wupol.myopia.business.management.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * 筛查计划相关接口
 *
 * @author Alix
 * @Date 2021-01-20
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/screeningPlan")
public class ScreeningPlanController {
    @Autowired
    private ScreeningTaskService screeningTaskService;
    @Autowired
    private ScreeningTaskOrgService screeningTaskOrgService;
    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private ScreeningPlanSchoolService screeningPlanSchoolService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private ExcelFacade excelFacade;

    /**
     * 新增
     *
     * @param screeningPlanDTO 新增参数
     * @return Object
     */
    @PostMapping()
    public void createInfo(@RequestBody @Valid ScreeningPlanDTO screeningPlanDTO) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        // 已创建校验
        if (screeningPlanService.checkIsCreated(screeningPlanDTO.getScreeningTaskId(), screeningPlanDTO.getScreeningOrgId())) {
            throw new ValidationException("筛查计划已创建");
        }
        // 校验用户机构
        ScreeningTask screeningTask = screeningTaskService.getById(screeningPlanDTO.getScreeningTaskId());
        ScreeningTaskOrg screeningTaskOrg = screeningTaskOrgService.getOne(screeningPlanDTO.getScreeningTaskId(), screeningPlanDTO.getScreeningOrgId());
        if (Objects.isNull(screeningTaskOrg)) {
            throw new ValidationException("筛查机构错误");
        }
        if (user.isScreeningUser()) {
            // 筛查机构人员，需校验是否同机构
            Assert.isTrue(user.getOrgId().equals(screeningPlanDTO.getGovDeptId()), "无该筛查机构权限");
        }
        if (user.isPlatformAdminUser() || user.isScreeningUser()) {
            screeningPlanDTO.setDistrictId(screeningTask.getDistrictId()).setGovDeptId(screeningTask.getGovDeptId());
        }
        if (user.isGovDeptUser()) {
            // 政府部门，无法新增计划
            throw new ValidationException("无权限");
        }
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
     * @param query 查询参数
     * @param page  分页数据
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
     * 获取计划学校的年级情况
     *
     * @param screeningPlanId 计划ID
     * @param schoolId        学校ID
     * @return Object
     */
    @GetMapping("grades/{screeningPlanId}/{schoolId}")
    public List<SchoolGradeVo> queryGradesInfo(@PathVariable Integer screeningPlanId, @PathVariable Integer schoolId) {
        return screeningPlanSchoolStudentService.getSchoolGradeVoByPlanIdAndSchoolId(screeningPlanId, schoolId);
    }

    /**
     * 新增筛查学校
     *
     * @param screeningPlanSchool 新增参数
     * @return Object
     */
    @PostMapping("schools")
    public void addSchoolsInfo(@RequestBody @Valid ScreeningPlanSchool screeningPlanSchool) {
        // 任务状态判断：已发布才能新增
        validateExistWithReleaseStatus(screeningPlanSchool.getScreeningPlanId(), CommonConst.STATUS_NOT_RELEASE);
        // 是否已存在
        ScreeningPlanSchool planSchool = screeningPlanSchoolService.getOne(screeningPlanSchool.getScreeningPlanId(), screeningPlanSchool.getSchoolId());
        if (Objects.nonNull(planSchool)) {
            screeningPlanSchool.setId(planSchool.getId());
        }
        if (!screeningPlanSchoolService.saveOrUpdate(screeningPlanSchool)) {
            throw new BusinessException("新增失败");
        }
    }

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
        screeningPlanService.removeWithSchools(CurrentUserUtil.getCurrentUser(), id);
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
        if (CollectionUtils.isEmpty(screeningPlanSchoolService.getSchoolListsByPlanId(id))) {
            throw new ValidationException("无筛查的学校");
        }
        screeningPlanService.release(id, CurrentUserUtil.getCurrentUser());
    }

    /**
     * 分页查询筛查学生信息
     *
     * @param query 查询参数
     * @param page  分页数据
     * @return Object
     */
    @GetMapping("students/page")
    public IPage queryStudentInfos(PageRequest page, StudentQuery query) {
        CurrentUser user = CurrentUserUtil.getCurrentUser();
        if (user.isGovDeptUser()) {
            throw new ValidationException("无查看权限");
        }
        return screeningPlanSchoolStudentService.getPage(query, page);
    }

    /**
     * 导入筛查计划的学生数据
     * @param file
     * @param screeningPlanId
     * @param schoolId
     * @throws IOException
     */
    @PostMapping("/import/{screeningPlanId}/{schoolId}")
    public void importOrganizationStaff(MultipartFile file, @PathVariable Integer screeningPlanId, @PathVariable Integer schoolId) throws IOException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        //1. 校验计划学校是否已存在
        ScreeningPlanSchool planSchool = screeningPlanSchoolService.getOne(screeningPlanId, schoolId);
        if (Objects.isNull(planSchool)) {
            throw new ValidationException("该筛查学校不存在");
        }
        //2. 与肖肖已确认：发布成功后才能导入
        validateExistWithReleaseStatus(screeningPlanId, CommonConst.STATUS_RELEASE);
        excelFacade.importScreeningSchoolStudents(currentUser.getId(), file, screeningPlanId, schoolId);
    }
}
