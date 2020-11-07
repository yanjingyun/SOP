package com.yjy.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

public class MyParam {
    private int id;

    @NotBlank(message = "name不能为空")
    @Length(max = 20, message = "name长度不能超过20")
    private String name;

    @Length(max = 64, message = "长度不能超过64")
    private String remark;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
