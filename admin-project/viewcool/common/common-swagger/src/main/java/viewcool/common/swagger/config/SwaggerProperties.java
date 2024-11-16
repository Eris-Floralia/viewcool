package viewcool.common.swagger.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置参数对象。
 *
 * @author eris
 * @date 2024-11-16
 */
@Data
@ConfigurationProperties("common-swagger")
public class SwaggerProperties {

	/**
	 * 是否开启Swagger。
	 */
	private Boolean enabled;

	/**
	 * Swagger解析的基础包路径。
	 **/
	private String basePackage = "";

	/**
	 * Swagger解析的服务包路径。
	 **/
	private String serviceBasePackage = "";

	/**
	 * ApiInfo中的标题。
	 **/
	private String title = "";

	/**
	 * ApiInfo中的描述信息。
	 **/
	private String description = "";

	/**
	 * ApiInfo中的版本信息。
	 **/
	private String version = "";
}
