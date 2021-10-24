package jp.co.rottenpear.bridge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.firewall.DefaultHttpFirewall;

@Configuration
@EnableWebSecurity
public class BridgeSimulatorSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(WebSecurity web) {
        //org.springframework.security.web.firewall.RequestRejectedException:
        //The request was rejected because the URL contained a potentially
        //malicious String ";"というエラーログがコンソールに出力されるため、下記を追加
        DefaultHttpFirewall firewall = new DefaultHttpFirewall();
        web.httpFirewall(firewall);
    }

    /**
     * SpringSecurityによる認証を設定
     *
     * @param http HttpSecurityオブジェクト
     * @throws Exception 例外
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
//        //初期表示画面を表示する際にBasic認証を実施する
//        http.antMatcher("/info").httpBasic()
//                .and()   //かつ
//                //それ以外の画面は全て認証を有効にする
//                .authorizeRequests().anyRequest().authenticated()
//                .and()   //かつ
//                //リクエスト毎に認証を実施するようにする
//                .sessionManagement().sessionCreationPolicy(
//                        SessionCreationPolicy.STATELESS);
        // (3) Basic認証の対象となるパス
        http.antMatcher("/**");

        // (4) Basic認証を指定
        http.httpBasic();

        // (5) 対象のすべてのパスに対して認証を有効にする
        http.authorizeRequests().anyRequest().authenticated();

        // (6) すべてのリクエストをステートレスとして設定
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);

    }

    @Override
    protected void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder.inMemoryAuthentication()
                .withUser("rottenpear").password(passwordEncoder().encode("ggl313")).roles("USER")
                .and().withUser("flyingblue").password(passwordEncoder().encode("flyingblue2000")).roles("USER")
                .and().withUser("7sf").password(passwordEncoder().encode("7sf2000")).roles("USER")
                .and().withUser("discard").password(passwordEncoder().encode("discard1900")).roles("USER")
                .and().withUser("flyingdance").password(passwordEncoder().encode("flyingdance2000")).roles("USER")
                .and().withUser("zhuzi").password(passwordEncoder().encode("zhuzi2000")).roles("USER")
                .and().withUser("usubmarine").password(passwordEncoder().encode("usubmarine2021")).roles("USER")
                .and().withUser("ninigege").password(passwordEncoder().encode("ninigege2021")).roles("USER")
                .and().withUser("langzi").password(passwordEncoder().encode("langzi2000")).roles("USER")
                .and().withUser("laolang").password(passwordEncoder().encode("laolang2000")).roles("USER")
                .and().withUser("huida").password(passwordEncoder().encode("huida2000")).roles("USER");

//        authenticationManagerBuilder.inMemoryAuthentication()
//                .withUser("rottenpear").password("{noop}ggl313").roles("USER")
//                .and().withUser("flyingblue").password("{noop}flyingblue2000").roles("USER")
//                .and().withUser("7sf").password("{noop}7sf2000").roles("USER")
//                .and().withUser("discard").password("{noop}discard2000").roles("USER")
//                .and().withUser("flyingdance").password("{noop}flyingdance2000").roles("USER")
//                .and().withUser("zhuzi").password("{noop}zhuzi2000").roles("USER")
//                .and().withUser("usubmarine").password("{noop}usubmarine2021").roles("USER")
//                .and().withUser("ninigege").password("{noop}ninigege2021").roles("USER")
//                .and().withUser("langzi").password("{noop}langzi2000").roles("USER")
//                .and().withUser("ninigege").password("{noop}ninigege2021").roles("USER")
//                .and().withUser("laolang").password("{noop}laolang2000").roles("USER");
//                .and().withUser("huida").password("{noop}huida2000").roles("USER");
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
