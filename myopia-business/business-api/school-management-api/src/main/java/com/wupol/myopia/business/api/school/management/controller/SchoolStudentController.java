package com.wupol.myopia.business.api.school.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.aggregation.student.service.StudentFacade;
import com.wupol.myopia.business.api.school.management.service.SchoolStudentBizService;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentListResponseDTO;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentRequestDTO;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StudentScreeningResultResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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

    @Autowired
    private SchoolStudentBizService schoolStudentBizService;

    @Autowired
    private StudentFacade studentFacade;


    /**
     * 获取学生列表
     *
     * @param pageRequest 分页请求
     * @param requestDTO  请求入参
     * @return IPage<SchoolStudentListResponseDTO>
     */
    @GetMapping
    public IPage<SchoolStudentListResponseDTO> getList(PageRequest pageRequest,@Valid SchoolStudentRequestDTO requestDTO) {
        return schoolStudentBizService.getList(pageRequest, requestDTO);
    }

    /**
     * 新增或更新学生
     *
     * @param student 学生
     * @return SchoolStudent
     */
    @PostMapping
    public SchoolStudent save(@RequestBody SchoolStudent student) {
        return schoolStudentBizService.saveStudent(student);
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


}
