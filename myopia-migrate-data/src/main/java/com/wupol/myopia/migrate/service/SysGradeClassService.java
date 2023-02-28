package com.wupol.myopia.migrate.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.migrate.domain.mapper.SysGradeClassMapper;
import com.wupol.myopia.migrate.domain.model.SysGradeClass;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author HaoHao
 * @Date 2022-03-23
 */
@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
@DS("data_source_db")
@Service
public class SysGradeClassService extends BaseService<SysGradeClassMapper, SysGradeClass> {

}
