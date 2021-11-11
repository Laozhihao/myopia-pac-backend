package com.wupol.myopia.business.api.school.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.student.service.StudentFacade;
import com.wupol.myopia.business.api.school.management.service.SchoolStudentBizService;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentListResponseDTO;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentRequestDTO;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StudentScreeningResultResponseDTO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 学校端学生
 *
 * @author Simple4H
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/school/student")
public class SchoolStudentController {

    @Resource
    private SchoolStudentBizService schoolStudentBizService;

    @Resource
    private StudentFacade studentFacade;

    @Resource
    private SchoolStudentService schoolStudentService;

    @Resource
    private StudentService studentService;


    /**
     * 获取学生列表
     *
     * @param pageRequest 分页请求
     * @param requestDTO  请求入参
     * @return IPage<SchoolStudentListResponseDTO>
     */
    @GetMapping
    public IPage<SchoolStudentListResponseDTO> getList(PageRequest pageRequest, SchoolStudentRequestDTO requestDTO) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        Integer schoolId = currentUser.getOrgId();
        return schoolStudentBizService.getList(pageRequest, requestDTO, schoolId);
    }

    /**
     * 新增或更新学生
     *
     * @param student 学生
     * @return SchoolStudent
     */
    @PostMapping
    public SchoolStudent save(@RequestBody SchoolStudent student) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        Integer schoolId = currentUser.getOrgId();
        student.setCreateUserId(currentUser.getId());
        return schoolStudentBizService.saveStudent(student, schoolId);
    }

    /**
     * 获取筛查记录
     *
     * @param studentId 学生Id
     * @return StudentScreeningResultResponseDTO
     */
    @GetMapping("screening/list/{studentId}")
    public StudentScreeningResultResponseDTO screeningList(@PathVariable("studentId") Integer studentId) {
        return studentFacade.getScreeningList(studentId);
    }


    /**
     * 获取学生
     *
     * @param id 学生Id
     * @return SchoolStudent
     */
    @GetMapping("{id}")
    public SchoolStudent getStudent(@PathVariable("id") Integer id) {
        return schoolStudentService.getById(id);
    }

    /**
     * 删除学生
     *
     * @param id 学生Id
     * @return 是否成功
     */
    @DeleteMapping("{id}")
    public Boolean deletedStudent(@PathVariable("id") Integer id) {
        schoolStudentService.deletedStudent(id);
        studentService.deletedStudent(schoolStudentService.getById(id).getStudentId());
        return Boolean.TRUE;
    }
}
