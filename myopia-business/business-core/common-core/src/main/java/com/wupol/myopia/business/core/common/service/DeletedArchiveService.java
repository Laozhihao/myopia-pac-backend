package com.wupol.myopia.business.core.common.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.common.domain.mapper.DeletedArchiveMapper;
import com.wupol.myopia.business.core.common.domain.model.DeletedArchive;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * 删除信息归档
 */
@Service
@Log4j2
public class DeletedArchiveService extends BaseService<DeletedArchiveMapper, DeletedArchive> {

}
