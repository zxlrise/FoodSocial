package com.xiao.commons.model.pojo;

import com.xiao.commons.model.base.BaseModel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(description = "Feed信息类")
public class Feeds extends BaseModel {

    @ApiModelProperty("内容")
    private String content;
    @ApiModelProperty("食客")
    private Integer fkDinerId;
    @ApiModelProperty("点赞")
    private int praiseAmount;
    @ApiModelProperty("评论")
    private int commentAmount;
    @ApiModelProperty("关联的餐厅")
    private Integer fkRestaurantId;

}