package com.didichuxing.datachannel.arius.admin.rest.servlet;

/**
 * 
 * druid 监控入口servlet
 * 
 * @author jinbinbin
 * @version $Id: DruidStatViewServlet.java, v 0.1 2018年09月26日 16:14 jinbinbin Exp $
 */

import javax.servlet.Servlet;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

import com.alibaba.druid.support.http.StatViewServlet;

@WebServlet(urlPatterns = { "/druid/*" }, initParams = {
                                                         // @WebInitParam(name = "allow", value = "127.0.0.1"),
                                                         @WebInitParam(name = "loginUsername", value = "druid"),
                                                         @WebInitParam(name = "loginPassword", value = "druid"),
                                                         @WebInitParam(name = "resetEnable", value = "false") })
public class DruidStatViewServlet extends StatViewServlet implements Servlet {
}
