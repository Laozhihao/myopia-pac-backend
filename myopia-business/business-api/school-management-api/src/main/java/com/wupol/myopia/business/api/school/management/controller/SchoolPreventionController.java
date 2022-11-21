package com.wupol.myopia.business.api.school.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.export.ExportStrategy;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExportExcelServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.api.school.management.domain.dto.EyeHealthResponseDTO;
import com.wupol.myopia.business.api.school.management.service.DataSubmitBizService;
import com.wupol.myopia.business.api.school.management.service.SchoolStudentBizService;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.common.utils.util.FileUtils;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.school.domain.dto.SchoolGradeItemsDTO;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentRequestDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.DataSubmit;
import com.wupol.myopia.business.core.screening.flow.service.DataSubmitService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    @Resource
    private DataSubmitBizService dataSubmitBizService;

    @Resource
    private DataSubmitService dataSubmitService;

    @Resource
    private ResourceFileService resourceFileService;


    /**
     * 获取眼健康列表
     *
     * @param pageRequest 分页请求
     * @param requestDTO  请求参数
     *
     * @return IPage<EyeHealthResponseDTO>
     */
    @GetMapping("/eyeHealth/list")
    public IPage<EyeHealthResponseDTO> eyeHealthList(PageRequest pageRequest, SchoolStudentRequestDTO requestDTO) {
        requestDTO.setIsEyeHealth(Boolean.TRUE);
        return schoolStudentBizService.getEyeHealthList(CurrentUserUtil.getCurrentUser().getOrgId(), pageRequest, requestDTO);
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

    /**
     * 获取有筛查数据的班级年级
     *
     * @return List<SchoolGradeItemsDTO>
     */
    @GetMapping("/getAllGradeList")
    public List<SchoolGradeItemsDTO> getAllGradeList() {
        return schoolStudentBizService.getAllGradeList(CurrentUserUtil.getCurrentUser().getOrgId());
    }

    /**
     * 数据上报列表
     *
     * @param pageRequest 分页
     *
     * @return IPage<DataSubmit>
     */
    @GetMapping("/data/submit/list")
    public IPage<DataSubmit> dataSubmitList(PageRequest pageRequest) {
        return dataSubmitService.getList(pageRequest, CurrentUserUtil.getCurrentUser().getOrgId());
    }

    /**
     * 数据上报
     *
     * @param file 文件
     */
    @PostMapping("data/submit")
    public void dataSubmit(MultipartFile file) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        List<Map<Integer, String>> listMap = FileUtils.readExcel(file);
        Integer dataSubmitId = dataSubmitService.createNewDataSubmit(currentUser.getOrgId());
        dataSubmitBizService.dataSubmit(listMap, dataSubmitId, currentUser.getId());
    }

    /**
     * 获取文件
     *
     * @param id id
     *
     * @return ApiResult<String>
     */
    @GetMapping("data/submit/file/{id}")
    public ApiResult<String> abc(@PathVariable("id") Integer id) {
        return ApiResult.success(resourceFileService.getResourcePath(id));
    }
}
