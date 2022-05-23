package com.wupol.myopia.business.api.management.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import com.alibaba.fastjson.JSONObject;
import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.domain.PdfResponseDTO;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.screening.domain.vos.SchoolGradeVO;
import com.wupol.myopia.business.aggregation.screening.service.ScreeningPlanSchoolStudentFacadeService;
import com.wupol.myopia.business.api.management.domain.dto.ReviewInformExportDataDTO;
import com.wupol.myopia.business.common.utils.constant.ExportTypeConst;
import com.wupol.myopia.business.common.utils.util.FileUtils;
import com.wupol.myopia.business.core.common.service.Html2PdfService;
import com.wupol.myopia.business.core.common.util.S3Utils;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dos.HeightAndWeightDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.system.service.NoticeService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 复查告知书
 *
 * @author Simple4H
 */
@Service
@Log4j2
public class ReviewInformService {

    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    @Resource
    private ScreeningPlanSchoolStudentFacadeService screeningPlanSchoolStudentFacadeService;

    @Resource
    private Html2PdfService html2PdfService;

    @Resource
    private ScreeningPlanService screeningPlanService;

    @Resource
    private VisionScreeningResultService visionScreeningResultService;

    @Resource
    private SchoolService schoolService;

    @Resource
    private SchoolGradeService schoolGradeService;

    @Resource
    private SchoolClassService schoolClassService;

    @Resource
    private NoticeService noticeService;

    @Resource
    private S3Utils s3Utils;

    @Value("${report.html.url-host}")
    public String htmlUrlHost;

    @Value("${file.temp.save-path}")
    public String pdfSavePath;

    private static final String RESCREEN_NAME = "复查结果通知书";

    /**
     * 筛查通知结果页面地址
     */
    public static final String RESCREEN_REVIEW_HTML_URL = "%s?planId=%s&schoolId=%s&gradeId=%s&classId=%s&orgId=%s&parentsInform=1";


    /**
     * 获取学校
     *
     * @param planId     筛查计划
     * @param orgId      机构Id
     * @param schoolName 学校名称
     * @return List<ScreeningPlanSchoolStudent>
     */
    public List<ScreeningPlanSchoolStudent> getReviewSchools(Integer planId, Integer orgId, String schoolName) {
        List<ScreeningPlanSchoolStudent> matchRescreenResults = getMatchRescreenResults(planId, orgId, null, null, null);
        return matchRescreenResults.stream()
                .filter(distinctByKey(ScreeningPlanSchoolStudent::getSchoolName))
                .filter(s -> {
                    if (StringUtils.isNotBlank(schoolName)) {
                        return StringUtils.countMatches(schoolName, s.getSchoolName()) > 0;
                    }
                    return true;
                }).collect(Collectors.toList());
    }

    /**
     * 获取复查年级班级
     *
     * @param planId   计划Id
     * @param orgId    机构Id
     * @param schoolId 学校Id
     * @return List<SchoolGradeVO>
     */
    public List<SchoolGradeVO> getReviewGrade(Integer planId, Integer orgId, Integer schoolId) {
        List<ScreeningPlanSchoolStudent> results = getMatchRescreenResults(planId, orgId, schoolId, null, null);
        if (CollectionUtils.isEmpty(results)) {
            return new ArrayList<>();
        }
        return screeningPlanSchoolStudentFacadeService.getSchoolGradeVOS(screeningPlanSchoolStudentService.getByPlanIdAndSchoolIdAndId(planId, schoolId, results.stream().map(ScreeningPlanSchoolStudent::getId).collect(Collectors.toList())));
    }

