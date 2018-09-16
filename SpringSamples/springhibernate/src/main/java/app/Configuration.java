package app;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@org.springframework.context.annotation.Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = "app")
public class Configuration {
    @Bean
    public LocalSessionFactoryBean localSessionFactoryBean() {
        LocalSessionFactoryBean bean = new LocalSessionFactoryBean();

        bean.setPackagesToScan("app");
        bean.setDataSource(dataSource());
        bean.setHibernateProperties(properties());

        return bean;
    }

    @Bean
    public DataSource dataSource() {
        BasicDataSource dataSource = new BasicDataSource();

        dataSource.setUsername("postgres");
        dataSource.setPassword("");
        dataSource.setUrl("jdbc:postgresql://192.168.99.100:5432/postgres");
        dataSource.setDefaultAutoCommit(false);
        dataSource.setDriverClassName("org.postgresql.Driver");

        return dataSource;
    }

    @Bean
    public PlatformTransactionManager platformTransactionManager() {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();

        transactionManager.setSessionFactory(localSessionFactoryBean().getObject());
        transactionManager.setAllowResultAccessAfterCompletion(true);
        transactionManager.setRollbackOnCommitFailure(true);

        return transactionManager;
    }

    private Properties properties() {
        Properties properties = new Properties();

        properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQL95Dialect");

        return properties;
    }
}
