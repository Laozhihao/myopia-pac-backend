package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.domain.model.GovDept;
import com.wupol.myopia.business.management.service.GovDeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.ValidationException;
import java.io.IOException;
import java.util.Objects;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/govDept")
public class GovDeptController {

    @Autowired
    private GovDeptService govDeptService;

    /**
     * 获取部门列表
     *
     * @param queryParam 查询参数
     * @return java.lang.Object
     **/
    @GetMapping("/list")
    public Object getGovDeptList(GovDept queryParam) throws IOException {
        if (Objects.isNull(queryParam.getDistrictId())) {
            throw new ValidationException("行政区ID为空");
        }
        return govDeptService.findByList(queryParam);
    }

    @PostMapping()
    public Object addGovDept(@RequestBody GovDept govDept) {
        return govDeptService.save(govDept);
    }

    @PutMapping()
    public Object modifyGovDept(@RequestBody GovDept govDept) {
        return govDeptService.updateById(govDept);
    }

    @GetMapping("/structure")
    public Object getGovDeptTree() {
        // 获取当前登录用户所在部门的行政区树
        return govDeptService.selectGovDeptTreeByPid(0);
    }

    @GetMapping("/{govDeptId}")
    public Object getGovDeptDetail(@PathVariable("govDeptId") Integer govDeptId) {
        // 获取当前登录用户所在部门的行政区树
        return govDeptService.getById(govDeptId);
    }

}
