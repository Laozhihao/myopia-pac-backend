package com.wupol.myopia.business.core.school.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.school.domain.mapper.SchoolStaffMapper;
import com.wupol.myopia.business.core.school.domain.model.SchoolStaff;
import org.springframework.stereotype.Service;

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

}
