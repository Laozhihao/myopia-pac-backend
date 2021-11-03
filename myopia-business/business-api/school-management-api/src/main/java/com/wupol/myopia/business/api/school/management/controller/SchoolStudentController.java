package com.wupol.myopia.business.api.school.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.api.school.management.service.SchoolStudentBizService;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentListResponseDTO;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentRequestDTO;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
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
    private SchoolStudentBizService schoolStudentBizService;

    @PostMapping
    public Boolean save(@RequestBody SchoolStudent student) {
        return null;
    }

    /**
     * 获取学生列表
     *
     * @param pageRequest 分页请求
     * @param requestDTO  请求入参
     * @return IPage<SchoolStudentListResponseDTO>
     */
    @GetMapping
    public IPage<SchoolStudentListResponseDTO> getList(PageRequest pageRequest, SchoolStudentRequestDTO requestDTO) {
        return schoolStudentBizService.getList(pageRequest, requestDTO);
    }

}
