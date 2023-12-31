package com.wupol.myopia.business.core.school.management.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentListResponseDTO;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentQueryBO;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentRequestDTO;
import com.wupol.myopia.business.core.school.management.domain.dto.StudentCountDTO;
import com.wupol.myopia.business.core.school.management.domain.mapper.SchoolStudentMapper;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.*;

/**
 * 学校端-学生服务
 *
 * @author Simple4H
 */
@Service
@Log4j2
public class SchoolStudentService extends BaseService<SchoolStudentMapper, SchoolStudent> {

    /**
     * 获取学生列表
     *
     * @param pageRequest 分页请求
     * @param requestDTO  入参
     * @param schoolId    学校Id
     * @return IPage<SchoolStudentListResponseDTO>
     */
    public IPage<SchoolStudentListResponseDTO> getList(PageRequest pageRequest, SchoolStudentRequestDTO requestDTO, Integer schoolId) {
        return baseMapper.getList(pageRequest.toPage(), requestDTO, schoolId);
    }

    /**
     * 通过身份证和学号获取学生
     *
     * @param id       学生Id
     * @param idCard   身份证
     * @param sno      学号
     * @param schoolId 学校Id
     * @return List<SchoolStudent>
     */
    public List<SchoolStudent> getByIdCardAndSno(Integer id, String idCard, String sno, Integer schoolId) {
        return baseMapper.getByIdCardAndSno(id, idCard, sno, schoolId);
    }

    /**
     * 删除学生
     *
     * @param id 学生Id
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletedStudent(Integer id) {
        baseMapper.deletedStudent(id);
    }

    /**
     * 通过学生ids获取学校学生
     *
     * @param studentIds 学生ids
     * @return List<SchoolStudent>
     */
    public List<SchoolStudent> getByStudentIds(List<Integer> studentIds) {
        return baseMapper.selectList(Wrappers.lambdaQuery(SchoolStudent.class)
                .in(SchoolStudent::getStudentId,studentIds)
                .eq(SchoolStudent::getStatus,CommonConst.STATUS_NOT_DELETED));
    }

    /**
     * 通过学生id获取学校学生
     *
     * @param studentId 学生id
     * @return List<SchoolStudent>
     */
    public List<SchoolStudent> getByStudentId(Integer studentId) {
        return baseMapper.getByStudentId(studentId);
    }

