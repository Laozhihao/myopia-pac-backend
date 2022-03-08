package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.aggregation.screening.service.ScreeningPlanStudentBizService;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.school.domain.dto.MockPlanStudentQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningStudentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Classname MockPlanStudentController
 * @Description
 * @Date 2022/2/22 4:44 下午
 * @Author Jacob
 * @Version
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/mockPlanStudent")
public class MockPlanStudentController {

    @Autowired
    protected ScreeningPlanStudentBizService screeningPlanStudentBizService;

    /**
     * 获取计划学生列表
     *
     * @param pageRequest             分页查询
     * @param mockPlanStudentQueryDTO 请求条件
     * @return 学生列表
     */
    @GetMapping("list")
    public IPage<ScreeningStudentDTO> getStudentsList(PageRequest pageRequest, MockPlanStudentQueryDTO mockPlanStudentQueryDTO) {
        return screeningPlanStudentBizService.getMockPlanStudentList(pageRequest, mockPlanStudentQueryDTO);
    }
}
