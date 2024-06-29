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


import java.util.Collections;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import com.alibaba.druid.support.http.StatViewServlet;
import com.yookue.commonplexus.javaseutil.util.StringUtilsWraps;


/**
 * Configuration for druid stat view
 *
 * @author <a href="mailto:89921218@qq.com">lihengming</a>
 * @author David Hsing
 */
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(name = "spring.datasource.druid.stat-view-servlet.enabled", havingValue = "true")
public class DruidStatViewServletConfiguration {
    public static final String STAT_VIEW_SERVLET = "druidStatViewServletRegistration";    // $NON-NLS-1$
    private static final String DEFAULT_ALLOW_IP = "127.0.0.1";    // $NON-NLS-1$

    @Bean(name = STAT_VIEW_SERVLET)
    @ConditionalOnBean(value = DruidStatProperties.class)
    @ConditionalOnMissingBean(value = StatViewServlet.class, parameterizedContainer = ServletRegistrationBean.class)
    public ServletRegistrationBean<StatViewServlet> statViewServletRegistration(@Nonnull DruidStatProperties properties) {
        ServletRegistrationBean<StatViewServlet> result = new ServletRegistrationBean<>(new StatViewServlet());
        result.setServlet(new StatViewServlet());
        DruidStatProperties.StatViewServlet servlet = properties.getStatViewServlet();

        // David Hsing modified on 2021-08-12
        StringUtilsWraps.ifNotBlank(servlet.getUrlPattern(), element -> result.setUrlMappings(Collections.singleton(element)));

        result.addInitParameter("allow", StringUtils.defaultIfBlank(servlet.getAllow(), DEFAULT_ALLOW_IP));    // $NON-NLS-1$
        if (StringUtils.isNotBlank(servlet.getDeny())) {
            result.addInitParameter("deny", servlet.getDeny());    // $NON-NLS-1$
        }
        if (StringUtils.isNotBlank(servlet.getLoginUsername())) {
            result.addInitParameter("loginUsername", servlet.getLoginUsername());    // $NON-NLS-1$
        }
        if (StringUtils.isNotBlank(servlet.getLoginPassword())) {
            result.addInitParameter("loginPassword", servlet.getLoginPassword());    // $NON-NLS-1$
        }
        if (StringUtils.isNotBlank(servlet.getResetEnable())) {
            result.addInitParameter("resetEnable", servlet.getResetEnable());    // $NON-NLS-1$
        }
        return result;
    }
}
