package com.chris.spring.action;

import com.chris.spring.annotation.Autowired;
import com.chris.spring.annotation.Controller;
import com.chris.spring.annotation.RequestMapping;
import com.chris.spring.service.IService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 模拟删除数据
 */
@Controller
@RequestMapping("/chris")
public class RemoveAction {

    @Autowired("service")
    private IService service;

    @RequestMapping("/remove")
    public void remove(HttpServletRequest request, HttpServletResponse response) throws IOException {
        service.remove(request, response);
    }
}
