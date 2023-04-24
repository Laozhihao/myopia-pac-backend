package com.wupol.myopia.business.api.management.service;

import com.alibaba.fastjson.JSON;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.WearingGlassesSituation;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.SchoolResultTemplateExcel;
import com.wupol.myopia.business.core.screening.flow.domain.dto.SchoolResultTemplateImportEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.system.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 学校筛查数据模板
 *
 * @author Simple4H
 */
@Service
@Slf4j
public class SchoolTemplateService {

    private static final String ERROR_MSG = "筛查学生Id为%s，数据错误：%s";

    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private NoticeService noticeService;

    @Resource
    private ScreeningPlanService screeningPlanService;

    @Resource
    private SchoolService schoolService;

    @Autowired
    private ImportScreeningDataHandler importScreeningDataHandler;


    /**
     * 导入筛查数据
     *
     * @param templateExcels 筛查数据
     * @param userId 当前用户ID
     * @param school 学校
     * @param plan 筛查计划
     */
    @Async
    public void importSchoolScreeningData(List<SchoolResultTemplateExcel> templateExcels, Integer userId, School school, ScreeningPlan plan) {
        try {
            importScreeningDataHandler.action(templateExcels, userId, plan, school);
        } catch (Exception e) {
            String content = String.format(CommonConst.SCHOOL_TEMPLATE_EXCEL_IMPORT_ERROR, plan.getTitle(), school.getName());
            noticeService.createExportNotice(userId, userId, content, content, null, CommonConst.NOTICE_STATION_LETTER);
            log.error("导入筛查数据异常，plan=[{}], school=[{}]", plan.getTitle(), school.getName(), e);
        } finally {
            unLock(plan.getId(), school.getId());
        }
    }

    /**
     * 解析Excel数据
     *
     * @param listMap Excel数据
     *
     * @return List<SchoolResultTemplateExcel>
     */
    public List<SchoolResultTemplateExcel> parseExcelData(List<Map<Integer, String>> listMap, Integer screeningPlanId, Integer schoolId) {
        tryLock(screeningPlanId, schoolId);
        List<SchoolResultTemplateExcel> templateExcels = listMap.stream().map(s -> {
            SchoolResultTemplateExcel resultExcelData = new SchoolResultTemplateExcel();
            resultExcelData.setPlanStudentId(s.get(SchoolResultTemplateImportEnum.PLAN_STUDENT_ID.getIndex()));
            resultExcelData.setGlassesType(s.get(SchoolResultTemplateImportEnum.GLASSES_TYPE.getIndex()));
            resultExcelData.setRightNakedVision(s.get(SchoolResultTemplateImportEnum.RIGHT_NAKED_VISION.getIndex()));
            resultExcelData.setLeftNakedVision(s.get(SchoolResultTemplateImportEnum.LEFT_NAKED_VISION.getIndex()));
            resultExcelData.setRightCorrection(s.get(SchoolResultTemplateImportEnum.RIGHT_CORRECTION.getIndex()));
            resultExcelData.setLeftCorrection(s.get(SchoolResultTemplateImportEnum.LEFT_CORRECTION.getIndex()));
            resultExcelData.setRightSph(s.get(SchoolResultTemplateImportEnum.RIGHT_SPH.getIndex()));
            resultExcelData.setRightCyl(s.get(SchoolResultTemplateImportEnum.RIGHT_CYL.getIndex()));
            resultExcelData.setRightAxial(s.get(SchoolResultTemplateImportEnum.RIGHT_AXIAL.getIndex()));
            resultExcelData.setLeftSph(s.get(SchoolResultTemplateImportEnum.LEFT_SPH.getIndex()));
            resultExcelData.setLeftCyl(s.get(SchoolResultTemplateImportEnum.LEFT_CYL.getIndex()));
            resultExcelData.setLeftAxial(s.get(SchoolResultTemplateImportEnum.LEFT_AXIAL.getIndex()));
            resultExcelData.setHeight(s.get(SchoolResultTemplateImportEnum.HEIGHT.getIndex()));
            resultExcelData.setWeight(s.get(SchoolResultTemplateImportEnum.WEIGHT.getIndex()));
            return resultExcelData;
        }).collect(Collectors.toList());
        List<String> errorList = preCheckData(templateExcels);
        if (!CollectionUtils.isEmpty(errorList)) {
            unLock(screeningPlanId, schoolId);
            throw new BusinessException(JSON.toJSONString(errorList));
        }
        return templateExcels;
    }

