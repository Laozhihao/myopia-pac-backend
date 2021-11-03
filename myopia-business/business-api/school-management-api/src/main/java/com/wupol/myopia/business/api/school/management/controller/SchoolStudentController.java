package com.wupol.myopia.business.api.school.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentRequestDTO;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    private SchoolStudentService schoolStudentService;

    @PostMapping
    public Boolean save(@RequestBody SchoolStudent student) {
        return schoolStudentService.saveOrUpdate(student);
    }

    @GetMapping
    public IPage<SchoolStudent> getList(PageRequest pageRequest, SchoolStudentRequestDTO requestDTO) {
        return schoolStudentService.getList(pageRequest, requestDTO);
    }

}
