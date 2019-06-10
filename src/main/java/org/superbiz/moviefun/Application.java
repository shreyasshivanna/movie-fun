package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Collections;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    @Bean
    public DatabaseServiceCredentials get()
    {
        return new DatabaseServiceCredentials(System.getenv("VCAP_SERVICES"));
    }

    @Bean(name="albums-datasource")
    public DataSource albumsDataSource(DatabaseServiceCredentials serviceCredentials) {
        MysqlDataSource source = new MysqlDataSource();
        source.setURL(serviceCredentials.jdbcUrl("albums-mysql"));
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDataSource(source);
        return hikariDataSource;
    }

    @Bean(name="movies-datasource")
    public DataSource moviesDataSource(DatabaseServiceCredentials serviceCredentials) {
        MysqlDataSource source = new MysqlDataSource();
        source.setURL(serviceCredentials.jdbcUrl("movies-mysql"));
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDataSource(source);
        return hikariDataSource;
    }

    @Bean
    public HibernateJpaVendorAdapter getJpa()
    {
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setDatabase(Database.MYSQL);
        hibernateJpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL5InnoDBDialect");
        hibernateJpaVendorAdapter.setGenerateDdl(true);
        return hibernateJpaVendorAdapter;
    }

    @Bean(name="albums")
    public LocalContainerEntityManagerFactoryBean albumsManagerFactory(@Autowired @Qualifier("albums-datasource")DataSource dataSource, HibernateJpaVendorAdapter hibernateJpa ) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("org.superbiz.moviefun");
        emf.setJpaVendorAdapter(hibernateJpa);
        // emf.setJpaPropertyMap(Collections.singletonMap("javax.persistence.validation.mode", "none"));
        emf.setPersistenceUnitName("albums");
        return emf;
    }

    @Bean(name="movies")
    public LocalContainerEntityManagerFactoryBean moviesManagerFactory(@Autowired @Qualifier("movies-datasource")DataSource dataSource,HibernateJpaVendorAdapter hibernateJpa ) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("org.superbiz.moviefun");
        emf.setJpaVendorAdapter(hibernateJpa);
        //  emf.setJpaPropertyMap(Collections.singletonMap("javax.persistence.validation.mode", "none"));
        emf.setPersistenceUnitName("movies");
        return emf;
    }


    @Bean(name = "albums-TransactionManager")
    public PlatformTransactionManager getAlbumTM(@Autowired @Qualifier("albums") EntityManagerFactory album) {
        JpaTransactionManager manager = new JpaTransactionManager(album);
        return manager;
    }

    @Bean(name = "movies-TransactionManager")
    public PlatformTransactionManager getMoviesTM(@Autowired @Qualifier("movies") EntityManagerFactory movie) {
        JpaTransactionManager manager = new JpaTransactionManager(movie);
        return manager;
    }

}
