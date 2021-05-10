package com.wupol.myopia.business.management.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.business.management.domain.model.ScreeningNotice;
import com.wupol.myopia.business.management.domain.model.ScreeningNoticeDeptOrg;
import com.wupol.myopia.business.management.domain.query.ScreeningNoticeQuery;
import com.wupol.myopia.business.management.domain.vo.ScreeningNoticeVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 筛查通知通知到的部门或者机构表Mapper接口
 *
 * @author Alix
 * @Date 2021-01-20
 */
public interface ScreeningNoticeDeptOrgMapper extends BaseMapper<ScreeningNoticeDeptOrg> {

    IPage<ScreeningNoticeVo> selectPageByQuery(@Param("page") IPage<ScreeningNotice> page, @Param("param") ScreeningNoticeQuery query);

    Integer updateStatusAndTaskPlanIdByNoticeIdAndAcceptOrgId(@Param("screeningNoticeId") Integer noticeId, @Param("acceptOrgId") Integer acceptOrgId, @Param("screeningTaskPlanId") Integer genTaskOrPlanId, @Param("operatorId") Integer userId, @Param("operationStatus") Integer operationStatus);

    List<ScreeningNotice> selectByAcceptIdAndType(Integer type, Integer acceptOrgId);

    ScreeningNoticeDeptOrg getByNoticeIdAndOrgId(@Param("noticeId") Integer noticeId, @Param("orgId") Integer orgId);

    List<ScreeningNoticeDeptOrg> getByNoticeId(@Param("noticeId") Integer noticeId);
}
