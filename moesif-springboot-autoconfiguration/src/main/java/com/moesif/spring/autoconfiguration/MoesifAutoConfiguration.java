package com.moesif.spring.autoconfiguration;

import com.moesif.servlet.MoesifFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

@Configuration
@EnableConfigurationProperties(MoesifProperties.class)
public class MoesifAutoConfiguration {

	@Bean
	@ConditionalOnWebApplication
	@ConditionalOnClass(Filter.class)
	Filter moesifFilter(MoesifProperties properties) {
		return new MoesifFilter(properties.getToken());
	}

}
