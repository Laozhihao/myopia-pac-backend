package com.wupol.myopia.business.api.parent.controller;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.parent.service.ParentStudentBizService;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.domain.dto.StudentDTO;
import com.wupol.myopia.business.core.school.domain.model.Student;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 0到6岁
 *
 * @author Simple4H
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/parent/preschool")
public class ParentPreschoolController {

    @Resource
    private ParentStudentBizService parentStudentBizService;

    @Resource
    private DistrictService districtService;

    /**
     * 通过身份证获取学生
     *
     * @param idCard 身份证
     * @return 学生
     */
    @GetMapping("getByIdCard")
    public StudentDTO getByIdCard(String idCard) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return parentStudentBizService.getByIdCard(idCard,currentUser.getId());
    }

    /**
     * 更新学生信息
     *
     * @param student 学生实体
     * @return 学生信息
     */
    @PutMapping("")
    public StudentDTO updateParentStudent(@RequestBody Student student) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return parentStudentBizService.updateStudent(currentUser, student);
    }

    /**
     * 新增孩子（没有绑定则绑定）
     *
     * @param student 学生信息
     * @return 更新个数
     */
    @PostMapping("")
    public Integer saveParentStudent(@RequestBody Student student) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return parentStudentBizService.saveRecordStudent(student, currentUser);
    }

    /**
     * 根据指定code，获取其下级行政区域集
     *
     * @param code code
     * @return List<District>
     */
    @GetMapping("child/district/{code}")
    public List<District> getChildDistrict(@PathVariable("code") @NotNull(message = "行政区域编号不能为空") Long code) {
        return districtService.getChildDistrictByParentIdPriorityCache(code);
    }


    /**
     * 根据指定code，获取其下级行政区域集
     *
     * @param code code
     * @return List<District>
     */
    @GetMapping("child/districtA/{code}")
    public List<District> getChildDistricta(@PathVariable("code") @NotNull(message = "行政区域编号不能为空") Long code) {
        return districtService.getDistrictPositionDetail(code);
    }


}
