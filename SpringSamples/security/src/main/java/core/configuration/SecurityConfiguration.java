package core.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    private Logger logger = Logger.getLogger(SecurityConfiguration.class.getSimpleName());


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/resources/**").permitAll()
                .antMatchers(HttpMethod.GET, "/").authenticated()
                .antMatchers(HttpMethod.GET, "/admin/**").hasAuthority("ADMIN")
                .antMatchers(HttpMethod.GET, "/dba/**").hasAnyAuthority("ADMIN", "DBA")
                .antMatchers(HttpMethod.GET, "/developer/**").hasAnyAuthority("ADMIN", "DEVELOPER")
                .anyRequest().authenticated()
                .and()
                .formLogin().permitAll()
                .and()
                .httpBasic()
                .and()
                .csrf().disable()
                .logout()
                .logoutSuccessUrl("/login");
    }

    @Override
    protected UserDetailsService userDetailsService() {
        User.UserBuilder userBuilder = User.withDefaultPasswordEncoder();
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();

        manager.createUser(userBuilder.username("user").password("user").authorities("USER").build());
        manager.createUser(userBuilder.username("admin").password("admin").authorities("ADMIN", "ALL").build());
        manager.createUser(userBuilder.username("dba").password("dba").authorities("DBA", "DBA_AUTHORITIES").build());
        manager.createUser(userBuilder.username("seniordba").password("seniordba").authorities("DBA", "SENIOR_DBA_AUTHORITIES").build());
        manager.createUser(userBuilder.username("developer").password("developer").authorities("DEVELOPER", "DEVELOPER_AUTHORITIES").build());
        manager.createUser(userBuilder.username("seniordeveloper").password("seniordeveloper").authorities("DEVELOPER", "SENIOR_DEVELOPER_AUTHORITIES").build());

        return manager;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService());
    }
}
