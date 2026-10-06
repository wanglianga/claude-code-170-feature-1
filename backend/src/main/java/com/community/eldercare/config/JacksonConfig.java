package com.community.eldercare.config;

import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 让 Jackson 正确序列化 JPA 懒加载代理（避免 ByteBuddyInterceptor 报错）。
 * open-in-view 开启时，序列化阶段会话仍在，允许懒加载属性输出。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Hibernate6Module hibernate6Module() {
        Hibernate6Module module = new Hibernate6Module();
        module.configure(Hibernate6Module.Feature.FORCE_LAZY_LOADING, true);
        module.configure(Hibernate6Module.Feature.USE_TRANSIENT_ANNOTATION, false);
        return module;
    }
}
