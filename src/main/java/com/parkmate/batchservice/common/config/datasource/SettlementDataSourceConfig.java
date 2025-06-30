package com.parkmate.batchservice.common.config.datasource;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@EnableJpaRepositories(
        basePackages = "com.parkmate.batchservice.hostsettlement.infrastructure.repository",
        entityManagerFactoryRef = "settlementEntityManagerFactory",
        transactionManagerRef = "transactionManager" // ✔ Spring이 기대하는 이름
)
public class SettlementDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.settlement-datasource")
    public DataSourceProperties settlementDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "settlementDataSource")
    public DataSource settlementDataSource() {
        return settlementDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean(name = "settlementEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean settlementEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("settlementDataSource") DataSource dataSource
    ) {
        Map<String, Object> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.hbm2ddl.auto", "update"); // ✔ 개발 환경에서 자동 테이블 생성

        return builder
                .dataSource(dataSource)
                .packages("com.parkmate.batchservice.hostsettlement.domain")
                .persistenceUnit("host_settlement")
                .properties(jpaProperties)
                .build();
    }

    @Bean(name = "transactionManager") // ✔ 기본 이름으로 선언
    public PlatformTransactionManager transactionManager(
            @Qualifier("settlementEntityManagerFactory") EntityManagerFactory emf
    ) {
        return new JpaTransactionManager(emf);
    }
}