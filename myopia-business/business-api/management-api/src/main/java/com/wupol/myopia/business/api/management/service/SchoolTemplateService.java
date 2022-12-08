package com.wupol.myopia.business.api.management.service;

import com.alibaba.fastjson.JSON;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.screening.service.VisionScreeningBizService;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.WearingGlassesSituation;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dos.HeightAndWeightDataDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ComputerOptometryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.SchoolResultTemplateExcel;
import com.wupol.myopia.business.core.screening.flow.domain.dto.SchoolResultTemplateImportEnum;
import com.wupol.myopia.business.core.screening.flow.domain.dto.VisionDataDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.system.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

    @Resource
    private VisionScreeningBizService visionScreeningBizService;

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

    private final static String ERROR_MSG = "筛查学生Id为%s，数据错误：%s";

    /**
     * 导入筛查数据
     *
     * @param templateExcels 筛查数据
     */
    @Transactional(rollbackFor = Exception.class)
    @Async
    public void importSchoolScreeningData(List<SchoolResultTemplateExcel> templateExcels, Integer userId, Integer screeningPlanId, Integer schoolId) {


        School school = schoolService.getById(schoolId);
        ScreeningPlan plan = screeningPlanService.getById(screeningPlanId);

        if (Objects.isNull(school) || Objects.isNull(plan)) {
            unLock(screeningPlanId, schoolId);
            log.error("导入筛查数据异常，userId:{}, screeningPlanId:{}, schoolId:{}", userId, screeningPlanId, schoolId);
            throw new BusinessException("导入筛查数据异常");
        }

        try {
            templateExcels.forEach(templateExcel -> {
                generateHeightAndWeight(templateExcel, plan.getScreeningOrgId(), schoolId, userId);
                generateVisionData(templateExcel, plan.getScreeningOrgId(), schoolId, userId);
                generateComputerOptometry(templateExcel, plan.getScreeningOrgId(), schoolId, userId);
            });
            String content = String.format(CommonConst.SCHOOL_TEMPLATE_EXCEL_IMPORT_SUCCESS, plan.getTitle(), school.getName());
            noticeService.createExportNotice(userId, userId, content, content, null, CommonConst.NOTICE_STATION_LETTER);
        } catch (Exception e) {
            String content = String.format(CommonConst.SCHOOL_TEMPLATE_EXCEL_IMPORT_ERROR, plan.getTitle(), school.getName());
            noticeService.createExportNotice(userId, userId, content, content, null, CommonConst.NOTICE_STATION_LETTER);
            log.error("导入筛查数据异常", e);
            throw new BusinessException("导入筛查数据异常");
        } finally {
            unLock(screeningPlanId, schoolId);
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
                if (StringUtils.isAnyBlank(s.getGlassesType(), s.getRightNakedVision(), s.getLeftNakedVision())) {
                    result.add(String.format(ERROR_MSG, s.getPlanStudentId(), "视力数据异常"));
                }
                if (WearingGlassesSituation.checkKeyByDesc(s.getGlassesType())) {
                    result.add(String.format(ERROR_MSG, s.getPlanStudentId(), "戴镜数据异常"));
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
     * 生成身高体重信息
     *
     * @param data     数据
     * @param orgId    机构Id
     * @param schoolId 学校Id
     * @param userId   用户Id
     */
    private void generateHeightAndWeight(SchoolResultTemplateExcel data, Integer orgId, Integer schoolId, Integer userId) {
        if (StringUtils.isBlank(data.getHeight())) {
            return;
        }
        HeightAndWeightDataDTO heightAndWeightDataDTO = new HeightAndWeightDataDTO();
        heightAndWeightDataDTO.setHeight(new BigDecimal(data.getHeight()).setScale(1, RoundingMode.DOWN));
        heightAndWeightDataDTO.setWeight(new BigDecimal(data.getWeight()).setScale(1, RoundingMode.DOWN));
        heightAndWeightDataDTO.setBmi(heightAndWeightDataDTO.getWeight().divide(heightAndWeightDataDTO.getHeight().multiply(heightAndWeightDataDTO.getHeight()), 1, RoundingMode.DOWN));
        heightAndWeightDataDTO.setDeptId(orgId);
        heightAndWeightDataDTO.setCreateUserId(userId);
        heightAndWeightDataDTO.setPlanStudentId(data.getPlanStudentId());
        heightAndWeightDataDTO.setSchoolId(String.valueOf(schoolId));
        visionScreeningBizService.saveOrUpdateStudentScreenData(heightAndWeightDataDTO);
    }

    /**
     * 生成视力信息
     *
     * @param data     数据
     * @param orgId    机构Id
     * @param schoolId 学校Id
     * @param userId   用户Id
     */
    private void generateVisionData(SchoolResultTemplateExcel data, Integer orgId, Integer schoolId, Integer userId) {
        if (StringUtils.isBlank(data.getGlassesType())) {
            return;
        }
        VisionDataDTO visionDataDTO = new VisionDataDTO();
        visionDataDTO.setRightNakedVision(new BigDecimal(data.getRightNakedVision()));
        visionDataDTO.setLeftNakedVision(new BigDecimal(data.getLeftNakedVision()));
        visionDataDTO.setRightCorrectedVision(Objects.isNull(data.getRightCorrection()) ? null : new BigDecimal(data.getRightCorrection()));
        visionDataDTO.setLeftCorrectedVision(Objects.isNull(data.getLeftCorrection()) ? null : new BigDecimal(data.getLeftCorrection()));
        visionDataDTO.setIsCooperative(0);
        visionDataDTO.setDeptId(orgId);
        visionDataDTO.setCreateUserId(userId);
        visionDataDTO.setPlanStudentId(data.getPlanStudentId());
        visionDataDTO.setSchoolId(String.valueOf(schoolId));
        visionDataDTO.setGlassesType(data.getGlassesType());
        visionScreeningBizService.saveOrUpdateStudentScreenData(visionDataDTO);
    }

    /**
     * 生成电脑验光信息
     *
     * @param data     数据
     * @param orgId    机构Id
     * @param schoolId 学校Id
     * @param userId   用户Id
     */
    private void generateComputerOptometry(SchoolResultTemplateExcel data, Integer orgId, Integer schoolId, Integer userId) {
        if (StringUtils.isBlank(data.getRightSph())) {
            return;
        }
        ComputerOptometryDTO computerOptometryDTO = new ComputerOptometryDTO();
        computerOptometryDTO.setLSph(new BigDecimal(replacePlusChar(data.getLeftSph())));
        computerOptometryDTO.setLCyl(new BigDecimal(replacePlusChar(data.getLeftCyl())));
        computerOptometryDTO.setLAxial(new BigDecimal(data.getLeftAxial()));
        computerOptometryDTO.setRSph(new BigDecimal(replacePlusChar(data.getRightSph())));
        computerOptometryDTO.setRCyl(new BigDecimal(replacePlusChar(data.getRightCyl())));
        computerOptometryDTO.setRAxial(new BigDecimal(data.getRightAxial()));
        computerOptometryDTO.setIsCooperative(0);
        computerOptometryDTO.setSchoolId(String.valueOf(schoolId));
        computerOptometryDTO.setDeptId(orgId);
        computerOptometryDTO.setCreateUserId(userId);
        computerOptometryDTO.setPlanStudentId(data.getPlanStudentId());
        computerOptometryDTO.setIsState(0);
        visionScreeningBizService.saveOrUpdateStudentScreenData(computerOptometryDTO);
    }

    /**
     * 除去+号
     *
     * @param val 值
     *
     * @return String
     */
    private String replacePlusChar(String val) {
        return StringUtils.replace(val, "+", "");
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
