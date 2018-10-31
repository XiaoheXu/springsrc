package com.chris.spring.service;

import com.chris.spring.annotation.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Service("service")
public class ServiceImpl implements IService {
    @Override
    public void insert(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.getWriter().write("data insert success!");
    }

    @Override
    public void query(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.getWriter().write("data query success!");
    }

    @Override
    public void remove(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.getWriter().write("data is removed!");
    }
}
