package com.yjy.controller;

import com.gitee.sop.servercommon.annotation.BizCode;
import com.gitee.sop.servercommon.annotation.Open;
import com.yjy.vo.MyParam;
import com.yjy.vo.MyVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 签名验证通过后，到达这里进行具体的业务处理。
 */
@RestController
@RequestMapping("mydemo")
@Slf4j
@Api(tags = "我的Demo接口")
public class MyTestController {

    @Value("${server.port}")
    private int port;

    // http://localhost:2222/mydemo/get
    // 原生的接口，可正常调用
    @RequestMapping("/get")
    public MyVo get() {
        MyVo result = new MyVo();
        result.setId(1L);
        result.setName("TestAA");
        result.setAge(33);
        return result;
    }

    // 基础用法
    @ApiOperation(value = "我的信息", notes = "获取我的详细信息")
    @Open(value = "mydemo.get", bizCode = {
            // 定义业务错误码，用于文档显示
            @BizCode(code = "100001", msg = "姓名错误", solution = "填写正确的姓名"),
            @BizCode(code = "100002", msg = "备注错误", solution = "填写正确备注"),
    })
    @RequestMapping("/get/v1")
    public MyVo get_v1(MyParam param) {
        MyVo story = new MyVo();
        story.setId(1L);
        story.setName("海底小纵队(mydemo.get1.0), " + "param:" + param + ", port:" + port);
        story.setAge(22);
        return story;
    }
}