package com.wupol.myopia.migrate.domain.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.migrate.domain.model.SysStudentEye;
import com.wupol.myopia.migrate.domain.model.SysStudentEyeSimple;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 视力筛查Mapper接口
 *
 * @Author HaoHao
 * @Date 2022-03-23
 */
@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
@DS("data_source_db")
public interface SysStudentEyeMapper extends BaseMapper<SysStudentEye> {

    /**
     * 获取简单版筛查数据
     *
     * @param deptId 筛查机构ID
     * @return java.util.List<com.wupol.myopia.migrate.domain.model.SysStudentEyeSimple>
     **/
    List<SysStudentEyeSimple> getSimpleDataList(String deptId);
}