    /**
     * 获取复查告知书数据
     *
     * @param planId   筛查计划Id
     * @param orgId    机构Id
     * @param schoolId 学校Id
     * @param gradeId  年级Id
     * @param classId  班级Id
     * @return List<ReviewInformExportDataDTO>
     */
    public List<ReviewInformExportDataDTO> getExportData(Integer planId, Integer orgId, Integer schoolId, Integer gradeId, Integer classId) {
        ScreeningPlan screeningPlan = screeningPlanService.getById(planId);

        List<ScreeningPlanSchoolStudent> planSchoolStudents = getMatchRescreenResults(planId, orgId, schoolId, gradeId, classId);

        // 获取体重筛查结果Map
        List<VisionScreeningResult> resultList = visionScreeningResultService.getFirstByPlanStudentIds(planSchoolStudents.stream().map(ScreeningPlanSchoolStudent::getId).collect(Collectors.toList()));
        Map<Integer, HeightAndWeightDataDO> heightAndWeightDataMap = resultList.stream().filter(plantStudent -> Objects.nonNull(plantStudent.getHeightAndWeightData())).collect(Collectors.toMap(VisionScreeningResult::getScreeningPlanSchoolStudentId, VisionScreeningResult::getHeightAndWeightData));

        // 筛查时间
        Map<Integer, Date> screeningDateMap = resultList.stream().collect(Collectors.toMap(VisionScreeningResult::getScreeningPlanSchoolStudentId, VisionScreeningResult::getCreateTime));

        List<ReviewInformExportDataDTO> exportDataDTOS = new ArrayList<>();
        planSchoolStudents.forEach(planSchoolStudent -> {
            ReviewInformExportDataDTO exportDataDTO = new ReviewInformExportDataDTO();
            BeanUtils.copyProperties(planSchoolStudent, exportDataDTO);
            exportDataDTO.setPlanDate(screeningPlan.getCreateTime());

            HeightAndWeightDataDO heightAndWeightDataDO = heightAndWeightDataMap.get(planSchoolStudent.getId());
            if (Objects.nonNull(heightAndWeightDataDO)) {
                exportDataDTO.setWeight(heightAndWeightDataDO.getWeight().toString());
                exportDataDTO.setHeight(heightAndWeightDataDO.getHeight().toString());
            }
            exportDataDTO.setScreeningDate(screeningDateMap.getOrDefault(planSchoolStudent.getId(), null));
            exportDataDTOS.add(exportDataDTO);
        });
        return exportDataDTOS;
    }

    /**
     * 同步导出
     *
     * @param planId   筛查计划Id
     * @param orgId    机构Id
     * @param schoolId 学校Id
     * @param gradeId  年级Id
     * @param classId  班级Id
     * @return 文件URL
     */
    public String syncExportReview(Integer planId, Integer orgId, Integer schoolId, Integer gradeId, Integer classId) {
        return html2PdfService.syncGeneratorPDF(getHtmlUrl(planId, orgId, schoolId, gradeId, classId), RESCREEN_NAME, UUID.randomUUID().toString()).getUrl();

    }

