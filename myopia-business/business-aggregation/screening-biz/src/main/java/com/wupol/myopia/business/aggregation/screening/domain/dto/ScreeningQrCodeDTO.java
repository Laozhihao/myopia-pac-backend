package com.wupol.myopia.business.aggregation.screening.domain.dto;

import com.wupol.myopia.business.common.utils.domain.model.NotificationConfig;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningStudentDTO;
import lombok.Data;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 钓猫的小鱼
 * @Date: 2022/03/11/19:02
 * @Description:
 */
@Data
public class ScreeningQrCodeDTO {

    private NotificationConfig notificationConfig;

    private List<ScreeningStudentDTO> students;

    private String classDisplay;

    private String schoolName;

    private String qrCodeFile;
}
