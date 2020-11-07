package com.gitee.sop.websiteserver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author tanghc
 */
@Controller
public class IndexController {

    @RequestMapping("/")
    public String index() {
        return "redirect:index.html";
    }

}
