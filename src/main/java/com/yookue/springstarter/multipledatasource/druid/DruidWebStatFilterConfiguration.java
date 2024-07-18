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
import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import com.alibaba.druid.support.jakarta.WebStatFilter;
import com.yookue.commonplexus.javaseutil.util.StringUtilsWraps;


/**
 * Configuration for druid stat filter
 *
 * @author <a href="mailto:89921218@qq.com">lihengming</a>
 * @author David Hsing
 */
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(name = "spring.datasource.druid.web-stat-filter.enabled", havingValue = "true")
public class DruidWebStatFilterConfiguration {
    public static final String WEB_STAT_FILTER = "druidWebStatFilterRegistration";    // $NON-NLS-1$
    private static final String DEFAULT_EXCLUSIONS = "*.3gp,*.7z,*.aac,*.ape,*.asf,*.avi,*.bmp,*.css,*.doc,*.docx,*.eot,*.flac,*.flv,*.gif,*.gz,*.ico,*.jpeg,*.jpg,*.js,*.less,*.log,*.map,*.mkv,*.mp3,*.mp4,*.ogg,*.pdf,*.png,*.ppt,*.pptx,*.psd,*.rar,*.rmvb,*.rtf,*.svg,*.swf,*.tar,*.tiff,*.ttf,*.txt,*.wav,*.wma,*.wmv,*.woff,*.woff2,*.xls,*.xlsx,*.xml,*.yml,*.zip";    // $NON-NLS-1$

    @Bean(name = WEB_STAT_FILTER)
    @ConditionalOnBean(value = DruidStatProperties.class)
    @ConditionalOnMissingBean(value = WebStatFilter.class, parameterizedContainer = FilterRegistrationBean.class)
    public FilterRegistrationBean<WebStatFilter> webStatFilterRegistration(@Nonnull DruidStatProperties properties) {
        FilterRegistrationBean<WebStatFilter> result = new FilterRegistrationBean<>();
        result.setFilter(new WebStatFilter());
        DruidStatProperties.StatViewServlet servletProps = properties.getStatViewServlet();
        DruidStatProperties.WebStatFilter filterProps = properties.getWebStatFilter();

        // David Hsing added on 2021-08-12
        StringUtilsWraps.ifNotBlank(filterProps.getUrlPattern(), element -> result.setUrlPatterns(Collections.singleton(element)));

        // David Hsing modified on 2021-08-12
        String exclusions = filterProps.getExclusions();
        if (BooleanUtils.isNotFalse(filterProps.getDefaultExclusionsEnable())) {
            exclusions = StringUtils.isNotBlank(exclusions) ? StringUtils.join(exclusions, ',', DEFAULT_EXCLUSIONS) : DEFAULT_EXCLUSIONS;
        }
        if (BooleanUtils.isNotFalse(filterProps.getExcludeStatServlet()) && BooleanUtils.isTrue(servletProps.getEnabled()) && StringUtils.isNotBlank(properties.getStatViewServlet().getUrlPattern())) {
            exclusions = StringUtils.isNotBlank(exclusions) ? StringUtils.join(exclusions, ',', properties.getStatViewServlet().getUrlPattern()) : properties.getStatViewServlet().getUrlPattern();
        }
        if (StringUtils.isNotBlank(exclusions)) {
            result.addInitParameter("exclusions", exclusions);    // $NON-NLS-1$
        }

        if (StringUtils.isNotBlank(filterProps.getSessionStatEnable())) {
            result.addInitParameter("sessionStatEnable", filterProps.getSessionStatEnable());    // $NON-NLS-1$
        }
        if (StringUtils.isNotBlank(filterProps.getSessionStatMaxCount())) {
            result.addInitParameter("sessionStatMaxCount", filterProps.getSessionStatMaxCount());    // $NON-NLS-1$
        }
        if (StringUtils.isNotBlank(filterProps.getPrincipalSessionName())) {
            result.addInitParameter("principalSessionName", filterProps.getPrincipalSessionName());    // $NON-NLS-1$
        }
        if (StringUtils.isNotBlank(filterProps.getPrincipalCookieName())) {
            result.addInitParameter("principalCookieName", filterProps.getPrincipalCookieName());    // $NON-NLS-1$
        }
        if (StringUtils.isNotBlank(filterProps.getProfileEnable())) {
            result.addInitParameter("profileEnable", filterProps.getProfileEnable());    // $NON-NLS-1$
        }
        return result;
    }
}
