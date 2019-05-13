package cn.taroco.oauth2.server.config;

import cn.taroco.common.config.TarocoOauth2Properties;
import cn.taroco.oauth2.server.extend.mobile.MobileSecurityConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

/**
 * webSecurity 权限控制类
 *
 * @author liuht
 * @date 2018/7/24 15:58
 */
@EnableWebSecurity
public class WebSecurityConfigration extends WebSecurityConfigurerAdapter {

    @Autowired
    private TarocoOauth2Properties oauth2Properties;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private MobileSecurityConfigurer mobileSecurityConfigurer;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry =
                http
                        .formLogin()
                        .loginPage("/authentication/require").permitAll()
                        .loginProcessingUrl("/authentication/form")
                        .failureUrl("/authentication/require?error=true")
                        .and().logout().logoutUrl("/authentication/logout").permitAll().logoutSuccessUrl("/authentication/require?logout=true")
                        .and().authorizeRequests();

        final List<String> urlPermitAll = oauth2Properties.getUrlPermitAll();
        urlPermitAll.forEach(url -> registry.antMatchers(url).permitAll());
        registry.anyRequest().authenticated().and().csrf().disable();
        // 聚合手机号登录配置
        http.apply(mobileSecurityConfigurer);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/static/**");
    }

    /**
     * 加密器 spring boot 2.x没有默认的加密器了
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 这一步的配置是必不可少的，否则SpringBoot会自动配置一个AuthenticationManager,覆盖掉内存中的用户
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
