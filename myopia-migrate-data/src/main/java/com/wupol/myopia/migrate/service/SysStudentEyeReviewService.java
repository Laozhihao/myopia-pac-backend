package com.wupol.myopia.migrate.service;

import com.wupol.myopia.migrate.domain.model.SysStudentEye;
import com.wupol.myopia.migrate.domain.model.SysStudentEyeReview;
import com.wupol.myopia.migrate.domain.mapper.SysStudentEyeReviewMapper;
import com.wupol.myopia.base.service.BaseService;
import org.springframework.stereotype.Service;

/**
 * @Author lzh
 * @Date 2023-06-19
 */
@Service
public class SysStudentEyeReviewService extends BaseService<SysStudentEyeReviewMapper, SysStudentEyeReview> {

    /**
     * 获取一条学生复测数据
     *
     * @param sysStudentEyeReview   查询参数
     * @return SysStudentEye
     */
    public SysStudentEye getOneStudentReview(SysStudentEyeReview sysStudentEyeReview) {
        return baseMapper.getOneStudentReview(sysStudentEyeReview);
    }

}
