package com.roufid.tutorial.configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.roufid.tutorial.entity.h2sql.Book;


@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
		entityManagerFactoryRef = "h2sqlEntityManager", 
		transactionManagerRef = "h2sqlTransactionManager", 
		basePackages = "com.roufid.tutorial.dao.h2sql"
)
public class h2sqlConfiguration {

	/**
	 * PostgreSQL datasource definition.
	 * 
	 * @return datasource.
	 */
	@Bean
	@Primary
	@ConfigurationProperties(prefix = "spring.h2.datasource")
	public DataSource h2sqlDataSource() {
		return DataSourceBuilder
					.create()
					.build();
	}

	/**
	 * Entity manager definition. 
	 *  
	 * @param builder an EntityManagerFactoryBuilder.
	 * @return LocalContainerEntityManagerFactoryBean.
	 */
	@Primary
	@Bean(name = "h2sqlEntityManager")
	public LocalContainerEntityManagerFactoryBean h2sqlEntityManagerFactory(EntityManagerFactoryBuilder builder) {
		return builder
					.dataSource(h2sqlDataSource())
					.properties(hibernateProperties())
					.packages(Book.class)
					.persistenceUnit("h2sqlPU")
					.build();
	}

	@Primary
	@Bean(name = "h2sqlTransactionManager")
	public PlatformTransactionManager h2sqlTransactionManager(@Qualifier("h2sqlEntityManager") EntityManagerFactory entityManagerFactory) {
		return new JpaTransactionManager(entityManagerFactory);
	}

	private Map<String, Object> hibernateProperties() {

		Resource resource = new ClassPathResource("hibernate.properties");
		
		try {
			Properties properties = PropertiesLoaderUtils.loadProperties(resource);
			return properties.entrySet().stream()
											.collect(Collectors.toMap(
														e -> e.getKey().toString(),
														e -> e.getValue())
													);
		} catch (IOException e) {
			return new HashMap<String, Object>();
		}
	}
}
