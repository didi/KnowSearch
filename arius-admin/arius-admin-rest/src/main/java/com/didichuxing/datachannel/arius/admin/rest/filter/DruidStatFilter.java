package com.didichuxing.datachannel.arius.admin.rest.filter;

import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;

import com.alibaba.druid.support.http.WebStatFilter;

/**
 * druid 监控入口filter
 * 
 * @author jinbinbin
 * @version $Id: DruidStatFilter.java, v 0.1 2018年09月26日 16:12 jinbinbin Exp $
 */
@WebFilter(filterName = "druidWebStatFilter", urlPatterns = { "/*" }, initParams = { @WebInitParam(name = "exclusions", value = "*.js,*.jpg,*.png,*.gif,*.ico,*.css,/druid/*") })
public class DruidStatFilter extends WebStatFilter {

}
