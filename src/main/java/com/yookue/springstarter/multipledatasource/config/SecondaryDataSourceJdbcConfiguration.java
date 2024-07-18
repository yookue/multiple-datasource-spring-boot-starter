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


import java.util.List;
import javax.sql.DataSource;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DruidDataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.XADataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.boot.jdbc.XADataSourceWrapper;
import org.springframework.boot.jdbc.metadata.CommonsDbcp2DataSourcePoolMetadata;
import org.springframework.boot.jdbc.metadata.DataSourcePoolMetadataProvider;
import org.springframework.boot.jdbc.metadata.HikariDataSourcePoolMetadata;
import org.springframework.boot.jdbc.metadata.OracleUcpDataSourcePoolMetadata;
import org.springframework.boot.jdbc.metadata.TomcatDataSourcePoolMetadata;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionManager;
import com.alibaba.druid.filter.Filter;
import com.yookue.commonplexus.springcondition.annotation.ConditionalOnAnyProperties;
import com.yookue.commonplexus.springcondition.annotation.ConditionalOnMissingProperty;
import com.yookue.springstarter.datasourcebuilder.composer.DataSourceBuilder;
import com.yookue.springstarter.datasourcebuilder.config.DataSourceBuilderConfiguration;
import com.yookue.springstarter.datasourcebuilder.constant.DataSourcePoolConst;
import com.yookue.springstarter.datasourcebuilder.enumeration.DataSourcePoolType;


