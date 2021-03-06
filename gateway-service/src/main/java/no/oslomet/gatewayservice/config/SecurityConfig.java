package no.oslomet.gatewayservice.config;

import no.oslomet.gatewayservice.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;


@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public AccessDeniedHandler accessDeniedHandler(){
        return new CustomAccessDeniedHandler();
    }

    @Autowired
    private LoginService loginService;

    @Override
    protected void configure(AuthenticationManagerBuilder authBuilder) throws Exception {
        authBuilder.userDetailsService(loginService);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.headers().frameOptions().disable();
        http.csrf().disable();
        http.authorizeRequests()
                //permitAll
                .antMatchers(
                        //pages
                        "/", "/index",
                        //api
                        "/users/save",
                        //resources
                        "/favicon.ico", "/images/bird.png", "/images/bird_.png").permitAll()
                //user/admin
                .antMatchers(
                        //api
                        "/tweets/**", "/follows/**", "/users/**").hasAnyRole("USER", "ADMIN")
                //admin
                .antMatchers("/userservice/users/**", "/tweetservice/tweets/**", "/followservice/follows/**").hasRole("ADMIN")
                .anyRequest().authenticated()

                .and()
                .exceptionHandling().accessDeniedHandler(accessDeniedHandler())

                .and()
                .formLogin()
                .loginPage("/")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/")
                .failureUrl("/errors/credentialError")
                .permitAll()

                .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .permitAll()

                .and()
                .sessionManagement()
                .maximumSessions(1)
                .expiredUrl("/");
    }
}
