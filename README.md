# Multiple Datasource Spring Boot Starter

Spring Boot application integrates multiple `DataSource` quickly.

## Quickstart

- Import dependencies

```xml
    <dependency>
        <groupId>com.yookue.springstarter</groupId>
        <artifactId>multiple-datasource-spring-boot-starter</artifactId>
        <version>LATEST</version>
    </dependency>
```

> By default, this starter will auto take effect, you can turn it off by `spring.multiple-datasource.enabled = false`

- Configure Spring Boot `application.yml` with prefix `spring.multiple-datasource`

```yml
spring:
    multiple-datasource:
        primary:
            url: 'jdbc:mysql://127.0.0.1:3306/test_db1'
            driver-class-name: com.mysql.cj.jdbc.Driver
            type: com.zaxxer.hikari.HikariDataSource
            jpa-enabled: true
            repository-enabled: true
            mybatis:
            xa:
        secondary:
            url: 'jdbc:mysql://127.0.0.1:3306/test_db2'
            driver-class-name: com.mysql.cj.jdbc.Driver
            type: com.mchange.v2.c3p0.ComboPooledDataSource
            jpa-enabled: true
            repository-enabled: true
            mybatis:
            xa:
        tertiary:
            url: 'jdbc:mysql://127.0.0.1:3306/test_db3'
            driver-class-name: com.mysql.cj.jdbc.Driver
            type: org.apache.commons.dbcp2.BasicDataSource
            jpa-enabled: true
            repository-enabled: true
            mybatis:
            xa:
```

> This starter supports 3 `DataSource` at most. (Three strikes and you're out)

- Locate your entities and repositories under the following packages (take `primary` as an example)

    - Entities: `**.domain.primary.rdbms`
    - Repositories: `**.repository.primary.rdbms`

- Configure your beans with the following beans by `@Autowired`/`@Resource` annotation, combined with `@Qualifier` annotation (take `primary` as an example)

| Bean Type              | Qualifier                                                  |
|------------------------|------------------------------------------------------------|
| DataSource             | PrimaryDataSourceJdbcConfiguration.DATA_SOURCE             |
| JdbcTemplate           | PrimaryDataSourceJdbcConfiguration.JDBC_TEMPLATE           |
| TransactionManager     | PrimaryDataSourceJdbcConfiguration.TRANSACTION_MANAGER     |
| PersistenceUnitManager | PrimaryDataSourceJpaConfiguration.PERSISTENCE_UNIT         |
| EntityManager          | PrimaryDataSourceJpaConfiguration.ENTITY_MANAGER           |
| SqlSessionFactory      | PrimaryDataSourceMybatisConfiguration.SQL_SESSION_FACTORY  |
| SqlSessionTemplate     | PrimaryDataSourceMybatisConfiguration.SQL_SESSION_TEMPLATE |

- This starter supports the most popular data source pools in the world, including
  - c3p0
  - dbcp2
  - druid
  - hikari
  - oracle ucp
  - tomcat
  - vibur

## Document

- Github: https://github.com/yookue/multiple-datasource-spring-boot-starter

## Requirement

- jdk 17+

## License

This project is under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)

See the `NOTICE.txt` file for required notices and attributions.

## Donation

You like this package? Then [donate to Yookue](https://yookue.com/public/donate) to support the development.

## Website

- Yookue: https://yookue.com
