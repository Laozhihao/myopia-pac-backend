package com.wupol.myopia.migrate.domain.mapper;

import com.wupol.myopia.migrate.domain.model.SysStudentEye;
import com.wupol.myopia.migrate.domain.model.SysStudentEyeReview;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 视力复测Mapper接口
 *
 * @Author lzh
 * @Date 2023-06-19
 */
public interface SysStudentEyeReviewMapper extends BaseMapper<SysStudentEyeReview> {

    /**
     * 获取一条学生复测数据
     *
     * @param sysStudentEyeReview   查询参数
     * @return SysStudentEye
     */
    SysStudentEye getOneStudentReview(SysStudentEyeReview sysStudentEyeReview);

}
