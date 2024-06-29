/*
 * Copyright (c) 2020 Yookue Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yookue.springstarter.multipledatasource.config;


import javax.annotation.Nonnull;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.yookue.commonplexus.springcondition.annotation.ConditionalOnPropertyPrefix;
import com.yookue.springstarter.mybatisdelegator.composer.MybatisConfigurationDelegator;
import com.yookue.springstarter.mybatisdelegator.config.MybatisDelegatorAutoConfiguration;


/**
 * Secondary datasource configuration for mybatis
 *
 * @author David Hsing
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "spring.multiple-datasource", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnPropertyPrefix(prefix = SecondaryDataSourceJdbcConfiguration.PROPERTIES_PREFIX + ".mybatis")
@ConditionalOnClass(value = {DataSource.class, SqlSession.class, MybatisConfigurationDelegator.class})
@ConditionalOnBean(name = SecondaryDataSourceJdbcConfiguration.DATA_SOURCE)
@AutoConfigureAfter(value = {MybatisDelegatorAutoConfiguration.class, PrimaryDataSourceMybatisConfiguration.class, SecondaryDataSourceRepositoryConfiguration.class})
@Import(value = MybatisDelegatorAutoConfiguration.class)
public class SecondaryDataSourceMybatisConfiguration {
    public static final String MYBATIS_PROPERTIES = "secondaryMybatisProperties";    // $NON-NLS-1$
    public static final String SQL_SESSION_FACTORY = "secondarySqlSessionFactory";    // $NON-NLS-1$
    public static final String SQL_SESSION_TEMPLATE = "secondarySqlSessionTemplate";    // $NON-NLS-1$

    @Bean(name = MYBATIS_PROPERTIES)
    @ConditionalOnMissingBean(name = MYBATIS_PROPERTIES)
    @ConfigurationProperties(prefix = SecondaryDataSourceJdbcConfiguration.PROPERTIES_PREFIX + ".mybatis")
    public MybatisProperties mybatisProperties() {
        return new MybatisProperties();
    }

    @Bean(name = SQL_SESSION_FACTORY)
    @ConditionalOnBean(name = MYBATIS_PROPERTIES, value = MybatisConfigurationDelegator.class)
    @ConditionalOnMissingBean(name = SQL_SESSION_FACTORY)
    public SqlSessionFactory sqlSessionFactory(@Nonnull MybatisConfigurationDelegator delegator,
        @Qualifier(value = SecondaryDataSourceJdbcConfiguration.DATA_SOURCE) @Nonnull DataSource dataSource,
        @Qualifier(value = MYBATIS_PROPERTIES) @Nonnull MybatisProperties properties) throws Exception {
        return delegator.sqlSessionFactory(dataSource, properties);
    }

    @Bean(name = SQL_SESSION_TEMPLATE)
    @ConditionalOnBean(name = {SQL_SESSION_FACTORY, MYBATIS_PROPERTIES}, value = MybatisConfigurationDelegator.class)
    @ConditionalOnMissingBean(name = SQL_SESSION_TEMPLATE)
    public SqlSessionTemplate sqlSessionTemplate(@Nonnull MybatisConfigurationDelegator delegator,
        @Qualifier(value = SQL_SESSION_FACTORY) @Nonnull SqlSessionFactory factory,
        @Qualifier(value = MYBATIS_PROPERTIES) @Nonnull MybatisProperties properties) {
        return delegator.sqlSessionTemplate(factory, properties);
    }
}
