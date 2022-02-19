package com.xiao.commons.model.pojo;

import com.xiao.commons.model.base.BaseModel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(description = "餐厅评论实体类")
public class Reviews extends BaseModel {

    @ApiModelProperty("评论餐厅主键")
    private Integer fkRestaurantId;
    @ApiModelProperty("评论内容")
    private String content;
    @ApiModelProperty("评论食客主键")
    private Integer fkDinerId;
    @ApiModelProperty(value = "是否喜欢", example = "0=不喜欢，1=喜欢")
    private int likeIt;

}