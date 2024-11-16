package viewcool.common.datafilter.aop;

import viewcool.common.core.object.GlobalThreadLocal;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 禁用Mybatis拦截器数据过滤的AOP处理类。
 *
 * @author eris
 * @date 2024-11-16
 */
@Aspect
@Component
@Order(1)
@Slf4j
public class DisableDataFilterAspect {

    /**
     * 所有标记了DisableDataFilter注解的类和方法。
     */
    @Pointcut("@within(viewcool.common.core.annotation.DisableDataFilter) " +
            "|| @annotation(viewcool.common.core.annotation.DisableDataFilter)")
    public void disableDataFilterPointCut() {
        // 空注释，避免sonar警告
    }

    @Around("disableDataFilterPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        boolean dataFilterEnabled = GlobalThreadLocal.setDataFilter(false);
        try {
            return point.proceed();
        } finally {
            GlobalThreadLocal.setDataFilter(dataFilterEnabled);
        }
    }
}
