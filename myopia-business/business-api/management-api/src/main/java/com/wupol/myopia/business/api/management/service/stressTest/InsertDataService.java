package com.wupol.myopia.business.api.management.service.stressTest;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.wupol.myopia.base.util.GlassesTypeEnum;
import com.wupol.myopia.business.aggregation.student.domain.builder.SchoolStudentInfoBuilder;
import com.wupol.myopia.business.aggregation.student.domain.builder.StudentInfoBuilder;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.NationEnum;
import com.wupol.myopia.business.common.utils.constant.ScreeningTypeEnum;
import com.wupol.myopia.business.common.utils.constant.SourceClientEnum;
import com.wupol.myopia.business.core.common.constant.ArtificialStatusConstant;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.dto.SchoolClassExportDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolGradeExportDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.builder.StatConclusionBuilder;
import com.wupol.myopia.business.core.screening.flow.domain.dos.ComputerOptometryDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.VisionDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.model.*;
import com.wupol.myopia.business.core.screening.flow.util.ScreeningCodeGenerator;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
@Service
public class InsertDataService {

    private static final double[] VISION = { 4.0, 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 4.9, 5.0, 5.1, 5.2, 5.3 };
    private static final double[] OPTOMETRY = { -5.00, -4.50, -4.25, -4.00, -3.75, -3.50, -3.25, -3.00, -2.75, -2.50, -2.25, -2.00,
            -1.75, -1.50, -1.25, -1.00, -0.75, -0.50, -0.25, 0.00, 0.25, 0.50, 0.75, 1.00, 1.25, 1.50, 1.75, 2.00, 2.25, 2.50  };

    /** 每个班学生人数 */
    private static final int EVERY_CLASS_STUDENT_NUM = 100;
    /** 学号前缀 */
    private static final String SNO_PREFIX = "10";
    /** 学号生成器 */
    private static final AtomicInteger SNO_GENERATOR = new AtomicInteger(100000000);
    private static final AtomicInteger STUDENT_ID_GENERATOR = new AtomicInteger(0);
    private static final AtomicInteger PLAN_STUDENT_ID_GENERATOR = new AtomicInteger(0);
    private static final AtomicInteger VISION_RESULT_ID_GENERATOR = new AtomicInteger(0);

    @Resource(name = "dataSource")
    private DataSource dataSource;

