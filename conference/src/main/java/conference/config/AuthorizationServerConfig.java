package conference.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.approval.UserApprovalHandler;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

@Configuration
@EnableAuthorizationServer
//@PropertySource("classpath:conference.properties")
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    private static final String RESOURCE_ID = "conference";

    @Autowired
    private TokenStore tokenStore;

    @Autowired
    private UserApprovalHandler userApprovalHandler;

    @Autowired
    @Qualifier("authenticationManagerBean")
    private AuthenticationManager authenticationManager;

    //@Value("${redirect:http://localhost:8080/client/conference/redirect}")
    //@Value("${redirect:contextRootURL:/client/conference/redirect}")
    @Value("${redirect:http://localhost:8080/client/conference/speakers}")
    private String redirectUri;

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    	
    	clients.inMemory().withClient("client")
            .resourceIds(RESOURCE_ID)
            .authorizedGrantTypes("authorization_code", "implicit")
            .authorities("ROLE_CLIENT")
            .scopes("read", "write")
            .secret("secret")
        .and()
            .withClient("client-with-redirect")
            .resourceIds(RESOURCE_ID)
            .authorizedGrantTypes("authorization_code", "implicit")
            .authorities("ROLE_CLIENT")
            .scopes("read", "write")
            .secret("secret")
            .redirectUris(redirectUri)
        .and()
            .withClient("my-client-with-registered-redirect")
            .resourceIds(RESOURCE_ID)
            .authorizedGrantTypes("authorization_code", "client_credentials")
            .authorities("ROLE_CLIENT")
            .scopes("read", "trust")
            .redirectUris("http://anywhere?key=value");
        
    }

    @Bean
    public TokenStore tokenStore() {
        //return new InMemoryTokenStore();
    	return new JwtTokenStore(tokenEnhancer());
    }
    
    @Bean
    public JwtAccessTokenConverter tokenEnhancer(){
        final JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
        jwtAccessTokenConverter.setSigningKey("pr1v4t3Sh4r3d"); //FIXME externalizar
        jwtAccessTokenConverter.setVerifierKey("pr1v4t3Sh4r3d"); //FIXME externalizar
        return jwtAccessTokenConverter;
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.tokenStore(tokenStore).userApprovalHandler(userApprovalHandler)
                .authenticationManager(authenticationManager)
                .accessTokenConverter(tokenEnhancer());
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        oauthServer.realm("conference/client");
    }
    
    @Bean
   	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
   		return new PropertySourcesPlaceholderConfigurer();
   	}
}
