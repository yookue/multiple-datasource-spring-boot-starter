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
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import com.alibaba.druid.filter.config.ConfigFilter;
import com.alibaba.druid.filter.encoding.EncodingConvertFilter;
import com.alibaba.druid.filter.logging.CommonsLogFilter;
import com.alibaba.druid.filter.logging.Log4j2Filter;
import com.alibaba.druid.filter.logging.Log4jFilter;
import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;


/**
 * Configuration for druid filter
 * <p>
 * Druid filters alias can be found in {@link "META-INF/druid-filter.properties"}
 *
 * @author <a href="mailto:89921218@qq.com">lihengming</a>
 * @author David Hsing
 *
 * @see com.alibaba.druid.filter.FilterManager#loadFilterConfig()
 */
public class DruidFilterConfiguration {
    private static final String FILTER_PREFIX = "spring.datasource.druid.filter";    // $NON-NLS-1$
    private static final String FILTER_CONFIG_PREFIX = FILTER_PREFIX + ".config";    // $NON-NLS-1$
    private static final String FILTER_STAT_PREFIX = FILTER_PREFIX + ".stat";    // $NON-NLS-1$
    private static final String FILTER_ENCODING_PREFIX = FILTER_PREFIX + ".encoding";    // $NON-NLS-1$
    private static final String FILTER_SLF4J_PREFIX = FILTER_PREFIX + ".slf4j";    // $NON-NLS-1$
    private static final String FILTER_LOG4J_PREFIX = FILTER_PREFIX + ".log4j";    // $NON-NLS-1$
    private static final String FILTER_LOG4J2_PREFIX = FILTER_PREFIX + ".log4j2";    // $NON-NLS-1$
    private static final String FILTER_COMMONS_LOG_PREFIX = FILTER_PREFIX + ".commons-log";    // $NON-NLS-1$
    private static final String FILTER_WALL_PREFIX = FILTER_PREFIX + ".wall";    // $NON-NLS-1$
    private static final String FILTER_WALL_CONFIG_PREFIX = FILTER_WALL_PREFIX + ".config";    // $NON-NLS-1$

    public static final String CONFIG_FILTER = "druidConfigFilter";    // $NON-NLS-1$
    public static final String STAT_FILTER = "druidStatFilter";    // $NON-NLS-1$
    public static final String ENCODING_CONVERT_FILTER = "druidEncodingConvertFilter";    // $NON-NLS-1$
    public static final String SLF4J_FILTER = "druidSlf4jFilter";    // $NON-NLS-1$
    public static final String LOG4J_FILTER = "druidLog4jFilter";    // $NON-NLS-1$
    public static final String LOG4J2_FILTER = "druidLog4j2Filter";    // $NON-NLS-1$
    public static final String COMMONS_LOG_FILTER = "druidCommonsLogFilter";    // $NON-NLS-1$
    public static final String WALL_CONFIG = "druidWallConfig";    // $NON-NLS-1$
    public static final String WALL_FILTER = "druidWallFilter";    // $NON-NLS-1$

    @Bean(name = CONFIG_FILTER)
    @ConfigurationProperties(FILTER_CONFIG_PREFIX)
    @ConditionalOnProperty(prefix = FILTER_CONFIG_PREFIX, name = "enabled")
    @ConditionalOnMissingBean
    public ConfigFilter configFilter() {
        return new ConfigFilter();
    }

    @Bean(name = STAT_FILTER)
    @ConfigurationProperties(FILTER_STAT_PREFIX)
    @ConditionalOnProperty(prefix = FILTER_STAT_PREFIX, name = "enabled")
    @ConditionalOnMissingBean
    public StatFilter statFilter() {
        return new StatFilter();
    }

    @Bean(name = ENCODING_CONVERT_FILTER)
    @ConfigurationProperties(FILTER_ENCODING_PREFIX)
    @ConditionalOnProperty(prefix = FILTER_ENCODING_PREFIX, name = "enabled")
    @ConditionalOnMissingBean
    public EncodingConvertFilter encodingConvertFilter() {
        return new EncodingConvertFilter();
    }

    @Bean(name = SLF4J_FILTER)
    @ConfigurationProperties(FILTER_SLF4J_PREFIX)
    @ConditionalOnProperty(prefix = FILTER_SLF4J_PREFIX, name = "enabled")
    @ConditionalOnClass(name = "org.slf4j.Logger")
    @ConditionalOnMissingBean
    public Slf4jLogFilter slf4jLogFilter() {
        return new DruidCompositeSlf4jFilter();
    }

    @Bean(name = LOG4J_FILTER)
    @ConfigurationProperties(FILTER_LOG4J_PREFIX)
    @ConditionalOnProperty(prefix = FILTER_LOG4J_PREFIX, name = "enabled")
    @ConditionalOnClass(name = "org.apache.log4j.Logger")
    @ConditionalOnMissingBean
    public Log4jFilter log4jFilter() {
        return new DruidCompositeLog4jFilter();
    }

    @Bean(name = LOG4J2_FILTER)
    @ConfigurationProperties(FILTER_LOG4J2_PREFIX)
    @ConditionalOnProperty(prefix = FILTER_LOG4J2_PREFIX, name = "enabled")
    @ConditionalOnClass(name = "org.apache.logging.log4j.Logger")
    @ConditionalOnMissingBean
    public Log4j2Filter log4j2Filter() {
        return new DruidCompositeLog4j2Filter();
    }

    @Bean(name = COMMONS_LOG_FILTER)
    @ConfigurationProperties(FILTER_COMMONS_LOG_PREFIX)
    @ConditionalOnProperty(prefix = FILTER_COMMONS_LOG_PREFIX, name = "enabled")
    @ConditionalOnClass(name = "org.apache.commons.logging.LogFactory")
    @ConditionalOnMissingBean
    public CommonsLogFilter commonsLogFilter() {
        return new DruidCompositeCommonsLogFilter();
    }

    @Bean(name = WALL_CONFIG)
    @ConfigurationProperties(FILTER_WALL_CONFIG_PREFIX)
    @ConditionalOnProperty(prefix = FILTER_WALL_PREFIX, name = "enabled")
    @ConditionalOnMissingBean
    public WallConfig wallConfig() {
        return new WallConfig();
    }

    @Bean(name = WALL_FILTER)
    @ConfigurationProperties(FILTER_WALL_PREFIX)
    @ConditionalOnProperty(prefix = FILTER_WALL_PREFIX, name = "enabled")
    @ConditionalOnBean(value = WallConfig.class)
    @ConditionalOnMissingBean
    public WallFilter wallFilter(@Nonnull WallConfig wallConfig) {
        WallFilter filter = new WallFilter();
        filter.setConfig(wallConfig);
        return filter;
    }
}
