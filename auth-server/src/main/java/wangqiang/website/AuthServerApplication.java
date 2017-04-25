package wangqiang.website;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import wangqiang.website.service.security.MongoUserDetailsService;

@SpringBootApplication
@EnableDiscoveryClient // eureka的发现客户端
@EnableResourceServer // 拥有权限资源的服务器
@EnableGlobalMethodSecurity(prePostEnabled = true)  //对方法实现权限控制
public class AuthServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServerApplication.class, args);
	}

	/**
	 * 对本省的资源进行权限限制
	 */
	@Configuration
	@EnableWebSecurity //开启security限制
	protected static class webSecurityConfig extends WebSecurityConfigurerAdapter {

		@Autowired
		private MongoUserDetailsService userDetailsService;

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			// @formatter:off
			http
					.authorizeRequests().anyRequest().authenticated()
					.and()
					.csrf().disable();
			// @formatter:on
		}

		@Override
		protected void configure(AuthenticationManagerBuilder auth) throws Exception {
			auth.userDetailsService(userDetailsService)
					.passwordEncoder(new BCryptPasswordEncoder());
		}

		@Override
		@Bean
		public AuthenticationManager authenticationManagerBean() throws Exception {
			return super.authenticationManagerBean();
		}
	}

	/**
	 * 授权服务器
	 */
	@Configuration
	@EnableAuthorizationServer // 配置授权逻辑
	protected static class OAuth2AuthorizationConfig extends AuthorizationServerConfigurerAdapter {

		private TokenStore tokenStore = new InMemoryTokenStore();

		@Autowired
		@Qualifier("authenticationManagerBean")
		private AuthenticationManager authenticationManager;

		@Autowired
		private MongoUserDetailsService userDetailsService;

		@Autowired
		private Environment env;

		@Override
		public void configure(ClientDetailsServiceConfigurer clients) throws Exception {

			// TODO persist clients details

			// @formatter:off
			clients.inMemory()
					.withClient("browser") // client_id
					.authorizedGrantTypes("refresh_token", "password") // 该client允许的授权类型
					.scopes("ui") // 允许的授权范围
					.and()
					.withClient("account-service")
					.secret(env.getProperty("ACCOUNT_SERVICE_PASSWORD"))
					.authorizedGrantTypes("client_credentials", "refresh_token")
					.scopes("server")
					.and()
					.withClient("statistics-service")
					.secret(env.getProperty("STATISTICS_SERVICE_PASSWORD")) // client_secret
					.authorizedGrantTypes("client_credentials", "refresh_token")
					.scopes("server")
					.and()
					.withClient("notification-service")
					.secret(env.getProperty("NOTIFICATION_SERVICE_PASSWORD"))
					.authorizedGrantTypes("client_credentials", "refresh_token")
					.scopes("server");
			// @formatter:on
		}

		@Override
		public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
			endpoints
					.tokenStore(tokenStore)
					.authenticationManager(authenticationManager)
					.userDetailsService(userDetailsService);
		}

		@Override
		public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
			oauthServer
					.tokenKeyAccess("permitAll()")
					.checkTokenAccess("isAuthenticated()");
		}
	}
}
