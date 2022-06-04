package com.mingyang.bootlaunch.common.logs;

import com.alibaba.fastjson.JSONObject;
import eu.bitwalker.useragentutils.UserAgent;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;

@Aspect
@Component
public class AopLog {
	private static final Logger logger = LoggerFactory.getLogger(AopLog.class);
	private static final String START_TIME = "request-start";

	/**
	 * 切入点
	 */
	@Pointcut("execution(public * com.mingyang.bootlaunch.controller.*.*(..))")
	public void log() {

	}

	/**
	 * 前置操作
	 *
	 * @param point 切入点
	 */
	@Before("log()")
	public void beforeLog(JoinPoint point) {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

		HttpServletRequest request = Objects.requireNonNull(attributes).getRequest();

		logger.info("【请求 URL】：{}", request.getRequestURL());
		logger.info("【请求 IP】：{}", request.getRemoteAddr());
		logger.info("【请求类名】：{}，【请求方法名】：{}", point.getSignature().getDeclaringTypeName(), point.getSignature().getName());

		Map<String, String[]> parameterMap = request.getParameterMap();
		logger.info("【请求参数】：{}，", JSONObject.toJSONString(parameterMap));
		Long start = System.currentTimeMillis();
		request.setAttribute(START_TIME, start);
	}

	/**
	 * 环绕操作
	 *
	 * @param point 切入点
	 * @return 原方法返回值
	 * @throws Throwable 异常信息
	 */
	@Around("log()")
	public Object aroundLog(ProceedingJoinPoint point) throws Throwable {
		Object result = point.proceed();
		logger.info("【返回值】：{}", JSONObject.toJSONString(result));
		return result;
	}

	/**
	 * 后置操作
	 */
	@AfterReturning("log()")
	public void afterReturning() {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = Objects.requireNonNull(attributes).getRequest();

		Long start = (Long) request.getAttribute(START_TIME);
		Long end = System.currentTimeMillis();
		logger.info("【请求耗时】：{}毫秒", end - start);

		String header = request.getHeader("User-Agent");
		UserAgent userAgent = UserAgent.parseUserAgentString(header);
		logger.info("【浏览器类型】：{}，【操作系统】：{}，【原始User-Agent】：{}", userAgent.getBrowser().toString(), userAgent.getOperatingSystem().toString(), header);
	}
}
