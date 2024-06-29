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
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.sql.DataSource;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.XADataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.EntityManagerFactoryBuilderCustomizer;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.orm.jpa.JpaDialect;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import com.yookue.commonplexus.springcondition.annotation.ConditionalOnAllProperties;
import com.yookue.commonplexus.springutil.util.ClassPathWraps;
import com.yookue.commonplexus.springutil.util.PropertyBinderWraps;
import com.yookue.springstarter.datasourcebuilder.composer.DataSourceBuilder;
import com.yookue.springstarter.datasourcebuilder.util.JpaConfigurationUtils;


/**
 * Tertiary datasource configuration for Spring Data JPA
 *
 * @author David Hsing
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnAllProperties(value = {
    @ConditionalOnProperty(prefix = "spring.multiple-datasource", name = "enabled", havingValue = "true", matchIfMissing = true),
    @ConditionalOnProperty(prefix = TertiaryDataSourceJdbcConfiguration.PROPERTIES_PREFIX, name = "jpa-enabled", havingValue = "true", matchIfMissing = true)
})
@ConditionalOnClass(value = {DataSource.class, JdbcOperations.class, JpaDialect.class, JpaRepository.class})
@ConditionalOnBean(name = TertiaryDataSourceJdbcConfiguration.DATA_SOURCE)
@AutoConfigureAfter(value = {SecondaryDataSourceJpaConfiguration.class, TertiaryDataSourceJdbcConfiguration.class})
@AutoConfigureBefore(value = {DataSourceAutoConfiguration.class, XADataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@EnableTransactionManagement(order = 103)
public class TertiaryDataSourceJpaConfiguration {
    public static final String PROPERTIES_PREFIX = TertiaryDataSourceJdbcConfiguration.PROPERTIES_PREFIX + ".jpa";    // $NON-NLS-1$
    public static final String JPA_PROPERTIES = "tertiaryDataSourceJpaProperties";    // $NON-NLS-1$
    public static final String PERSISTENCE_UNIT_MANAGER = "tertiaryDataSourcePersistenceUnitManager";    // $NON-NLS-1$
    public static final String PERSISTENCE_UNIT_POST_PROCESSOR = "tertiaryDataSourcePersistenceUnitPostProcessor";    // $NON-NLS-1$
    public static final String PERSISTENCE_UNIT = "tertiaryDataSourcePersistenceUnit";    // $NON-NLS-1$
    public static final String PERSISTENCE_XML_LOCATION = "classpath*:META-INF/persistence-tertiary.xml";    // $NON-NLS-1$
    public static final String ENTITY_MANAGER_FACTORY_BUILDER_CUSTOMIZER = "tertiaryDataSourceEntityManagerFactoryBuilderCustomizer";    // $NON-NLS-1$
    public static final String ENTITY_MANAGER_FACTORY_BUILDER_EXECUTOR = "tertiaryDataSourceEntityManagerFactoryBuilderExecutor";    // $NON-NLS-1$
    public static final String ENTITY_MANAGER_FACTORY_BUILDER = "tertiaryDataSourceEntityManagerFactoryBuilder";    // $NON-NLS-1$
    public static final String ENTITY_MANAGER_FACTORY_BEAN = "tertiaryDataSourceEntityManagerFactoryBean";    // $NON-NLS-1$
    public static final String ENTITY_MANAGER_FACTORY = "tertiaryDataSourceEntityManagerFactory";    // $NON-NLS-1$
    public static final String ENTITY_MANAGER = "tertiaryDataSourceEntityManager";    // $NON-NLS-1$
    public static final String ENTITY_PACKAGE = "**.domain.tertiary.rdbms";    // $NON-NLS-1$
    private static final String XA_PREFIX = TertiaryDataSourceJdbcConfiguration.PROPERTIES_PREFIX + ".xa";    // $NON-NLS-1$

    @Bean(name = JPA_PROPERTIES)
    @ConditionalOnMissingBean(name = JPA_PROPERTIES)
    @ConfigurationProperties(prefix = PROPERTIES_PREFIX)
    public JpaProperties jpaProperties() {
        return new JpaProperties();
    }

    @Bean(name = PERSISTENCE_UNIT_MANAGER)
    @ConditionalOnMissingBean(name = PERSISTENCE_UNIT_MANAGER)
    @SuppressWarnings("DuplicatedCode")
    public PersistenceUnitManager persistenceUnitManager(@Autowired(required = false) @Qualifier(value = TertiaryDataSourceJdbcConfiguration.DATA_SOURCE) @Nullable DataSource dataSource,
        @Autowired(required = false) @Qualifier(value = PERSISTENCE_UNIT_POST_PROCESSOR) @Nullable PersistenceUnitPostProcessor processor,
        @Nonnull ResourceLoader loader, @Nonnull Environment environment) {
        String[] packagesToScan = new String[]{ENTITY_PACKAGE};
        String[] xmlLocations = ClassPathWraps.existsResource(PERSISTENCE_XML_LOCATION, loader.getClassLoader()) ? new String[]{PERSISTENCE_XML_LOCATION} : ArrayUtils.EMPTY_STRING_ARRAY;
        boolean jta = PropertyBinderWraps.contains(environment, XA_PREFIX);
        PersistenceUnitPostProcessor[] postProcessors = (processor == null) ? null : new PersistenceUnitPostProcessor[]{processor};
        return JpaConfigurationUtils.defaultPersistenceUnitManager(PERSISTENCE_UNIT, null, dataSource, packagesToScan, null, xmlLocations, jta, loader, SharedCacheMode.ENABLE_SELECTIVE, ValidationMode.AUTO, postProcessors);
    }

    @Bean(name = ENTITY_MANAGER_FACTORY_BUILDER)
    @ConditionalOnBean(name = {PERSISTENCE_UNIT_MANAGER, JPA_PROPERTIES})
    @ConditionalOnMissingBean(name = ENTITY_MANAGER_FACTORY_BUILDER)
    public EntityManagerFactoryBuilder entityManagerFactoryBuilder(@Qualifier(value = PERSISTENCE_UNIT_MANAGER) @Nonnull PersistenceUnitManager manager,
        @Autowired(required = false) @Qualifier(value = ENTITY_MANAGER_FACTORY_BUILDER_EXECUTOR) @Nullable AsyncTaskExecutor executor,
        @Autowired(required = false) @Qualifier(value = ENTITY_MANAGER_FACTORY_BUILDER_CUSTOMIZER) @Nullable EntityManagerFactoryBuilderCustomizer customizer,
        @Qualifier(value = JPA_PROPERTIES) @Nonnull JpaProperties properties) {
        EntityManagerFactoryBuilder builder = new EntityManagerFactoryBuilder(JpaConfigurationUtils.hibernateJpaVendorAdapter(properties), properties.getProperties(), manager);
        builder.setBootstrapExecutor(executor);
        if (customizer != null) {
            customizer.customize(builder);
        }
        return builder;
    }

    @Bean(name = ENTITY_MANAGER_FACTORY_BEAN)
    @ConditionalOnBean(name = {ENTITY_MANAGER_FACTORY_BUILDER, TertiaryDataSourceJdbcConfiguration.DATA_SOURCE, JPA_PROPERTIES})
    @ConditionalOnMissingBean(name = ENTITY_MANAGER_FACTORY_BEAN)
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(@Qualifier(value = ENTITY_MANAGER_FACTORY_BUILDER) @Nonnull EntityManagerFactoryBuilder builder,
        @Qualifier(value = TertiaryDataSourceJdbcConfiguration.DATA_SOURCE) @Nonnull DataSource dataSource,
        @Qualifier(value = JPA_PROPERTIES) @Nonnull JpaProperties properties, @Nonnull Environment environment) {
        return builder.dataSource(dataSource).packages(ENTITY_PACKAGE).persistenceUnit(PERSISTENCE_UNIT).properties(properties.getProperties()).jta(PropertyBinderWraps.contains(environment, XA_PREFIX)).build();
    }

    @Bean(name = ENTITY_MANAGER_FACTORY)
    @ConditionalOnBean(name = ENTITY_MANAGER_FACTORY_BEAN)
    @ConditionalOnMissingBean(name = ENTITY_MANAGER_FACTORY)
    public EntityManagerFactory entityManagerFactory(@Qualifier(value = ENTITY_MANAGER_FACTORY_BEAN) @Nonnull LocalContainerEntityManagerFactoryBean factoryBean) {
        return factoryBean.getObject();
    }

    @Bean(name = ENTITY_MANAGER)
    @ConditionalOnBean(name = ENTITY_MANAGER_FACTORY)
    @ConditionalOnMissingBean(name = ENTITY_MANAGER)
    public EntityManager entityManager(@Qualifier(value = ENTITY_MANAGER_FACTORY) @Nonnull EntityManagerFactory factory) {
        return factory.createEntityManager();
    }

    @Bean(name = TertiaryDataSourceJdbcConfiguration.TRANSACTION_MANAGER)
    @ConditionalOnProperty(prefix = TertiaryDataSourceJpaConfiguration.PROPERTIES_PREFIX, name = "jpa-transaction", havingValue = "true", matchIfMissing = true)
    @ConditionalOnBean(name = ENTITY_MANAGER_FACTORY, value = DataSourceBuilder.class)
    @ConditionalOnMissingBean(name = TertiaryDataSourceJdbcConfiguration.TRANSACTION_MANAGER)
    public TransactionManager transactionManager(@Nonnull DataSourceBuilder builder, @Qualifier(value = ENTITY_MANAGER_FACTORY) @Nonnull EntityManagerFactory factory, @Nonnull ObjectProvider<TransactionManagerCustomizers> customizers) {
        return builder.jpaTransactionManager(factory, customizers);
    }
}
