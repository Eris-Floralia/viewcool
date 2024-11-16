package viewcool.common.core.util;

import groovy.lang.GroovyObject;
import org.springframework.stereotype.Component;

/**
 * Groovy脚本的执行器。
 *
 * @author eris
 * @date 2024-11-16
 */
@Component
public class GroovyExecutor {

    public String exec(String script, String method, Object[] args) {
        GroovyObject go = GroovyLoader.loadScript(script);
        return (String) go.invokeMethod(method, args);
    }
}
