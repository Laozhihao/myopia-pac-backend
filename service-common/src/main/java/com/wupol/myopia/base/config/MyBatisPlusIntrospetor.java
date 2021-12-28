package com.wupol.myopia.base.config;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

import java.util.Objects;

/**
 * @Author wulizhou
 * @Date 2021/12/22 14:49
 */
public class MyBatisPlusIntrospetor extends JacksonAnnotationIntrospector {

    /**
     * 序列化时去除标记@TableField(exist = false)的字段
     * @param m
     * @return
     */
    @Override
    public boolean hasIgnoreMarker(AnnotatedMember m) {
        boolean hasIgnoreMarker = super.hasIgnoreMarker(m);
        if(!hasIgnoreMarker) {
            TableField anno = _findAnnotation(m, TableField.class);
            if (Objects.nonNull(anno) && !anno.exist()) {
                return true;
            }
        }
        return hasIgnoreMarker;
    }

}
