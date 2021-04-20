package com.wupol.myopia.business.aggregation.student.service;

import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.system.service.ResourceFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author HaoHao
 * @Date 2021/4/20
 **/
@Service
public class StudentFacade {

    @Autowired
    private ResourceFileService resourceFileService;
    @Autowired
    private StudentService studentService;


}
