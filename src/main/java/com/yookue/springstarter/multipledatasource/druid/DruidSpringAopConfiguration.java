/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yookue.springstarter.multipledatasource.druid;


import javax.annotation.Nonnull;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.aop.support.RegexpMethodPointcutAdvisor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import com.alibaba.druid.support.spring.stat.DruidStatInterceptor;


/**
 * Configuration for Spring AOP of druid
 *
 * @author <a href="mailto:89921218@qq.com">lihengming</a>
 * @author David Hsing
 */
@ConditionalOnProperty(name = "spring.datasource.druid.aop-patterns")
public class DruidSpringAopConfiguration {
    public static final String STAT_INTERCEPTOR_ADVICE = "druidStatInterceptorAdvice";    // $NON-NLS-1$
    public static final String METHOD_POINTCUT_ADVISOR = "druidMethodPointcutAdvisor";    // $NON-NLS-1$
    public static final String ADVISOR_PROXY_CREATOR = "druidAdvisorProxyCreator";    // $NON-NLS-1$

    @Bean(name = STAT_INTERCEPTOR_ADVICE)
    @ConditionalOnMissingBean(name = STAT_INTERCEPTOR_ADVICE)
    public Advice statInterceptorAdvice() {
        return new DruidStatInterceptor();
    }

    @Bean(name = METHOD_POINTCUT_ADVISOR)
    @ConditionalOnBean(name = STAT_INTERCEPTOR_ADVICE, value = DruidStatProperties.class)
    @ConditionalOnMissingBean(name = METHOD_POINTCUT_ADVISOR)
    public Advisor methodPointcutAdvisor(@Qualifier(value = STAT_INTERCEPTOR_ADVICE) @Nonnull Advice advice, @Nonnull DruidStatProperties properties) {
        return new RegexpMethodPointcutAdvisor(properties.getAopPatterns(), advice);
    }

    @Bean(name = ADVISOR_PROXY_CREATOR)
    @ConditionalOnMissingBean(name = ADVISOR_PROXY_CREATOR)
    @ConditionalOnProperty(name = "spring.aop.auto", havingValue = "false")
    public DefaultAdvisorAutoProxyCreator advisorProxyCreator() {
        DefaultAdvisorAutoProxyCreator creator = new DefaultAdvisorAutoProxyCreator();
        creator.setProxyTargetClass(true);
        return creator;
    }
}
