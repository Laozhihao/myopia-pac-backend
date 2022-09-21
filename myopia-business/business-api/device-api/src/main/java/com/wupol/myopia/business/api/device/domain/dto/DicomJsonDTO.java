package com.wupol.myopia.business.api.device.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * DicomJsonDTO:
 *
 * @author Simple4H
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class DicomJsonDTO {

    /**
     * 批次号
     */
    @JsonProperty("StudyInstanceUID")
    private String batch;
}