    /**
     * 通过学生id和学校ID获取学校学生
     * @param studentId
     * @param schoolId
     * @param status
     */
    public SchoolStudent getByStudentIdAndSchoolId(Integer studentId,Integer schoolId,Integer status) {
        LambdaQueryWrapper<SchoolStudent> queryWrapper = Wrappers.lambdaQuery(SchoolStudent.class)
                .eq(SchoolStudent::getStudentId, studentId)
                .eq(SchoolStudent::getSchoolId,schoolId)
                .eq(SchoolStudent::getStatus, status);
        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 通过学校、班级Id获取学生
     *
     * @param schoolId 学校Id
     * @param gradeId  年级Id
     * @return List<SchoolStudent>
     */
    public List<SchoolStudent> getBySchoolIdAndGradeId(Integer schoolId, Integer gradeId) {
        return baseMapper.getBySchoolIdAndGradeId(schoolId, gradeId);
    }

    /**
     * 通过身份证或学号获取学生
     *
     * @param idCards  身份证
     * @param snos     学号
     * @param schoolId 学校Id
     * @return List<SchoolStudent>
     */
    public List<SchoolStudent> getAllStatusStudentByIdCardAndSnoAndPassport(List<String> idCards, List<String> snos, List<String> passports, Integer schoolId) {
        return baseMapper.getAllStatusStudentByIdCardAndSnoAndPassport(idCards, snos, passports, schoolId);
    }

    /**
     * 通过身份证获取学生(没删除的)
     *
     * @param idCards  身份证
     * @param schoolId 学校Id
     * @return List<SchoolStudent>
     */
    public List<SchoolStudent> getByIdCards(List<String> idCards, Integer schoolId) {
        return baseMapper.getByIdCards(idCards, schoolId);
    }


    /**
     * 学号、身份证是否重复
     *
     * @param id       id
     * @param idCard   身份证
     * @param sno      学号
     * @param schoolId 学校Id
     * @return true-没有重复 false-存在重复
     */
    public Boolean checkIdCardAndSno(Integer id, String idCard, String sno, Integer schoolId) {
        List<SchoolStudent> studentList = baseMapper.getByIdCardAndSno(id, idCard, sno, schoolId);
        return CollectionUtils.isEmpty(studentList);
    }

    /**
     * 获取已经删除的学生
     *
     * @param idCard   学生证
     * @param sno      学号
     * @param schoolId 学校Id
     * @return SchoolStudent
     */
    public SchoolStudent getDeletedByIdCardAndSno(String idCard, String sno, Integer schoolId) {
        return baseMapper.getDeletedByIdCardAndSno(idCard, sno, schoolId);
    }

    /**
     * 通过身份证获取已经删除的学生
     *
     * @param idCards   身份证
     * @param schoolId  学校Id
     * @param passports 护照
     * @return List<SchoolStudent>
     */
    public List<SchoolStudent> getDeletedByIdCard(List<String> idCards, List<String> passports, Integer schoolId) {
        if (CollectionUtils.isEmpty(idCards) && CollectionUtils.isEmpty(passports) ) {
            throw new BusinessException("身份证/护照不能为空");
        }
        return baseMapper.getDeletedByIdCardsAndPassports(idCards, passports, schoolId);
    }

    /**
     * 学号、身份证、护照是否重复
     *
     * @param id       id
     * @param idCard   身份证
     * @param sno      学号
     * @param passport 护照
     * @param schoolId 学校Id
     * @return true-没有重复 false-存在重复
     */
    public Boolean getByIdCardAndSnoAndPassport(Integer id, String idCard, String sno, String passport, Integer schoolId) {
        List<SchoolStudent> studentList = baseMapper.getByIdCardAndSnoAndPassport(id, idCard, sno, passport, schoolId);
        return CollectionUtils.isEmpty(studentList);
    }
    /**
     * 获取 学号、身份证、护照重复数据
     *
     * @param id       id
     * @param idCard   身份证
     * @param sno      学号
     * @param passport 护照
     * @param schoolId 学校Id
     */
    public List<SchoolStudent> listByIdCardAndSnoAndPassport(Integer id, String idCard, String sno, String passport, Integer schoolId) {
        return baseMapper.getByIdCardAndSnoAndPassport(id, idCard, sno, passport, schoolId);
    }
    /**
     * 通过身份证、护照获取学生信息
     *
     * @param idCard   身份证
     * @param passport 护照
     * @param schoolId 学校Id
     * @return true-没有重复 false-存在重复
     */
    public SchoolStudent getByIdCardAndPassport(String idCard, String passport, Integer schoolId) {
        return baseMapper.getByIdCardAndPassport(idCard, passport, schoolId);
    }

    /**
     * 通过身份证、学号、护照获取学生(包括删除的)
     *
     * @param idCards   身份证
     * @param snoList   学号
     * @param passports 护照
     * @param schoolId  学校Id
     * @return List<SchoolStudent>
     */
    public List<SchoolStudent> getAllByIdCardAndSnoAndPassports(List<String> idCards, List<String> snoList, List<String> passports, Integer schoolId) {
        return baseMapper.getAllByIdCardAndSnoAndPassports(idCards, snoList, passports, schoolId);
    }

    /**
     * 通过学生Ids删除学校学生
     * <p>删库操作，谨慎使用</p>
     *
     * @param studentIds 学生Id
     */
    public void deleteByStudentIds(List<Integer> studentIds) {
        if (CollectionUtils.isEmpty(studentIds)) {
            return;
        }
        baseMapper.deleteByStudentIds(studentIds);
    }

    /**
     * 通过学生ids获取学校学生
     *
     * @param studentIds 学生ids
     * @param schoolId   学校Id
     * @return List<SchoolStudent>
     */
    public List<SchoolStudent> getByStudentIdsAndSchoolId(List<Integer> studentIds, Integer schoolId) {
        if (CollectionUtils.isEmpty(studentIds)) {
            return Lists.newArrayList();
        }
        return baseMapper.getByStudentIdsAndSchoolId(studentIds, schoolId);
    }

    /**
     * 判断是否能删除学校端的学生
     *
     * @param schoolStudentMap 学校端学生集合
     * @param studentId        学生Id
     * @return true-能删除 fasle-不能删除
     */
    public boolean isCanDeletedSchoolStudent(Map<Integer, SchoolStudent> schoolStudentMap, Integer studentId) {
        SchoolStudent schoolStudent = schoolStudentMap.get(studentId);
        if (Objects.isNull(schoolStudent)) {
            return true;
        }
        return SourceClientEnum.SCREENING_PLAN.type.equals(schoolStudent.getSourceClient());
    }

    /**
     * 根据学校ID查询学生
     * @param schoolId 学校ID
     */
    public List<SchoolStudent> listBySchoolId(Integer schoolId) {
        return baseMapper.selectList(Wrappers.lambdaQuery(SchoolStudent.class)
                .eq(SchoolStudent::getSchoolId,schoolId)
                .eq(SchoolStudent::getStatus, CommonConst.STATUS_NOT_DELETED));
    }

    /**
     * 根据学校ID和年级ID获取学生集合
     * @param schoolId 学校ID
     * @param gradeIds 年级ID集合
     */
    public List<SchoolStudent> listBySchoolIdAndGradeIds(Integer schoolId,List<Integer> gradeIds){
        LambdaQueryWrapper<SchoolStudent> queryWrapper = Wrappers.lambdaQuery(SchoolStudent.class)
                .eq(SchoolStudent::getSchoolId, schoolId)
                .eq(SchoolStudent::getStatus,CommonConst.STATUS_NOT_DELETED)
                .in(SchoolStudent::getGradeId, gradeIds);
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 获取只有预警等级的学生
     *
     * @param schoolId   学校Id
     *
     * @return 学生
     */
    public List<SchoolStudent> getBySchoolIdAndVisionLabel(Integer schoolId) {
        return baseMapper.getBySchoolIdAndVisionLabel(schoolId);
    }

    /**
     * 根据条件查询学校学生
     * @param pageRequest
     * @param schoolStudentQueryBO
     */
    public IPage<SchoolStudent> listByCondition(PageRequest pageRequest, SchoolStudentQueryBO schoolStudentQueryBO) {
        LambdaQueryWrapper<SchoolStudent> queryWrapper = Wrappers.lambdaQuery(SchoolStudent.class)
                .ne(SchoolStudent::getStatus,CommonConst.STATUS_IS_DELETED)
                .eq(Objects.nonNull(schoolStudentQueryBO.getGradeId()), SchoolStudent::getGradeId, schoolStudentQueryBO.getGradeId())
                .eq(Objects.nonNull(schoolStudentQueryBO.getClassId()), SchoolStudent::getClassId, schoolStudentQueryBO.getClassId())
                .like(StrUtil.isNotBlank(schoolStudentQueryBO.getName()), SchoolStudent::getName, schoolStudentQueryBO.getName())
                .like(StrUtil.isNotBlank(schoolStudentQueryBO.getSno()), SchoolStudent::getSno, schoolStudentQueryBO.getSno())
                .eq(Objects.nonNull(schoolStudentQueryBO.getSchoolId()), SchoolStudent::getSchoolId, schoolStudentQueryBO.getSchoolId())
                .eq(Objects.nonNull(schoolStudentQueryBO.getGlassesType()),SchoolStudent::getGlassesType,schoolStudentQueryBO.getGlassesType())
                .eq(Objects.nonNull(schoolStudentQueryBO.getYear()),SchoolStudent::getParticularYear,schoolStudentQueryBO.getYear())
                .eq(Objects.nonNull(schoolStudentQueryBO.getMyopiaLevel()),SchoolStudent::getMyopiaLevel,schoolStudentQueryBO.getMyopiaLevel())
                .eq(Objects.nonNull(schoolStudentQueryBO.getHyperopiaLevel()),SchoolStudent::getHyperopiaLevel,schoolStudentQueryBO.getHyperopiaLevel())
                .eq(Objects.nonNull(schoolStudentQueryBO.getAstigmatismLevel()),SchoolStudent::getAstigmatismLevel,schoolStudentQueryBO.getAstigmatismLevel())
                .eq(Objects.nonNull(schoolStudentQueryBO.getRefractiveError()),SchoolStudent::getIsRefractiveError,schoolStudentQueryBO.getRefractiveError())
                .eq(Objects.nonNull(schoolStudentQueryBO.getAnisometropia()),SchoolStudent::getIsAnisometropia,schoolStudentQueryBO.getAnisometropia())
                .in(CollUtil.isNotEmpty(schoolStudentQueryBO.getVisionLabels()), SchoolStudent::getVisionLabel, schoolStudentQueryBO.getVisionLabels())
                .in(CollUtil.isNotEmpty(schoolStudentQueryBO.getLowVisionList()),SchoolStudent::getLowVision,schoolStudentQueryBO.getLowVisionList())
                .in(CollUtil.isNotEmpty(schoolStudentQueryBO.getGradeTypeList()),SchoolStudent::getGradeType,schoolStudentQueryBO.getGradeTypeList())
                .in(CollUtil.isNotEmpty(schoolStudentQueryBO.getMyopiaList()),SchoolStudent::getMyopiaLevel,schoolStudentQueryBO.getMyopiaList())
                .in(CollUtil.isNotEmpty(schoolStudentQueryBO.getHyperopiaList()),SchoolStudent::getHyperopiaLevel,schoolStudentQueryBO.getHyperopiaList())
                .in(CollUtil.isNotEmpty(schoolStudentQueryBO.getAstigmatismList()),SchoolStudent::getAstigmatismLevel,schoolStudentQueryBO.getAstigmatismList())
                .orderByDesc(SchoolStudent::getCreateTime);

        Page page = pageRequest.toPage();
        return baseMapper.selectPage(page,queryWrapper);
    }

    @Override
    public List<SchoolStudent> listByIds(Collection<? extends Serializable> idList) {
        return baseMapper.selectList(Wrappers.lambdaQuery(SchoolStudent.class)
                .in(SchoolStudent::getId,idList)
                .eq(SchoolStudent::getStatus,CommonConst.STATUS_NOT_DELETED));
    }

    /**
     * 统计学生人数
     *
     * @return List<StudentCountDTO>
     */
    public List<StudentCountDTO> countStudentBySchoolId(List<Integer> schoolIdList) {
        return baseMapper.countStudentBySchoolId(schoolIdList);
    }

    public List<SchoolStudent> getByIdCardsOrPassports(List<String> idCards, List<String> passports, Integer schoolId) {
        return baseMapper.getByIdCardsOrPassports(idCards, passports, schoolId);

    }

    public List<SchoolStudent> getBySnoList(List<String> snoList, Integer schoolId) {
        return baseMapper.selectList(Wrappers.lambdaQuery(SchoolStudent.class)
                .in(SchoolStudent::getSno, snoList)
                .eq(SchoolStudent::getSchoolId, schoolId)
                .eq(SchoolStudent::getStatus, CommonConst.STATUS_NOT_DELETED));
    }

}