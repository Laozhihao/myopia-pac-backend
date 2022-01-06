package com.wupol.myopia.business.aggregation.export.excel.imports;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelAnalysisException;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.IOUtils;
import com.wupol.myopia.business.aggregation.export.excel.ExcelStudentService;
import com.wupol.myopia.business.aggregation.export.excel.constant.ImportExcelEnum;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
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
        String fileName = IOUtils.getTempPath() + multipartFile.getName() + "_" + System.currentTimeMillis() + CommonConst.FILE_SUFFIX;
        File file = new File(fileName);
        FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), file);
        // 这里 也可以不指定class，返回一个list，然后读取第一个sheet 同步读取会自动finish
        List<Map<Integer, String>> listMap;
        try {
            listMap = EasyExcel.read(fileName).sheet().doReadSync();
        } catch (ExcelAnalysisException excelAnalysisException) {
            log.error("导入筛查学生数据异常", excelAnalysisException);
            throw new BusinessException("解析文件格式异常");
        } catch (Exception e) {
            log.error("导入筛查学生数据异常", e);
            throw new BusinessException("解析Excel文件异常");
        }
        if (!listMap.isEmpty()) {
            // 去头部
            listMap.remove(0);
        }
        if (CollectionUtils.isEmpty(listMap)) {
            // 无数据，直接返回
            return;
        }
        // 这里是Excel的一个小坑
//        List<Map<Integer, String>> resultList = listMap.stream().filter(s -> s.get(ImportExcelEnum.NAME.getIndex()) != null).collect(Collectors.toList());
        excelStudentService.insertByUpload(userId, listMap, screeningPlan, schoolId);
        screeningPlanService.updateStudentNumbers(userId, screeningPlan.getId(), screeningPlanSchoolStudentService.getCountByScreeningPlanId(screeningPlan.getId()));
    }
}
