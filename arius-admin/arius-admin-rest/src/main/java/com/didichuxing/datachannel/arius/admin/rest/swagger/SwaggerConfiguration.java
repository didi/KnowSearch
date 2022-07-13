package com.didichuxing.datachannel.arius.admin.rest.swagger;

import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Swagger配置类
 */
@Configuration
@EnableSwagger2
@NoArgsConstructor
public class SwaggerConfiguration {

    private static final ILog   LOGGER = LogFactory.getLog(SwaggerConfiguration.class);

    private static List<String> envs   = Lists.newArrayList();
    private final static String HEADER = "header";

    @Value("${swagger.enable:false}")
    private boolean             enable;
    @Value(value = "${admin.port.web}")
    private int                 port;

    @Bean
    public Docket createRestApi() {
        String envStr = String.join(",", Lists.newArrayList(envs));
        if (!envStr.contains(EnvUtil.EnvType.TEST.getStr()) && !envStr.contains(EnvUtil.EnvType.DEV.getStr())) {
            return new Docket(DocumentationType.SWAGGER_2).enable(enable);
        }

        LOGGER.info("class=SwaggerConfiguration||method=createRestApi||swagger started||env={}", envStr);

        List<ResponseMessage> responseMessageList = new ArrayList<>();
        responseMessageList.add(new ResponseMessageBuilder().code(200)
            .message("OK；服务正常返回或者异常都返回200，通过返回结构中的code来区分异常；code为0表示操作成功，其他为异常").build());
        responseMessageList.add(new ResponseMessageBuilder().code(404).message("资源找不到；请确认URL是否正确").build());

        return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo()).select()
            .apis(RequestHandlerSelectors.basePackage("com.didichuxing.datachannel.arius.admin"))
            .paths(PathSelectors.any()).build().globalOperationParameters(parameters()).useDefaultResponseMessages(true)
            .globalResponseMessage(RequestMethod.GET, responseMessageList)
            .globalResponseMessage(RequestMethod.POST, responseMessageList)
            .globalResponseMessage(RequestMethod.PUT, responseMessageList)
            .globalResponseMessage(RequestMethod.DELETE, responseMessageList);
    }

    private List<Parameter> parameters() {

        final Parameter user = new ParameterBuilder().name(HttpRequestUtil.USER).description("操作人")
            .modelRef(new ModelRef("string")).parameterType(HEADER).required(false).defaultValue("").build();
        final Parameter userId = new ParameterBuilder().name(HttpRequestUtil.USER_ID).description("操作人id")
            .modelRef(new ModelRef("integer")).parameterType(HEADER).required(false).build();
        final Parameter projectId = new ParameterBuilder().name(HttpRequestUtil.PROJECT_ID)
            .description("项目id:在项目创建后，各个接口都需要带此header").modelRef(new ModelRef("integer")).parameterType(HEADER)
            .required(false).build();

        return Lists.newArrayList(user, userId, projectId);
    }

    /**
     * 创建该Api的基本信息（这些基本信息会展现在文档页面中）
     *
     * @return
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("AriusAdmin接口文档").description(String.format("当前环境:%s", envs))
            .version("1.0_" + String.join(",", envs)).build();
    }

    public static void initEnv(String[] args) {
        if (args == null) {
            return;
        }

        try {
            for (String arg : args) {
                String[] argArr = arg.split("=");
                if (argArr[0].toLowerCase().contains("spring.profiles.active")) {
                    envs.add(argArr[1]);
                }
            }
            LOGGER.info("class=SwaggerConfiguration||method=initEnv||swagger||env={}", envs);
        } catch (Exception e) {
            LOGGER.warn("class=SwaggerConfiguration||method=initEnv||initEnv error||args={}", args, e);
        }
    }

}