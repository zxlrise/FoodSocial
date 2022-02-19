package com.xiao.commons.model.pojo;

import com.xiao.commons.model.base.BaseModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DinerPoints extends BaseModel {

    @ApiModelProperty("关联DinerId")
    private Integer fkDinerId;
    @ApiModelProperty("积分")
    private Integer points;
    @ApiModelProperty(name = "类型",example = "0=签到，1=关注好友，2=添加Feed，3=添加商户评论")
    private Integer types;

}