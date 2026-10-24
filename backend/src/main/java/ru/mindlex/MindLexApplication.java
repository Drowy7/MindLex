package ru.mindlex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Точка входа MindLex API.
 *
 * <p>Приложение не хранит состояние между запросами: аутентификация построена
 * на JWT, данные — в PostgreSQL. Это позволяет запускать несколько экземпляров
 * за балансировщиком без дополнительной настройки (факторы VI и VIII).</p>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class MindLexApplication {

    public static void main(String[] args) {
        SpringApplication.run(MindLexApplication.class, args);
    }
}
