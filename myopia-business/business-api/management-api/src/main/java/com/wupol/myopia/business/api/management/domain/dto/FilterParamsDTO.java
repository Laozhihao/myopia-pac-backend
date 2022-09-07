package com.wupol.myopia.business.api.management.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 过滤参数
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class FilterParamsDTO<T, V> implements Serializable {
    private T id;
    private V name;
}
