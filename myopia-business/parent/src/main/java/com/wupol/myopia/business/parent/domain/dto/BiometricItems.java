package com.wupol.myopia.business.parent.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 生物测量
 *
 * @author Simple4H
 */
@Getter
@Setter
public class BiometricItems {

    /**
     * 标题
     */
    private String title;

    /**
     * 右眼
     */
    private Item od;

    /**
     * 左眼
     */
    private Item os;

    @Getter
    @Setter
    public static class Item {

        /**
         * 类型
         */
        private String data;

        private List<String> eyeDiseases;
    }
}
