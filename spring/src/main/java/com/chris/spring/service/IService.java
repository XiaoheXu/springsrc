package com.chris.spring.service;

import com.chris.spring.annotation.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 服务提供接口，用来定义需要提供的服务
 */

public interface IService {
    public void insert(HttpServletRequest request, HttpServletResponse response) throws IOException;
    public void query(HttpServletRequest request, HttpServletResponse response) throws IOException;
    public void remove(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