    /**
     * @param templateExcels 数据
     */
    private List<String> preCheckData(List<SchoolResultTemplateExcel> templateExcels) {
        List<String> result = new ArrayList<>();
        if (!(templateExcels.size() == templateExcels.stream().map(SchoolResultTemplateExcel::getPlanStudentId).filter(StringUtils::isNotBlank).count())) {
            result.add("存在筛查学生Id为空，请确认！");
        }
        if (!CollectionUtils.isEmpty(result)) {
            return result;
        }
        List<Integer> planStudentIds = templateExcels.stream().map(s -> {
            try {
                return Integer.valueOf(s.getPlanStudentId());
            } catch (Exception e) {
                result.add("筛查学生Id异常" + s.getPlanStudentId());
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(result)) {
            return result;
        }
        List<ScreeningPlanSchoolStudent> planSchoolStudentList = screeningPlanSchoolStudentService.getByIds(planStudentIds);
        if (!Objects.equals(planStudentIds.size(), planSchoolStudentList.size())) {
            result.add("存在筛查学生Id为空，请确认！");
        }
        if (!CollectionUtils.isEmpty(result)) {
            return result;
        }

        templateExcels.forEach(s -> {
            if (!StringUtils.isAllBlank(s.getGlassesType(), s.getRightNakedVision(), s.getLeftNakedVision())) {
                if (WearingGlassesSituation.checkKeyByDesc(s.getGlassesType())) {
                    result.add(String.format(ERROR_MSG, s.getPlanStudentId(), "戴镜类型数据为空或无效"));
                }
                if (!WearingGlassesSituation.WEARING_OVERNIGHT_ORTHOKERATOLOGY_TYPE.equals(s.getGlassesType()) && StringUtils.isAnyBlank(s.getRightNakedVision(), s.getLeftNakedVision())) {
                    result.add(String.format(ERROR_MSG, s.getPlanStudentId(), "裸眼视力数据不能为空"));
                }
                if (!WearingGlassesSituation.NOT_WEARING_GLASSES_TYPE.equals(s.getGlassesType()) && StringUtils.isAnyBlank(s.getRightCorrection(), s.getLeftCorrection())) {
                    result.add(String.format(ERROR_MSG, s.getPlanStudentId(), "矫正视力数据不能为空"));
                }
            }
            if (!StringUtils.isAllBlank(s.getLeftSph(), s.getLeftCyl(), s.getLeftAxial(), s.getRightSph(), s.getRightCyl(), s.getRightAxial())) {
                if (StringUtils.isAnyBlank(s.getLeftSph(), s.getLeftCyl(), s.getLeftAxial(), s.getRightSph(), s.getRightCyl(), s.getRightAxial())) {
                    result.add(String.format(ERROR_MSG, s.getPlanStudentId(), "电脑验光数据数据异常"));
                }
            }
            if (!StringUtils.isAllBlank(s.getHeight(), s.getWeight())) {
                if (StringUtils.isAnyBlank(s.getHeight(), s.getWeight())) {
                    result.add(String.format(ERROR_MSG, s.getPlanStudentId(), "体重数据数据异常"));
                }
            }
        });
        return result;
    }

    /**
     * 校验计划和学校
     *
     * @param userId            用户ID
     * @param screeningPlanId   计划ID
     * @param schoolId          学校ID
     * @return  TwoTuple<School, ScreeningPlan>
     */
    public TwoTuple<School, ScreeningPlan> checkPlanAndSchool(Integer userId, Integer screeningPlanId, Integer schoolId) {
        School school = schoolService.getById(schoolId);
        ScreeningPlan plan = screeningPlanService.getById(screeningPlanId);

        if (Objects.isNull(school) || Objects.isNull(plan)) {
            unLock(screeningPlanId, schoolId);
            log.error("【导入筛查数据异常】找不到对应学校或计划，userId:{}, screeningPlanId:{}, schoolId:{}", userId, screeningPlanId, schoolId);
            throw new BusinessException("【导入筛查数据异常】找不到对应学校或计划");
        }
        return TwoTuple.of(school, plan);
    }

    /**
     * 上锁
     *
     * @param screeningPlanId 计划Id
     * @param schoolId        学校Id
     */
    private synchronized void tryLock(Integer screeningPlanId, Integer schoolId) {
        String key = String.format(RedisConstant.IMPORT_SCHOOL_SCREENING_DATA, screeningPlanId, schoolId);
        if (Objects.nonNull(redisUtil.get(key))) {
            throw new BusinessException("数据处理中，请勿重复上传!");
        }
        redisUtil.set(key, screeningPlanId, 3600);
    }

    /**
     * 解锁
     *
     * @param screeningPlanId 计划Id
     * @param schoolId        学校Id
     */
    private void unLock(Integer screeningPlanId, Integer schoolId) {
        String key = String.format(RedisConstant.IMPORT_SCHOOL_SCREENING_DATA, screeningPlanId, schoolId);
        redisUtil.del(key);
    }
}