    /**
     * 异步导出
     *
     * @param planId   筛查计划Id
     * @param orgId    机构Id
     * @param schoolId 学校Id
     * @param gradeId  年级Id
     * @param classId  班级Id
     * @param type     类型
     * @param userId   用户Id
     */
    @Async
    public void asyncExportReview(Integer planId, Integer orgId, Integer schoolId, Integer gradeId, Integer classId, Integer type, Integer userId) {
        List<ScreeningPlanSchoolStudent> matchRescreenResults = getMatchRescreenResults(planId, orgId, schoolId, gradeId, classId);
        if (CollectionUtils.isEmpty(matchRescreenResults)) {
            return;
        }
        String fileSaveParentPath = FileUtils.getFileSaveParentPath(pdfSavePath);

        List<Integer> schoolIds = matchRescreenResults.stream().map(ScreeningPlanSchoolStudent::getSchoolId).collect(Collectors.toList());
        Map<Integer, String> schoolMap = schoolService.getByIds(schoolIds).stream().collect(Collectors.toMap(School::getId, School::getName));

        List<Integer> gradeIds = matchRescreenResults.stream().map(ScreeningPlanSchoolStudent::getGradeId).collect(Collectors.toList());
        Map<Integer, SchoolGrade> gradeMap = schoolGradeService.getGradeMapByIds(gradeIds);

        List<Integer> classIds = matchRescreenResults.stream().map(ScreeningPlanSchoolStudent::getClassId).collect(Collectors.toList());
        Map<Integer, SchoolClass> classMap = schoolClassService.getClassMapByIds(classIds);

        Map<Integer, List<ScreeningPlanSchoolStudent>> planMap = matchRescreenResults.stream().collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getScreeningPlanId));
        planMap.forEach((planKey, planValue) -> {
            Map<Integer, List<ScreeningPlanSchoolStudent>> schoolGroup = planValue.stream().collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getSchoolId));
            schoolGroup.forEach((schoolKey, schoolValue) -> {
                Map<Integer, List<ScreeningPlanSchoolStudent>> gradeGroup = schoolValue.stream().collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getGradeId));
                gradeGroup.forEach((gradeKey, gradeValue) -> {
                    Map<Integer, List<ScreeningPlanSchoolStudent>> classGroup = gradeValue.stream().collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getClassId));
                    classGroup.forEach((classKey, classValue) -> {
                        PdfResponseDTO pdfResponseDTO = html2PdfService.syncGeneratorPDF(getHtmlUrl(planKey, orgId, schoolKey, gradeKey, classKey), RESCREEN_NAME, UUID.randomUUID().toString());
                        log.info("response:{}", JSONObject.toJSONString(pdfResponseDTO));
                        try {
                            if (ExportTypeConst.GRADE.equals(type)) {
                                FileUtils.downloadFile(pdfResponseDTO.getUrl(),
                                        Paths.get(fileSaveParentPath,
                                                gradeMap.get(gradeKey).getName() + RESCREEN_NAME,
                                                classMap.get(classKey).getName() + RESCREEN_NAME,
                                                RESCREEN_NAME + ".pdf").toString());
                            } else {
                                FileUtils.downloadFile(pdfResponseDTO.getUrl(),
                                        Paths.get(fileSaveParentPath,
                                                schoolMap.get(schoolKey) + RESCREEN_NAME,
                                                gradeMap.get(gradeKey).getName() + RESCREEN_NAME,
                                                classMap.get(classKey).getName() + RESCREEN_NAME,
                                                RESCREEN_NAME + ".pdf").toString());
                            }

                        } catch (Exception e) {
                            log.error("Exception", e);
                        }
                    });
                });
            });
        });

        File renameFile = FileUtil.rename(ZipUtil.zip(fileSaveParentPath), getZipFileName(schoolId, gradeId, type), true);
        try {
            noticeService.sendExportSuccessNotice(userId, userId, getNoticeTitle(schoolId, gradeId, type), s3Utils.uploadFileToS3(renameFile));
        } catch (UtilException e) {
            noticeService.sendExportFailNotice(userId, userId, getNoticeTitle(schoolId, gradeId, type) + RESCREEN_NAME);
            throw new BusinessException("发送通知异常");
        } finally {
            FileUtils.deleteDir(new File(fileSaveParentPath));
        }
    }

    private String getHtmlUrl(Integer planKey, Integer orgId, Integer schoolKey, Integer gradeKey, Integer classKey) {
        return String.format(RESCREEN_REVIEW_HTML_URL, htmlUrlHost, planKey,
                Objects.nonNull(schoolKey) ? schoolKey : StringUtils.EMPTY,
                Objects.nonNull(gradeKey) ? gradeKey : StringUtils.EMPTY,
                Objects.nonNull(classKey) ? classKey : StringUtils.EMPTY,
                Objects.nonNull(orgId) ? orgId : StringUtils.EMPTY);
    }

    /**
     * 获取需要复查的学生
     *
     * @param planId   计划Id
     * @param orgId    机构Id
     * @param schoolId 学校Id
     * @param gradeId  年级Id
     * @param classId  班级Id
     * @return List<ScreeningPlanSchoolStudent>
     */
    private List<ScreeningPlanSchoolStudent> getMatchRescreenResults(Integer planId, Integer orgId, Integer schoolId, Integer gradeId, Integer classId) {

        List<ScreeningPlanSchoolStudent> planStudentList = screeningPlanSchoolStudentService.getReviewStudentList(planId, orgId, schoolId, gradeId, classId);
        if (CollectionUtils.isEmpty(planStudentList)) {
            return new ArrayList<>();
        }
        return planStudentList;
    }

    /**
     * 获取导出压缩包名
     *
     * @param schoolId 学校Id
     * @param gradeId  年级Id
     * @param type     类型
     * @return 文件名
     */
    private String getZipFileName(Integer schoolId, Integer gradeId, Integer type) {
        return getDirFileName(schoolId, gradeId, type) + ".zip";
    }

    /**
     * 获取导出通知标题
     *
     * @param schoolId 学校Id
     * @param gradeId  年级Id
     * @param type     类型
     * @return 文件名
     */
    private String getNoticeTitle(Integer schoolId, Integer gradeId, Integer type) {
        return getDirFileName(schoolId, gradeId, type);
    }

    /**
     * 获取导出文件名
     *
     * @param schoolId 学校Id
     * @param gradeId  年级Id
     * @param type     类型
     * @return 文件名
     */
    private String getDirFileName(Integer schoolId, Integer gradeId, Integer type) {
        if (ExportTypeConst.SCHOOL.equals(type)) {
            return schoolService.getById(schoolId).getName() + RESCREEN_NAME;
        }
        if (ExportTypeConst.GRADE.equals(type)) {
            return schoolService.getById(schoolId).getName() + schoolGradeService.getById(gradeId).getName() + RESCREEN_NAME;
        }
        throw new BusinessException("类型异常");
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new HashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
