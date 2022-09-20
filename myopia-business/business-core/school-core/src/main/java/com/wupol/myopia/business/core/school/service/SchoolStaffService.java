package com.wupol.myopia.business.core.school.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.school.domain.mapper.SchoolStaffMapper;
import com.wupol.myopia.business.core.school.domain.model.SchoolStaff;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author Simple4H
 */
@Service
public class SchoolStaffService extends BaseService<SchoolStaffMapper, SchoolStaff> {

    /**
     * 获取学校员工
     *
     * @param request  分页
     * @param schoolId 学校Id
     *
     * @return IPage<SchoolStaff>
     */
    public IPage<SchoolStaff> getSchoolStaff(PageRequest request, Integer schoolId) {
        return baseMapper.getSchoolStaff(request.toPage(), schoolId);
    }

    /**
     * 学校员工统计
     *
     * @param schoolId 学校Id
     *
     * @return 统计
     */
    public Integer countStaffBySchool(Integer schoolId) {
        return baseMapper.selectCount(new LambdaQueryWrapper<SchoolStaff>().eq(SchoolStaff::getSchoolId, schoolId));
    }

    /**
     * 检查手机号码重复
     *
     * @param idCard 身份证
     * @param id     id
     *
     * @return 是否重复
     */
    public boolean checkByIdCard(String idCard, Integer id) {
        return !baseMapper.selectList(new LambdaQueryWrapper<SchoolStaff>()
                .eq(SchoolStaff::getIdCard, idCard)
                .ne(Objects.nonNull(id), SchoolStaff::getId, id)).isEmpty();
    }

    /**
     * 检查手机号码重复
     *
     * @param phone 手机号码
     * @param id    id
     *
     * @return 是否重复
     */
    public boolean checkByPhone(String phone, Integer id) {
        return !baseMapper.selectList(new LambdaQueryWrapper<SchoolStaff>()
                .eq(SchoolStaff::getPhone, phone)
                .ne(Objects.nonNull(id), SchoolStaff::getId, id)).isEmpty();
    }

}
