package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.model.SchoolClass;
import com.wupol.myopia.business.management.service.SchoolClassService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 班级控制层
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/schoolClass")
public class SchoolClassController {

    @Resource
    private SchoolClassService schoolClassService;

    @PostMapping()
    public Object saveGrade(@RequestBody SchoolClass schoolClass) {
        CurrentUser user = CurrentUserUtil.getLegalCurrentUser();
        if (null == schoolClass.getSchoolId()
                || StringUtils.isBlank(schoolClass.getName())
                || null == schoolClass.getGradeId()
                || null == schoolClass.getSeatCount()) {
            throw new BusinessException("数据异常");
        }
        schoolClass.setCreateUserId(user.getId());
        return schoolClassService.saveClass(schoolClass);
    }

    @DeleteMapping("{id}")
    public Object deletedGrade(@PathVariable("id") Integer id) {
        CurrentUserUtil.getLegalCurrentUser();
        return schoolClassService.deletedClass(id);
    }
}
