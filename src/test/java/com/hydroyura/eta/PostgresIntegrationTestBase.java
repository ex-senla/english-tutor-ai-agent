package com.hydroyura.eta;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * База для интеграционных тестов: реальный PostgreSQL через Testcontainers
 * (postgres:17-alpine, как в docker-compose). Spring-контекст поднимается без
 * Telegram/AI credentials — бот и LLM отключены, вызовов к ним нет.
 */
@Testcontainers
@SpringBootTest(properties = {
        "telegram.bot.enabled=false",
        "telegram.bot.token=test-token",
        "spring.ai.openai.api-key=test",
        "spring.ai.openai.base-url=http://localhost:8080",
        "spring.ai.openai.chat.model=test-model",
        "eta.demo-students.enabled=false",
        "spring.jpa.properties.hibernate.generate_statistics=true"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class PostgresIntegrationTestBase {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    protected EntityManagerFactory entityManagerFactory;

    @Autowired
    protected EntityManager entityManager;

    protected void clearStatistics() {
        entityManagerFactory.unwrap(SessionFactory.class).getStatistics().clear();
    }

    protected Statistics statistics() {
        return entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    }

    protected void clearSession() {
        entityManager.clear();
    }

    protected QueryWindow measureWindow(Runnable action) {
        clearStatistics();
        action.run();
        var s = statistics();
        return new QueryWindow(
                s.getPrepareStatementCount(),
                s.getQueryExecutionCount(),
                s.getEntityFetchCount(),
                s.getCollectionFetchCount());
    }

    public record QueryWindow(long prepare, long queryExecution, long entityFetch, long collectionFetch) {

        public long lazyFetches() {
            return entityFetch + collectionFetch;
        }
    }
}