/**
 * Secondary datasource configuration for JDBC
 *
 * @author David Hsing
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "spring.multiple-datasource", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperties(value = {
    @ConditionalOnProperty(prefix = SecondaryDataSourceJdbcConfiguration.PROPERTIES_PREFIX, name = "jndi-name"),
    @ConditionalOnProperty(prefix = SecondaryDataSourceJdbcConfiguration.PROPERTIES_PREFIX, name = "url")
})
@ConditionalOnClass(value = {DataSource.class, JdbcOperations.class})
@AutoConfigureAfter(value = {DataSourceBuilderConfiguration.class, PrimaryDataSourceJdbcConfiguration.class})
@AutoConfigureBefore(value = {DataSourceAutoConfiguration.class, XADataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@Import(value = {DataSourceBuilderConfiguration.class, SecondaryDataSourceJdbcConfiguration.Entry.class, SecondaryDataSourceJdbcConfiguration.Xa.class, SecondaryDataSourceJdbcConfiguration.Jndi.class, SecondaryDataSourceJdbcConfiguration.C3p0.class, SecondaryDataSourceJdbcConfiguration.Dbcp2.class, SecondaryDataSourceJdbcConfiguration.Druid.class, SecondaryDataSourceJdbcConfiguration.Hikari.class, SecondaryDataSourceJdbcConfiguration.OracleUcp.class, SecondaryDataSourceJdbcConfiguration.Tomcat.class, SecondaryDataSourceJdbcConfiguration.Generic.class, SecondaryDataSourceJdbcConfiguration.Stage.class})
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class SecondaryDataSourceJdbcConfiguration {
    public static final String PROPERTIES_PREFIX = "spring.multiple-datasource.secondary";    // $NON-NLS-1$
    public static final String DATA_SOURCE_PROPERTIES = "secondaryDataSourceProperties";    // $NON-NLS-1$
    public static final String DATA_SOURCE = "secondaryDataSource";    // $NON-NLS-1$
    public static final String JDBC_TEMPLATE = "secondaryDataSourceJdbcTemplate";    // $NON-NLS-1$
    public static final String TRANSACTION_MANAGER = "secondaryDataSourceTransactionManager";    // $NON-NLS-1$
    public static final String METADATA_PROVIDER = "secondaryDataSourceMetadataProvider";    // $NON-NLS-1$


    @Order(value = 0)
    static class Entry {
        @Bean(name = DATA_SOURCE_PROPERTIES)
        @ConditionalOnBean(value = DataSourceBuilder.class)
        @ConditionalOnMissingBean(name = DATA_SOURCE_PROPERTIES)
        public DataSourceProperties dataSourceProperties(@Nonnull DataSourceBuilder builder, @Nonnull Environment environment) {
            return builder.dataSourceProperties(environment, PROPERTIES_PREFIX);
        }
    }


    @ConditionalOnProperty(prefix = PROPERTIES_PREFIX, name = "xa")
    @ConditionalOnBean(name = DATA_SOURCE_PROPERTIES, value = {DataSourceBuilder.class, XADataSourceWrapper.class})
    @Order(value = 1)
    static class Xa {
        @Bean(name = DATA_SOURCE)
        @ConditionalOnMissingBean(name = DATA_SOURCE)
        public DataSource dataSource(@Nonnull DataSourceBuilder builder, @Nonnull XADataSourceWrapper wrapper, @Qualifier(value = DATA_SOURCE_PROPERTIES) @Nonnull DataSourceProperties properties) throws Exception {
            return builder.xaDataSource(wrapper, properties, null);
        }
    }


    @ConditionalOnProperty(prefix = PROPERTIES_PREFIX, name = "jndi-name")
    @ConditionalOnClass(value = org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup.class)
    @ConditionalOnBean(name = DATA_SOURCE_PROPERTIES, value = DataSourceBuilder.class)
    @Order(value = 2)
    static class Jndi {
        @Bean(name = DATA_SOURCE)
        @ConditionalOnMissingBean(name = DATA_SOURCE)
        public DataSource dataSource(@Nonnull DataSourceBuilder builder, @Qualifier(value = DATA_SOURCE_PROPERTIES) @Nonnull DataSourceProperties properties) {
            return builder.dataSource(properties);
        }
    }


    @ConditionalOnProperty(prefix = PROPERTIES_PREFIX, name = "type", havingValue = DataSourcePoolConst.C3P0, matchIfMissing = true)
    @ConditionalOnMissingProperty(prefix = PROPERTIES_PREFIX, name = "jndi-name")
    @ConditionalOnClass(value = com.mchange.v2.c3p0.ComboPooledDataSource.class)
    @ConditionalOnBean(name = DATA_SOURCE_PROPERTIES, value = DataSourceBuilder.class)
    @Order(value = 3)
    static class C3p0 {
        @Bean(name = DATA_SOURCE, destroyMethod = "close")
        @ConditionalOnMissingBean(name = DATA_SOURCE)
        public com.mchange.v2.c3p0.ComboPooledDataSource dataSource(@Nonnull DataSourceBuilder builder, @Qualifier(value = DATA_SOURCE_PROPERTIES) @Nonnull DataSourceProperties properties) {
            return (com.mchange.v2.c3p0.ComboPooledDataSource) builder.dataSource(properties, DataSourcePoolType.C3P0);
        }
    }


    @ConditionalOnProperty(prefix = PROPERTIES_PREFIX, name = "type", havingValue = DataSourcePoolConst.DBCP2, matchIfMissing = true)
    @ConditionalOnMissingProperty(prefix = PROPERTIES_PREFIX, name = "jndi-name")
    @ConditionalOnClass(value = org.apache.commons.dbcp2.BasicDataSource.class)
    @ConditionalOnBean(name = DATA_SOURCE_PROPERTIES, value = DataSourceBuilder.class)
    @Order(value = 4)
    static class Dbcp2 {
        @Bean(name = DATA_SOURCE, destroyMethod = "close")
        @ConditionalOnMissingBean(name = DATA_SOURCE)
        public org.apache.commons.dbcp2.BasicDataSource dataSource(@Nonnull DataSourceBuilder builder, @Qualifier(value = DATA_SOURCE_PROPERTIES) @Nonnull DataSourceProperties properties) {
            return (org.apache.commons.dbcp2.BasicDataSource) builder.dataSource(properties, DataSourcePoolType.DBCP2);
        }

        @Bean(name = METADATA_PROVIDER)
        @ConditionalOnMissingBean(name = METADATA_PROVIDER)
        public DataSourcePoolMetadataProvider metadataProvider(@Qualifier(value = DATA_SOURCE) @Nonnull org.apache.commons.dbcp2.BasicDataSource dataSource) {
            return sqlDataSource -> new CommonsDbcp2DataSourcePoolMetadata(dataSource);
        }
    }


    @ConditionalOnProperty(prefix = PROPERTIES_PREFIX, name = "type", havingValue = DataSourcePoolConst.DRUID, matchIfMissing = true)
    @ConditionalOnMissingProperty(prefix = PROPERTIES_PREFIX, name = "jndi-name")
    @ConditionalOnClass(value = com.alibaba.druid.pool.DruidDataSource.class)
    @ConditionalOnBean(name = DATA_SOURCE_PROPERTIES, value = DataSourceBuilder.class)
    @Order(value = 5)
    static class Druid {
        @Bean(name = DATA_SOURCE, initMethod = "init", destroyMethod = "close")
        @ConditionalOnMissingBean(name = DATA_SOURCE)
        public com.alibaba.druid.pool.DruidDataSource dataSource(@Nonnull DataSourceBuilder builder, @Qualifier(value = DATA_SOURCE_PROPERTIES) @Nonnull DataSourceProperties properties, @Nullable List<Filter> filters) {
            com.alibaba.druid.pool.DruidDataSource dataSource = (com.alibaba.druid.pool.DruidDataSource) builder.dataSource(properties, DataSourcePoolType.DRUID);
            DruidDataSourceBuilder.addOrReplaceFilters(dataSource, filters);
            return dataSource;
        }
    }


    @ConditionalOnProperty(prefix = PROPERTIES_PREFIX, name = "type", havingValue = DataSourcePoolConst.HIKARI, matchIfMissing = true)
    @ConditionalOnMissingProperty(prefix = PROPERTIES_PREFIX, name = "jndi-name")
    @ConditionalOnClass(value = com.zaxxer.hikari.HikariDataSource.class)
    @ConditionalOnBean(name = DATA_SOURCE_PROPERTIES, value = DataSourceBuilder.class)
    @Order(value = 6)
    static class Hikari {
        @Bean(name = DATA_SOURCE, destroyMethod = "close")
        @ConditionalOnMissingBean(name = DATA_SOURCE)
        public com.zaxxer.hikari.HikariDataSource dataSource(@Nonnull DataSourceBuilder builder, @Qualifier(value = DATA_SOURCE_PROPERTIES) @Nonnull DataSourceProperties properties) {
            return (com.zaxxer.hikari.HikariDataSource) builder.dataSource(properties, DataSourcePoolType.HIKARI);
        }

        @Bean(name = METADATA_PROVIDER)
        @ConditionalOnMissingBean(name = METADATA_PROVIDER)
        public DataSourcePoolMetadataProvider metadataProvider(@Qualifier(value = DATA_SOURCE) @Nonnull com.zaxxer.hikari.HikariDataSource dataSource) {
            return sqlDataSource -> new HikariDataSourcePoolMetadata(dataSource);
        }
    }


    @ConditionalOnProperty(prefix = PROPERTIES_PREFIX, name = "type", havingValue = DataSourcePoolConst.ORACLE_UCP, matchIfMissing = true)
    @ConditionalOnMissingProperty(prefix = PROPERTIES_PREFIX, name = "jndi-name")
    @ConditionalOnClass(value = oracle.ucp.jdbc.PoolDataSourceImpl.class)
    @ConditionalOnBean(name = DATA_SOURCE_PROPERTIES, value = DataSourceBuilder.class)
    @Order(value = 7)
    static class OracleUcp {
        @Bean(name = DATA_SOURCE)
        @ConditionalOnMissingBean(name = DATA_SOURCE)
        public oracle.ucp.jdbc.PoolDataSourceImpl dataSource(@Nonnull DataSourceBuilder builder, @Qualifier(value = DATA_SOURCE_PROPERTIES) @Nonnull DataSourceProperties properties) {
            return (oracle.ucp.jdbc.PoolDataSourceImpl) builder.dataSource(properties, DataSourcePoolType.ORACLE_UCP);
        }

        @Bean(name = METADATA_PROVIDER)
        @ConditionalOnMissingBean(name = METADATA_PROVIDER)
        public DataSourcePoolMetadataProvider metadataProvider(@Qualifier(value = DATA_SOURCE) @Nonnull oracle.ucp.jdbc.PoolDataSourceImpl dataSource) {
            return sqlDataSource -> new OracleUcpDataSourcePoolMetadata(dataSource);
        }
    }


    @ConditionalOnProperty(prefix = PROPERTIES_PREFIX, name = "type", havingValue = DataSourcePoolConst.ORACLE_UCP_XA, matchIfMissing = true)
    @ConditionalOnMissingProperty(prefix = PROPERTIES_PREFIX, name = "jndi-name")
    @ConditionalOnClass(value = oracle.ucp.jdbc.PoolXADataSourceImpl.class)
    @ConditionalOnBean(name = DATA_SOURCE_PROPERTIES, value = DataSourceBuilder.class)
    @Order(value = 8)
    static class OracleUcpXa {
        @Bean(name = DATA_SOURCE)
        @ConditionalOnMissingBean(name = DATA_SOURCE)
        public oracle.ucp.jdbc.PoolXADataSourceImpl dataSource(@Nonnull DataSourceBuilder builder, @Qualifier(value = DATA_SOURCE_PROPERTIES) @Nonnull DataSourceProperties properties) {
            return (oracle.ucp.jdbc.PoolXADataSourceImpl) builder.dataSource(properties, DataSourcePoolType.ORACLE_UCP_XA);
        }

        @Bean(name = METADATA_PROVIDER)
        @ConditionalOnMissingBean(name = METADATA_PROVIDER)
        public DataSourcePoolMetadataProvider metadataProvider(@Qualifier(value = DATA_SOURCE) @Nonnull oracle.ucp.jdbc.PoolXADataSourceImpl dataSource) {
            return sqlDataSource -> new OracleUcpDataSourcePoolMetadata(dataSource);
        }
    }


    @ConditionalOnProperty(prefix = PROPERTIES_PREFIX, name = "type", havingValue = DataSourcePoolConst.TOMCAT, matchIfMissing = true)
    @ConditionalOnMissingProperty(prefix = PROPERTIES_PREFIX, name = "jndi-name")
    @ConditionalOnBean(name = DATA_SOURCE, value = org.apache.tomcat.jdbc.pool.DataSource.class)
    @Order(value = 9)
    static class Tomcat {
        @Bean(name = DATA_SOURCE, destroyMethod = "close")
        @ConditionalOnProperty(prefix = PROPERTIES_PREFIX, name = "type", havingValue = DataSourcePoolConst.TOMCAT, matchIfMissing = true)
        @ConditionalOnMissingProperty(prefix = PROPERTIES_PREFIX, name = "jndi-name")
        @ConditionalOnClass(value = org.apache.tomcat.jdbc.pool.DataSource.class)
        @ConditionalOnBean(name = DATA_SOURCE_PROPERTIES, value = DataSourceBuilder.class)
        @ConditionalOnMissingBean(name = DATA_SOURCE)
        public org.apache.tomcat.jdbc.pool.DataSource dataSource(@Nonnull DataSourceBuilder builder, @Qualifier(value = DATA_SOURCE_PROPERTIES) @Nonnull DataSourceProperties properties) {
            return (org.apache.tomcat.jdbc.pool.DataSource) builder.dataSource(properties, DataSourcePoolType.TOMCAT);
        }

        @Bean(name = METADATA_PROVIDER)
        @ConditionalOnMissingBean(name = METADATA_PROVIDER)
        public DataSourcePoolMetadataProvider metadataProvider(@Qualifier(value = DATA_SOURCE) @Nonnull org.apache.tomcat.jdbc.pool.DataSource dataSource) {
            return sqlDataSource -> new TomcatDataSourcePoolMetadata(dataSource);
        }
    }


    @ConditionalOnMissingProperty(prefix = PROPERTIES_PREFIX, name = "jndi-name")
    @Order(value = 10)
    static class Generic {
        @Bean(name = DATA_SOURCE)
        @ConditionalOnBean(name = DATA_SOURCE_PROPERTIES, value = DataSourceBuilder.class)
        @ConditionalOnMissingBean(name = DATA_SOURCE)
        public DataSource dataSource(@Nonnull DataSourceBuilder builder, @Qualifier(value = DATA_SOURCE_PROPERTIES) @Nonnull DataSourceProperties properties) {
            return builder.dataSource(properties);
        }
    }


    @Order(value = 11)
    static class Stage {
        @Bean(name = JDBC_TEMPLATE)
        @ConditionalOnBean(name = DATA_SOURCE)
        @ConditionalOnMissingBean(name = JDBC_TEMPLATE)
        public JdbcTemplate jdbcTemplate(@Qualifier(value = DATA_SOURCE) @Nonnull DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }

        @Bean(name = TRANSACTION_MANAGER)
        @ConditionalOnProperty(prefix = PROPERTIES_PREFIX, name = "jdbc-transaction", havingValue = "true", matchIfMissing = true)
        @ConditionalOnBean(name = DATA_SOURCE, value = DataSourceBuilder.class)
        @ConditionalOnMissingBean(name = TRANSACTION_MANAGER)
        public TransactionManager transactionManager(@Nonnull DataSourceBuilder builder, @Qualifier(value = DATA_SOURCE) DataSource dataSource, @Nonnull ObjectProvider<TransactionManagerCustomizers> customizers) {
            return builder.jdbcTransactionManager(dataSource, customizers);
        }
    }
}