    @Async
    public void insertDataBatch(List<ScreeningPlanSchool> planSchoolList, int batchNo, CountDownLatch latch, int schoolNumPerThread, Map<Integer, ScreeningPlan> planMap,
                                Map<Integer, School> schoolMap, Map<Integer, List<SchoolGradeExportDTO>> gradeMap, Map<Integer, List<SchoolClassExportDTO>> classMap) throws SQLException {
        // 获取连接
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        // 获取拼凑SQL容器
        PreparedStatement studentStatement = connection.prepareStatement("INSERT INTO `m_student`(`id`, `sno`, `grade_id`, `grade_type`, `class_id`, `name`, `gender`, `birthday`, `nation`, `id_card`, `last_screening_time`, `status`, `school_id`, `source_client`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        PreparedStatement planStudentStatement = connection.prepareStatement("INSERT INTO `m_screening_plan_school_student`(`id`, `screening_plan_id`, `school_id`, `school_name`, `plan_district_id`, `school_district_id`, `grade_id`, `screening_task_id`, `class_id`, `screening_org_id`, `student_id`, `birthday`, `id_card`, `student_age`, `student_no`, `student_name`, `gender`, `src_screening_notice_id`, `grade_type`, `nation`, `artificial`, `screening_code`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        PreparedStatement schoolStudentStatement = connection.prepareStatement("INSERT INTO `m_school_student`(`student_id`, `school_id`, `sno`, `grade_id`, `grade_name`, `grade_type`, `class_id`, `class_name`, `name`, `gender`, `birthday`, `nation`, `id_card`, `status`, `last_screening_time`, `source_client`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        PreparedStatement visionScreeningResultStatement = connection.prepareStatement("INSERT INTO `m_vision_screening_result`(`id`, `screening_plan_school_student_id`, `screening_org_id`, `task_id`, `create_user_id`, `plan_id`, `school_id`, `student_id`, `district_id`, `vision_data`, `computer_optometry`, `is_double_screen`, `screening_type`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        PreparedStatement statConclusionStatement = connection.prepareStatement("INSERT INTO `m_stat_conclusion`(`result_id`, `src_screening_notice_id`, `task_id`, `plan_id`, `screening_org_id`, `district_id`, `age`, `school_age`, `gender`, `warning_level`, " +
                                                                    "`vision_l`, `vision_r`, `is_low_vision`, `is_refractive_error`, `is_myopia`, `is_hyperopia`, `is_wearing_glasses`, `is_recommend_visit`, `is_rescreen`, `is_vision_warning`, " +
                                                                    "`vision_warning_update_time`, `rescreen_error_num`, `is_valid`, `screening_plan_school_student_id`, `school_grade_code`, `school_id`, `school_class_name`, `myopia_warning_level`, `naked_vision_warning_level`, " +
                                                                    "`glasses_type`, `vision_correction`, `student_id`, `myopia_level`, `hyperopia_level`, `astigmatism_level`, `screening_type`,  `is_review`, `is_anisometropia`, `low_vision_level`, `is_cooperative`, `screening_myopia`, `is_astigmatism`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        try {
            // 拼凑SQL
            for (int j = 0; j < schoolNumPerThread; j++) {
                int index = batchNo * schoolNumPerThread + j;
                ScreeningPlanSchool planSchool = planSchoolList.get(index);
                log.info("【{}批-{}条】计划：{}，学校：{}", batchNo, index, planSchool.getScreeningPlanId(), planSchool.getSchoolName());
                ScreeningPlan screeningPlan = planMap.get(planSchool.getScreeningPlanId());
                School school = schoolMap.get(planSchool.getSchoolId());
                batchInsertStudentData(screeningPlan, school, gradeMap, classMap, studentStatement, planStudentStatement, schoolStudentStatement, visionScreeningResultStatement, statConclusionStatement);
                // 提交SQL(200家学校提交一次)
                if ((index + 1) % 75 == 0) {
                    log.info("第{}批前{}条，提交......", batchNo, j + 1);
                    executeBatch(studentStatement, planStudentStatement, schoolStudentStatement, visionScreeningResultStatement, statConclusionStatement);
                    log.info("第{}批前{}条，提交完成", batchNo, j + 1);
                }
                if ((index + 1) % 1000 == 0) {
                    log.info("第{}批前{}条，提交事务", batchNo, j + 1);
                    connection.commit();
                }
            }
            // 提交SQL(避免有遗漏的)
            executeBatch(studentStatement, planStudentStatement, schoolStudentStatement, visionScreeningResultStatement, statConclusionStatement);
            // 提交事务
            connection.commit();
            log.info("第{}批，完成", batchNo);
        } catch (Exception e) {
            log.error("批量插入数据异常", e);
            throw new RuntimeException(e);
        } finally {
            // 关闭连接
            studentStatement.close();
            planStudentStatement.close();
            schoolStudentStatement.close();
            visionScreeningResultStatement.close();
            statConclusionStatement.close();
            connection.close();
            latch.countDown();
        }

    }

    private void executeBatch(PreparedStatement studentStatement, PreparedStatement planStudentStatement, PreparedStatement schoolStudentStatement, PreparedStatement visionScreeningResultStatement, PreparedStatement statConclusionStatement) throws SQLException {
        // 提交SQL到数据库
        studentStatement.executeBatch();
        planStudentStatement.executeBatch();
        schoolStudentStatement.executeBatch();
        visionScreeningResultStatement.executeBatch();
        statConclusionStatement.executeBatch();
        // 清空
        studentStatement.clearBatch();
        planStudentStatement.clearBatch();
        schoolStudentStatement.clearBatch();
        visionScreeningResultStatement.clearBatch();
        statConclusionStatement.clearBatch();
    }


    private void batchInsertStudentData(ScreeningPlan screeningPlan, School school, Map<Integer, List<SchoolGradeExportDTO>> gradeMap, Map<Integer, List<SchoolClassExportDTO>> classMap,
                                        PreparedStatement studentStatement, PreparedStatement planStudentStatement, PreparedStatement schoolStudentStatement, PreparedStatement visionScreeningResultStatement, PreparedStatement statConclusionStatement) throws IOException, SQLException {
        List<SchoolGradeExportDTO> grades = gradeMap.get(school.getId());
        for (SchoolGradeExportDTO schoolGrade : grades) {
            List<SchoolClassExportDTO> classListOfGrade = classMap.get(schoolGrade.getId());
            for (SchoolClassExportDTO schoolClass : classListOfGrade) {
                batchInsertStudentData(screeningPlan, school, schoolGrade, schoolClass, studentStatement, planStudentStatement, schoolStudentStatement, visionScreeningResultStatement, statConclusionStatement);
            }
        }
    }

    private void batchInsertStudentData(ScreeningPlan screeningPlan, School school, SchoolGradeExportDTO schoolGrade, SchoolClassExportDTO schoolClass,
                                        PreparedStatement studentStatement, PreparedStatement planStudentStatement, PreparedStatement schoolStudentStatement, PreparedStatement visionScreeningResultStatement, PreparedStatement statConclusionStatement) throws IOException, SQLException {
        // 1. 批量导入筛查学生
        // 1) student
        List<Student> studentList = new ArrayList<>();
        for (int i = 0; i < EVERY_CLASS_STUDENT_NUM; i++) {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(schoolGrade.getGradeCode());
            String idCard = IdCardGenerator.getIdCard(gradeCodeEnum.getAge());
            if (StringUtils.isEmpty(idCard)) {
                log.error("IdCard 为空！idCard = [{}], age =[{}]", idCard, gradeCodeEnum.getAge());
                IdCardUtil.generateIdCard(gradeCodeEnum.getAge(), gradeCodeEnum.getAge());
            }
            Student student = new Student()
                    .setId(STUDENT_ID_GENERATOR.addAndGet(1))
                    .setBirthday(IdCardUtil.getBirthDay(idCard))
                    .setIdCard(idCard)
                    .setGender(IdCardUtil.getGender(idCard))
                    .setClassId(schoolClass.getId())
                    .setGradeId(schoolGrade.getId())
                    .setGradeType(gradeCodeEnum.getType())
                    .setLastScreeningTime(new Date())
                    .setName(ChineseNameUtil.getNameFromLocalTxtFile())
                    .setNation(NationEnum.HAN.getCode())
                    .setSchoolId(school.getId())
                    .setSno(SNO_PREFIX + SNO_GENERATOR.getAndAdd(1))
                    .setStatus(0)
                    .setSourceClient(SourceClientEnum.SCREENING_PLAN.type);
            studentList.add(student);
        }
        for (Student student: studentList) {
            // 2) planStudent
            ScreeningPlanSchoolStudent planStudent = new ScreeningPlanSchoolStudent()
                    .setId(PLAN_STUDENT_ID_GENERATOR.addAndGet(1))
                    .setStudentNo(student.getSno())
                    .setStudentId(student.getId())
                    .setStudentAge(IdcardUtil.getAgeByIdCard(student.getIdCard()))
                    .setStudentName(student.getName())
                    .setScreeningOrgId(screeningPlan.getScreeningOrgId())
                    .setScreeningPlanId(screeningPlan.getId())
                    .setScreeningTaskId(screeningPlan.getScreeningTaskId())
                    .setSrcScreeningNoticeId(screeningPlan.getSrcScreeningNoticeId())
                    .setPlanDistrictId(screeningPlan.getDistrictId())
                    .setArtificial(ArtificialStatusConstant.NON_ARTIFICIAL)
                    .setBirthday(student.getBirthday())
                    .setClassId(student.getClassId())
                    .setGender(student.getGender())
                    .setGradeId(student.getGradeId())
                    .setGradeType(student.getGradeType())
                    .setIdCard(student.getIdCard())
                    .setNation(student.getNation())
                    .setSchoolId(school.getId())
                    .setSchoolDistrictId(school.getDistrictId())
                    .setSchoolName(school.getName())
                    .setScreeningCode(ScreeningCodeGenerator.nextId());
            // 3) schoolStudent (得有学号)
            SchoolStudent schoolStudent = new SchoolStudent()
                    .setSchoolId(school.getId())
                    .setStudentId(student.getId())
                    .setName(student.getName())
                    .setBirthday(student.getBirthday())
                    .setGender(student.getGender())
                    .setIdCard(student.getIdCard())
                    .setClassId(student.getClassId())
                    .setClassName(schoolClass.getName())
                    .setGradeId(student.getGradeId())
                    .setGradeName(schoolGrade.getName())
                    .setGradeType(student.getGradeType())
                    .setLastScreeningTime(new Date())
                    .setNation(student.getNation())
                    .setSno(student.getSno())
                    .setSourceClient(student.getSourceClient())
                    .setStatus(0);

            // 2. 批量上传筛查数据
            // 1) 插入 result
            ComputerOptometryDO computerOptometryDO = getComputerData();
            VisionDataDO visionDataDO = getVisionData();
            VisionScreeningResult visionScreeningResult = new VisionScreeningResult()
                    .setId(VISION_RESULT_ID_GENERATOR.addAndGet(1))
                    .setScreeningOrgId(screeningPlan.getScreeningOrgId())
                    .setStudentId(student.getId())
                    .setScreeningType(ScreeningTypeEnum.VISION.getType())
                    .setScreeningPlanSchoolStudentId(PLAN_STUDENT_ID_GENERATOR.get())
                    .setCreateUserId(-1)
                    .setDistrictId(school.getDistrictId())
                    .setIsDoubleScreen(Boolean.FALSE)
                    .setPlanId(screeningPlan.getId())
                    .setSchoolId(school.getId())
                    .setTaskId(screeningPlan.getScreeningTaskId()).setVisionData(visionDataDO).setComputerOptometry(computerOptometryDO);
            // 2) 插入 statConclusion
            StatConclusionBuilder statConclusionBuilder = StatConclusionBuilder.getStatConclusionBuilder();
            StatConclusion statConclusion = statConclusionBuilder.setCurrentVisionScreeningResult(visionScreeningResult, null).setStatConclusion(null)
                    .setScreeningPlanSchoolStudent(planStudent)
                    .setGradeCode(schoolGrade.getGradeCode())
                    .setSchoolClass(new SchoolClass().setName(schoolClass.getName()).setId(schoolClass.getId()).setGradeId(schoolClass.getGradeId()).setSchoolId(schoolClass.getSchoolId()))
                    .build();
            // 3) 更新 student、studentSchool
            StudentInfoBuilder.setStudentInfoByStatConclusion(student, statConclusion, new Date());
            SchoolStudentInfoBuilder.setSchoolStudentInfoByStatConclusion(schoolStudent, statConclusion, new Date());

            // 3. 构造SQL
            preparedStudentSQL(student, studentStatement);
            preparedPlanStudentSQL(planStudent, planStudentStatement);
            preparedSchoolStudentSQL(schoolStudent, schoolStudentStatement);
            preparedScreeningResultSQL(visionScreeningResult, visionScreeningResultStatement);
            preparedStatConclusionSQL(statConclusion, statConclusionStatement);
        }

    }

    private void preparedStudentSQL(Student student, PreparedStatement studentStatement) throws SQLException {
        // TODO: 加上视力结果
        studentStatement.setInt(1, student.getId());
        studentStatement.setString(2, student.getSno());
        studentStatement.setInt(3, student.getGradeId());
        studentStatement.setInt(4, student.getGradeType());
        studentStatement.setInt(5, student.getClassId());
        studentStatement.setString(6, StringUtils.hasLength(student.getName()) ? student.getName() : student.getSno());
        studentStatement.setInt(7, student.getGender());
        studentStatement.setDate(8, new java.sql.Date(student.getBirthday().getTime()));
        studentStatement.setInt(9, student.getNation());
        studentStatement.setString(10, student.getIdCard());
        studentStatement.setDate(11, new java.sql.Date(student.getLastScreeningTime().getTime()));
        studentStatement.setInt(12, student.getStatus());
        studentStatement.setInt(13, student.getSchoolId());
        studentStatement.setInt(14, student.getSourceClient());
        studentStatement.addBatch();
    }

    private void preparedPlanStudentSQL(ScreeningPlanSchoolStudent planStudent, PreparedStatement planStudentStatement) throws SQLException {
        planStudentStatement.setInt(1, planStudent.getId());
        planStudentStatement.setInt(2, planStudent.getScreeningPlanId());
        planStudentStatement.setInt(3, planStudent.getSchoolId());
        planStudentStatement.setString(4, planStudent.getSchoolName());
        planStudentStatement.setInt(5, planStudent.getPlanDistrictId());
        planStudentStatement.setInt(6, planStudent.getSchoolDistrictId());
        planStudentStatement.setInt(7, planStudent.getGradeId());
        planStudentStatement.setInt(8, planStudent.getScreeningTaskId());
        planStudentStatement.setInt(9, planStudent.getClassId());
        planStudentStatement.setInt(10, planStudent.getScreeningOrgId());
        planStudentStatement.setInt(11, planStudent.getStudentId());
        planStudentStatement.setDate(12, new java.sql.Date(planStudent.getBirthday().getTime()));
        planStudentStatement.setString(13, planStudent.getIdCard());
        planStudentStatement.setInt(14, planStudent.getStudentAge());
        planStudentStatement.setString(15, planStudent.getStudentNo());
        planStudentStatement.setString(16, planStudent.getStudentName());
        planStudentStatement.setInt(17, planStudent.getGender());
        planStudentStatement.setInt(18, planStudent.getSrcScreeningNoticeId());
        planStudentStatement.setInt(19, planStudent.getGradeType());
        planStudentStatement.setInt(20, planStudent.getNation());
        planStudentStatement.setInt(21, planStudent.getArtificial());
        planStudentStatement.setLong(22, planStudent.getScreeningCode());
        planStudentStatement.addBatch();
    }

    private void preparedSchoolStudentSQL(SchoolStudent schoolStudent, PreparedStatement schoolStudentStatement) throws SQLException {
        // TODO: 加上视力结果
        schoolStudentStatement.setInt(1, schoolStudent.getStudentId());
        schoolStudentStatement.setInt(2, schoolStudent.getSchoolId());
        schoolStudentStatement.setString(3, schoolStudent.getSno());
        schoolStudentStatement.setInt(4, schoolStudent.getGradeId());
        schoolStudentStatement.setString(5, schoolStudent.getGradeName());
        schoolStudentStatement.setInt(6, schoolStudent.getGradeType());
        schoolStudentStatement.setInt(7, schoolStudent.getClassId());
        schoolStudentStatement.setString(8, schoolStudent.getClassName());
        schoolStudentStatement.setString(9, schoolStudent.getName());
        schoolStudentStatement.setInt(10, schoolStudent.getGender());
        schoolStudentStatement.setDate(11, new java.sql.Date(schoolStudent.getBirthday().getTime()));
        schoolStudentStatement.setInt(12, schoolStudent.getNation());
        schoolStudentStatement.setString(13, schoolStudent.getIdCard());
        schoolStudentStatement.setInt(14, schoolStudent.getStatus());
        schoolStudentStatement.setDate(15, new java.sql.Date(schoolStudent.getLastScreeningTime().getTime()));
        schoolStudentStatement.setInt(16, schoolStudent.getSourceClient());
        schoolStudentStatement.addBatch();
    }

    private void preparedScreeningResultSQL(VisionScreeningResult visionScreeningResult, PreparedStatement visionScreeningResultStatement) throws SQLException {
        visionScreeningResultStatement.setInt(1, visionScreeningResult.getId());
        visionScreeningResultStatement.setInt(2, visionScreeningResult.getScreeningPlanSchoolStudentId());
        visionScreeningResultStatement.setInt(3, visionScreeningResult.getScreeningOrgId());
        visionScreeningResultStatement.setInt(4, visionScreeningResult.getTaskId());
        visionScreeningResultStatement.setInt(5, visionScreeningResult.getCreateUserId());
        visionScreeningResultStatement.setInt(6, visionScreeningResult.getPlanId());
        visionScreeningResultStatement.setInt(7, visionScreeningResult.getSchoolId());
        visionScreeningResultStatement.setInt(8, visionScreeningResult.getStudentId());
        visionScreeningResultStatement.setInt(9, visionScreeningResult.getDistrictId());
        visionScreeningResultStatement.setString(10, JSONObject.toJSONString(visionScreeningResult.getVisionData()));
        visionScreeningResultStatement.setString(11, JSONObject.toJSONString(visionScreeningResult.getComputerOptometry()));
        visionScreeningResultStatement.setBoolean(12, visionScreeningResult.getIsDoubleScreen());
        visionScreeningResultStatement.setInt(13, visionScreeningResult.getScreeningType());
        visionScreeningResultStatement.addBatch();
    }

    private void preparedStatConclusionSQL(StatConclusion statConclusion, PreparedStatement statConclusionStatement) throws SQLException {
        statConclusionStatement.setInt(1, statConclusion.getResultId());
        statConclusionStatement.setInt(2, statConclusion.getSrcScreeningNoticeId());
        statConclusionStatement.setInt(3, statConclusion.getTaskId());
        statConclusionStatement.setInt(4, statConclusion.getPlanId());
        statConclusionStatement.setInt(5, statConclusion.getScreeningOrgId());
        statConclusionStatement.setInt(6, statConclusion.getDistrictId());
        statConclusionStatement.setInt(7, statConclusion.getAge());
        statConclusionStatement.setInt(8, statConclusion.getSchoolAge());
        statConclusionStatement.setInt(9, statConclusion.getGender());
        setIntValueIfNull(statConclusionStatement, 10, statConclusion.getWarningLevel());

        statConclusionStatement.setBigDecimal(11, statConclusion.getVisionL());
        statConclusionStatement.setBigDecimal(12, statConclusion.getVisionR());
        statConclusionStatement.setBoolean(13, statConclusion.getIsLowVision());
        statConclusionStatement.setBoolean(14, statConclusion.getIsRefractiveError());
        statConclusionStatement.setBoolean(15, statConclusion.getIsMyopia());
        statConclusionStatement.setBoolean(16, statConclusion.getIsHyperopia());
        statConclusionStatement.setBoolean(17, statConclusion.getIsWearingGlasses());
        statConclusionStatement.setBoolean(18, statConclusion.getIsRecommendVisit());
        statConclusionStatement.setBoolean(19, statConclusion.getIsRescreen());
        statConclusionStatement.setBoolean(20, statConclusion.getIsVisionWarning());

        statConclusionStatement.setDate(21, new java.sql.Date(statConclusion.getVisionWarningUpdateTime().getTime()));
        statConclusionStatement.setLong(22, statConclusion.getRescreenErrorNum());
        statConclusionStatement.setBoolean(23, statConclusion.getIsValid());
        statConclusionStatement.setInt(24, statConclusion.getScreeningPlanSchoolStudentId());
        statConclusionStatement.setString(25, statConclusion.getSchoolGradeCode());
        statConclusionStatement.setInt(26, statConclusion.getSchoolId());
        statConclusionStatement.setString(27, statConclusion.getSchoolClassName());
        setIntValueIfNull(statConclusionStatement, 28, statConclusion.getMyopiaWarningLevel());
        setIntValueIfNull(statConclusionStatement, 29, statConclusion.getNakedVisionWarningLevel());

        statConclusionStatement.setInt(30, statConclusion.getGlassesType());
        setIntValueIfNull(statConclusionStatement, 31, statConclusion.getVisionCorrection());
        statConclusionStatement.setInt(32, statConclusion.getStudentId());
        setIntValueIfNull(statConclusionStatement, 33, statConclusion.getMyopiaLevel());
        setIntValueIfNull(statConclusionStatement, 34, statConclusion.getHyperopiaLevel());
        setIntValueIfNull(statConclusionStatement, 35, statConclusion.getAstigmatismLevel());
        statConclusionStatement.setInt(36, statConclusion.getScreeningType());
        statConclusionStatement.setBoolean(37, statConclusion.getIsReview());
        statConclusionStatement.setBoolean(38, statConclusion.getIsAnisometropia());
        setIntValueIfNull(statConclusionStatement, 39, statConclusion.getLowVisionLevel());
        statConclusionStatement.setInt(40, statConclusion.getIsCooperative());
        setIntValueIfNull(statConclusionStatement, 41, statConclusion.getScreeningMyopia());
        statConclusionStatement.setBoolean(42, statConclusion.getIsAstigmatism());
        statConclusionStatement.addBatch();
    }

    private void setIntValueIfNull(PreparedStatement statement, Integer parameterIndex, Integer parameterValue) throws SQLException {
        if (Objects.isNull(parameterValue)) {
            statement.setNull(parameterIndex, Types.TINYINT);
        } else {
            statement.setInt(parameterIndex, parameterValue);
        }
    }
    private VisionDataDO getVisionData() {
        int glassesType = RandomUtil.randomInt(0, 4);
        double leftNakedVision = randomFrom(VISION);
        double rightNakedVision = randomFrom(VISION);
        VisionDataDO.VisionData leftVisionData = new VisionDataDO.VisionData()
                .setNakedVision(BigDecimal.valueOf(leftNakedVision))
                .setGlassesType(glassesType)
                .setLateriality(CommonConst.LEFT_EYE);
        VisionDataDO.VisionData rightVisionData = new VisionDataDO.VisionData()
                .setNakedVision(BigDecimal.valueOf(rightNakedVision))
                .setGlassesType(glassesType)
                .setLateriality(CommonConst.RIGHT_EYE);

        // 如果戴镜，则填充矫正视力
        if (glassesType != GlassesTypeEnum.NOT_WEARING.code) {
            int leftIndex = ArrayUtil.indexOf(VISION, leftNakedVision);
            int rightIndex = ArrayUtil.indexOf(VISION, rightNakedVision);
            leftVisionData.setCorrectedVision(BigDecimal.valueOf(randomFrom(VISION, leftIndex)));
            rightVisionData.setCorrectedVision(BigDecimal.valueOf(randomFrom(VISION, rightIndex)));
        }
        // 戴OK镜
        if (glassesType == GlassesTypeEnum.ORTHOKERATOLOGY.code) {
            leftVisionData.setOkDegree(BigDecimal.valueOf(RandomUtil.randomInt(100, 600)));
            rightVisionData.setOkDegree(BigDecimal.valueOf(RandomUtil.randomInt(100, 600)));
        }

        VisionDataDO visionDataDO = new VisionDataDO().setRightEyeData(rightVisionData).setLeftEyeData(leftVisionData).setIsCooperative(0);
        visionDataDO.setDiagnosis(0);
        visionDataDO.setCreateUserId(-1);
        visionDataDO.setUpdateTime(System.currentTimeMillis());
        return visionDataDO;
    }

    private ComputerOptometryDO getComputerData() {
        ComputerOptometryDO.ComputerOptometry leftComputerOptometry = new ComputerOptometryDO.ComputerOptometry().setAxial(BigDecimal.valueOf(randomFrom(OPTOMETRY))).setCyl(BigDecimal.valueOf(randomFrom(OPTOMETRY))).setSph(BigDecimal.valueOf(randomFrom(OPTOMETRY))).setLateriality(CommonConst.LEFT_EYE);
        ComputerOptometryDO.ComputerOptometry rightComputerOptometry = new ComputerOptometryDO.ComputerOptometry().setAxial(BigDecimal.valueOf(randomFrom(OPTOMETRY))).setCyl(BigDecimal.valueOf(randomFrom(OPTOMETRY))).setSph(BigDecimal.valueOf(randomFrom(OPTOMETRY))).setLateriality(CommonConst.RIGHT_EYE);
        ComputerOptometryDO computerOptometryDO = new ComputerOptometryDO().setRightEyeData(rightComputerOptometry).setLeftEyeData(leftComputerOptometry).setIsCooperative(0);
        computerOptometryDO.setDiagnosis(0);
        computerOptometryDO.setCreateUserId(-1);
        computerOptometryDO.setUpdateTime(System.currentTimeMillis());
        return computerOptometryDO;
    }


    private static double randomFrom(double[] items) {
        return randomFrom(items, 0);
    }

    private static double randomFrom(double[] items, int min) {
        return items[RandomUtil.randomInt(min, items.length)];
    }

}
