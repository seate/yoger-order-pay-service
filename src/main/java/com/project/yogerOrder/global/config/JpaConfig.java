package com.project.yogerOrder.global.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.orm.jpa.JpaTransactionManager;

@Configuration
@EnableJpaAuditing // 테스트 시에 관련 bean을 로드하지 않기 때문에, 에러 발생 가능성이 있기에 메인에 적용하지 않음
public class JpaConfig {

    @Bean
    @Primary
    public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
