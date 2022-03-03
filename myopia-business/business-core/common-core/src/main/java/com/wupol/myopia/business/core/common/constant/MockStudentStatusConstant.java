package com.wupol.myopia.business.core.common.constant;

/**
 * Mock计划学生的状态
 *
 * @Author Jacob
 * @Date 2022-02-20
 */
public interface MockStudentStatusConstant {
    /**
     * 模拟出来的数据,已经被初始化过(目前初始化的定义是: Mock出来的计划学生的ID或者Passport被补充)
     */
    int INITIALIZED_MOCK = 1;
    /**
     * 模拟出来的数据
     */
    int MOCK = 0;
    /**
     * 非模拟出来的数据
     */
    int NOT_MOCK = -1;
}
