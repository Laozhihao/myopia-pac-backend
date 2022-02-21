package com.wupol.myopia.business.aggregation.export.excel.imports;

import com.wupol.myopia.business.aggregation.export.excel.ExcelStudentService;
import com.wupol.myopia.business.aggregation.export.excel.constant.ImportExcelEnum;
import com.wupol.myopia.business.common.utils.util.FileUtils;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 筛查学生
 *
 * @author Simple4H
 */
@Service
@Log4j2
public class PlanStudentExcelImportService {

    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    @Resource
    private ScreeningPlanService screeningPlanService;

    @Resource
    private ExcelStudentService excelStudentService;

    /**
     * 导入筛查学生信息
     *
     * @param userId        学生信息
     * @param multipartFile 文件
     * @param schoolId      学校Id
     * @throws IOException IO异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void importScreeningSchoolStudents(Integer userId, MultipartFile multipartFile, ScreeningPlan screeningPlan, Integer schoolId) throws IOException {

        List<Map<Integer, String>> listMap = FileUtils.readExcel(multipartFile);
        if (CollectionUtils.isEmpty(listMap)) {
            // 无数据，直接返回
            return;
        }
        excelStudentService.insertByUpload(userId, listMap, screeningPlan, schoolId);
        screeningPlanService.updateStudentNumbers(userId, screeningPlan.getId(), screeningPlanSchoolStudentService.getCountByScreeningPlanId(screeningPlan.getId()));
    }
}
