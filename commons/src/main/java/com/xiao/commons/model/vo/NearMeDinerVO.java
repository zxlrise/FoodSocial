package com.xiao.commons.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel(description = "附近的人")
@Getter
@Setter
public class NearMeDinerVO extends ShortDinerInfo {

    @ApiModelProperty(value = "距离", example = "98m")
    private String distance;

}