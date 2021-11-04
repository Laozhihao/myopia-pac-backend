package com.wupol.myopia.business.core.school.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentListResponseDTO;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentRequestDTO;
import com.wupol.myopia.business.core.school.management.domain.mapper.SchoolStudentMapper;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

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
     * @return IPage<SchoolStudentListResponseDTO>
     */
    public IPage<SchoolStudentListResponseDTO> getList(PageRequest pageRequest, SchoolStudentRequestDTO requestDTO) {
        return baseMapper.getList(pageRequest.toPage(), requestDTO);
    }

    /**
     * 通过身份证和学号获取学生
     *
     * @param id     学生Id
     * @param idCard 身份证
     * @param sno    学号
     * @return List<SchoolStudent>
     */
    public List<SchoolStudent> getByIdCardAndSno(Integer id, String idCard, String sno) {
        return baseMapper.getByIdCardAndSno(id, idCard, sno);
    }
}