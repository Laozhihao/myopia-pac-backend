package com.wupol.myopia.business.hospital.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.hospital.domain.mapper.DepartmentMapper;
import com.wupol.myopia.business.hospital.domain.model.Department;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * 医院-科室
 * @author Chikong
 * @date 2021-02-10
 */
@Service
@Log4j2
public class DepartmentService extends BaseService<DepartmentMapper, Department> {

}