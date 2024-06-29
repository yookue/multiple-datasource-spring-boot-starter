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


import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.XADataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.orm.jpa.JpaDialect;
import org.springframework.stereotype.Repository;
import com.yookue.commonplexus.springcondition.annotation.ConditionalOnAllProperties;
import com.yookue.commonplexus.springcondition.annotation.ConditionalOnAnnotation;


/**
 * Primary datasource configuration for Spring Data JPA repository
 *
 * @author David Hsing
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnAllProperties(value = {
    @ConditionalOnProperty(prefix = "spring.multiple-datasource", name = "enabled", havingValue = "true", matchIfMissing = true),
    @ConditionalOnProperty(prefix = PrimaryDataSourceJpaConfiguration.PROPERTIES_PREFIX, name = "jpa-enabled", havingValue = "true", matchIfMissing = true),
    @ConditionalOnProperty(prefix = PrimaryDataSourceJpaConfiguration.PROPERTIES_PREFIX, name = "repository-enabled", havingValue = "true", matchIfMissing = true)
})
@ConditionalOnClass(value = {DataSource.class, JdbcOperations.class, JpaDialect.class, JpaRepository.class})
@ConditionalOnBean(name = PrimaryDataSourceJdbcConfiguration.DATA_SOURCE)
@ConditionalOnAnnotation(includeFilter = Repository.class, basePackage = PrimaryDataSourceRepositoryConfiguration.REPOSITORY_PACKAGE)
@AutoConfigureAfter(value = PrimaryDataSourceJpaConfiguration.class)
@AutoConfigureBefore(value = {DataSourceAutoConfiguration.class, XADataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@EnableJpaRepositories(basePackages = PrimaryDataSourceRepositoryConfiguration.REPOSITORY_PACKAGE, entityManagerFactoryRef = PrimaryDataSourceJpaConfiguration.ENTITY_MANAGER_FACTORY, transactionManagerRef = PrimaryDataSourceJdbcConfiguration.TRANSACTION_MANAGER)
public class PrimaryDataSourceRepositoryConfiguration {
    public static final String REPOSITORY_PACKAGE = "**.repository.primary.rdbms";    // $NON-NLS-1$
}
