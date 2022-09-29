package com.wupol.myopia.business.api.school.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.export.ExportStrategy;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExportExcelServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.api.school.management.domain.dto.EyeHealthResponseDTO;
import com.wupol.myopia.business.api.school.management.service.SchoolStudentBizService;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentRequestDTO;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * 防控中心
 *
 * @author Simple4H
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/school/prevention")
public class SchoolPreventionController {

    @Resource
    private SchoolStudentBizService schoolStudentBizService;

    @Resource
    private ExportStrategy exportStrategy;


    /**
     * 获取眼健康列表
     *
     * @param schoolId    学校Id
     * @param pageRequest 分页请求
     * @param requestDTO  请求参数
     *
     * @return IPage<EyeHealthResponseDTO>
     */
    @GetMapping("eyeHealth/list")
    public IPage<EyeHealthResponseDTO> eyeHealthList(Integer schoolId, PageRequest pageRequest, SchoolStudentRequestDTO requestDTO) {
        return schoolStudentBizService.getEyeHealthList(schoolId, pageRequest, requestDTO);
    }

    /**
     * 导出眼健康中心数据
     */
    @GetMapping("/eyeHealthData/Export")
    public void eyeHealthDataExport() throws IOException {

        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        ExportCondition exportCondition = new ExportCondition()
                .setApplyExportFileUserId(currentUser.getId())
                .setSchoolId(currentUser.getOrgId());
        exportStrategy.doExport(exportCondition, ExportExcelServiceNameConstant.EXPORT_SCHOOL_EYE_HEALTH_SERVICE);
    }
}
