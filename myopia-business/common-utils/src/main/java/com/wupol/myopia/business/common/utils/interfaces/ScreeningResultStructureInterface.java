package com.wupol.myopia.business.common.utils.interfaces;

/**
 * @Description
 * @Date 2021/2/8 21:31
 * @Author by Jacob
 */
public interface ScreeningResultStructureInterface<T extends ValidResultDataInterface> {

    T getLeftEyeData() ;

    T getRightEyeData() ;

    default boolean judgeValidData() {
        if (getLeftEyeData() == null || getRightEyeData() == null) {
            return false;
        }
        return getLeftEyeData().judgeValidData() && getRightEyeData().judgeValidData();
    }
}
