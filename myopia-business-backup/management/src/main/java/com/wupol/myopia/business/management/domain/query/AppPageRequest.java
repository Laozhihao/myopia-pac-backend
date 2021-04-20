package com.wupol.myopia.business.management.domain.query;

import lombok.Getter;
import lombok.Setter;

/**
 * 分页基类
 *
 * @author Jacob
 */
@Setter
@Getter
public class AppPageRequest {
    private Integer page;
    private Integer size;
}
