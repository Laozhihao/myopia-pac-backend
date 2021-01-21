package com.wupol.myopia.business.screening.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.service.SchoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 筛查端App接口
 *
 * @Author Chikong
 * @Date 2021-01-21
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/app")
public class ScreeningAppController {

    @Autowired
    private SchoolService schoolService;

    /**
     * 模糊查询所有学校名称
     * @param schoolName 模糊查询
     * @param deptId    机构id
     * @return
     */
    @GetMapping("/school/findAllLikeSchoolName")
    public List<String> getAllSchoolByNameLike(@RequestParam String schoolName, @RequestParam Integer deptId) {
        return schoolService.getBySchoolName(schoolName, deptId);
    }



}
