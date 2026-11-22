package com.sm.product_service.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

	private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

	/**
	 * Logs service method entry, successful completion, execution time, and
	 * failures.
	 */
	@Around("execution(* com.sm.product_service.services..*(..))")
	public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {

		String className = joinPoint.getTarget().getClass().getSimpleName();
		String methodName = joinPoint.getSignature().getName();

		long startTime = System.nanoTime();

		log.info("Entering {}.{}", className, methodName);

		try {
			Object result = joinPoint.proceed();

			long executionTime = (System.nanoTime() - startTime) / 1_000_000;

			log.info("Completed {}.{} | Execution time: {} ms", className, methodName, executionTime);

			return result;

		} catch (Throwable ex) {

			long executionTime = (System.nanoTime() - startTime) / 1_000_000;

			log.warn("Failed {}.{} | Execution time: {} ms | Exception: {}", className, methodName, executionTime,
					ex.getClass().getSimpleName());

			throw ex;
		}
	}
}