package com.wupol.myopia.business.management.domain.query;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 分页基类
 *
 * @author Simple4H
 */
@Setter
@Getter
@Accessors(chain = true)
public class PageRequest {

    private Integer current;

    private Integer size;

    public Page<?> toPage() {
        if (null == current) {
            current = 1;
        }
        if (null == size) {
            size = 10;
        }
        return new Page<>(getCurrent(), getSize());
    }
}
