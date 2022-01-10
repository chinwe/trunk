package com.learn.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * @author chinwe
 * 2021/12/26
 */
@Component
@Aspect
public class UserDaoEnhance {

    @Pointcut("execution(* com.learn.aop.impl.UserDaoImpl.add(..))")
    private void pointcut() {
    }

    @Before(value = "pointcut()")
    public void before() {
        System.out.println("Before Advice");
    }

    @AfterReturning(pointcut = "pointcut()",
        returning = "retVal")
    public void afterAddReturning(int retVal) {
        System.out.println("After Returning Advice");
    }

    @AfterThrowing("pointcut()")
    public void doRecoveryActions() {
        System.out.println("After Throwing Advice");
    }

    @After("pointcut()")
    public void doReleaseLock() {
        System.out.println("After (Finally) Advice");
    }

    @Around("pointcut()")
    public Object doBasicProfiling(ProceedingJoinPoint pjp) throws Throwable {
        // start stopwatch
        Object retVal = pjp.proceed();
        // stop stopwatch
        return retVal;
    }
}
