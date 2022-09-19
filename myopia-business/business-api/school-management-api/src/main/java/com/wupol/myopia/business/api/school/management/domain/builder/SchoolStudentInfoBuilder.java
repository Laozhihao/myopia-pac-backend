package com.wupol.myopia.business.api.school.management.domain.builder;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.domain.vo.FamilyInfoVO;
import com.wupol.myopia.business.api.school.management.domain.dto.StudentBaseInfoDTO;
import com.wupol.myopia.business.api.school.management.domain.vo.StudentBaseInfoVO;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import lombok.experimental.UtilityClass;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 学校学生信息构建
 *
 * @author hang.yuan 2022/9/18 17:53
 */
@UtilityClass
public class SchoolStudentInfoBuilder {

    /**
     * 构建学生基础信息
     * @param schoolStudent 学校学生信息
     */
    public StudentBaseInfoVO buildStudentBaseInfoVO(SchoolStudent schoolStudent, List<District> districtPositionDetail) {
        StudentBaseInfoVO studentBaseInfoVO = new StudentBaseInfoVO()
                .setId(schoolStudent.getId())
                .setRecordNo(schoolStudent.getRecordNo())
                .setStudentId(schoolStudent.getStudentId())
                .setSno(schoolStudent.getSno())
                .setName(schoolStudent.getName())
                .setIdCard(schoolStudent.getIdCard())
                .setPassport(schoolStudent.getPassport())
                .setBirthday(schoolStudent.getBirthday())
                .setSchoolId(schoolStudent.getSchoolId())
                .setAddress(schoolStudent.getAddress())
                .setNation(schoolStudent.getNation())
                .setGender(schoolStudent.getGender())
                .setParentPhone(schoolStudent.getParentPhone())
                .setGradeId(schoolStudent.getGradeId())
                .setClassId(schoolStudent.getClassId())
                .setCommitteeCode(schoolStudent.getCommitteeCode())
                .setIsNewbornWithoutIdCard(schoolStudent.getIsNewbornWithoutIdCard());

        studentBaseInfoVO.setProvinceCode(schoolStudent.getProvinceCode())
                .setCityCode(schoolStudent.getCityCode())
                .setAreaCode(schoolStudent.getAreaCode())
                .setTownCode(schoolStudent.getTownCode());

        FamilyInfoVO familyInfo = schoolStudent.getFamilyInfo();
        if (Objects.nonNull(familyInfo) && CollUtil.isNotEmpty(familyInfo.getMember())){
            List<FamilyInfoVO.MemberInfo> memberList = familyInfo.getMember();
            studentBaseInfoVO.setFatherInfo(memberList.get(0)).setMotherInfo(memberList.get(1));
        }

        if (Objects.nonNull(schoolStudent.getCommitteeCode())) {
            studentBaseInfoVO.setCommitteeLists(districtPositionDetail);
        }
        return studentBaseInfoVO;
    }

    /**
     * 构建学校学生信息
     * @param studentBaseInfoDTO 修改学生信息
     */
    public void changeSchoolStudent(SchoolStudent schoolStudent, StudentBaseInfoDTO studentBaseInfoDTO, SchoolGrade schoolGrade, SchoolClass schoolClass) {
        schoolStudent.setStudentId(studentBaseInfoDTO.getStudentId())
                .setSchoolId(studentBaseInfoDTO.getSchoolId())
                .setSno(studentBaseInfoDTO.getSno())
                .setGradeId(studentBaseInfoDTO.getGradeId())
                .setGradeName(schoolGrade.getName())
                .setGradeType(GradeCodeEnum.getByCode(schoolGrade.getGradeCode()).getType())
                .setClassId(studentBaseInfoDTO.getClassId())
                .setClassName(schoolClass.getName())
                .setName(studentBaseInfoDTO.getName())
                .setGender(studentBaseInfoDTO.getGender())
                .setBirthday(studentBaseInfoDTO.getBirthday())
                .setNation(studentBaseInfoDTO.getNation())
                .setIdCard(studentBaseInfoDTO.getIdCard())
                .setParentPhone(studentBaseInfoDTO.getParentPhone())
                .setAddress(studentBaseInfoDTO.getAddress())
                .setPassport(studentBaseInfoDTO.getPassport())
                .setCommitteeCode(studentBaseInfoDTO.getCommitteeCode())
                .setRecordNo(studentBaseInfoDTO.getRecordNo())
                .setIsNewbornWithoutIdCard(studentBaseInfoDTO.getIsNewbornWithoutIdCard())
                .setUpdateTime(new Date());

        schoolStudent.setProvinceCode(studentBaseInfoDTO.getProvinceCode())
                .setCityCode(studentBaseInfoDTO.getCityCode())
                .setAreaCode(studentBaseInfoDTO.getAreaCode())
                .setTownCode(studentBaseInfoDTO.getTownCode());

        FamilyInfoVO familyInfo = new FamilyInfoVO();
        List<FamilyInfoVO.MemberInfo> member = Lists.newArrayList();
        if (Objects.nonNull(studentBaseInfoDTO.getFatherInfo())){
            member.add(studentBaseInfoDTO.getFatherInfo());
        }
        if (Objects.nonNull(studentBaseInfoDTO.getMotherInfo())){
            member.add(studentBaseInfoDTO.getMotherInfo());
        }
        if (CollUtil.isNotEmpty(member)){
            familyInfo.setMember(member);
            schoolStudent.setFamilyInfo(familyInfo);
        }
    }
}
