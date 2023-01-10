package org.springframework.boot.actuate.autoconfigure.tracing.zipkin;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin2.reporter.Sender;

@Configuration(proxyBeanMethods = false)
public class Config {
    @Bean
    @ConditionalOnMissingBean(Sender.class)
    @ConditionalOnBean(ZipkinProperties.class)
    ZipkinRestTemplateSender restTemplateSender(ZipkinProperties properties,
                                                ObjectProvider<ZipkinRestTemplateBuilderCustomizer> customizers) {
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder()
            .setConnectTimeout(properties.getConnectTimeout())
//            .setReadTimeout(properties.getReadTimeout())
            ;
        customizers.orderedStream().forEach((c) -> c.customize(restTemplateBuilder));
        return new ZipkinRestTemplateSender(properties.getEndpoint(), restTemplateBuilder.build());
    }
}
