@file:Suppress("DEPRECATION")
package com.kio.configuration.security.oauth

import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer
import org.springframework.security.core.token.TokenService
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer
import org.springframework.security.oauth2.provider.token.TokenStore

@Configuration
@EnableResourceServer
class OAuth2ResourceServerConfiguration : ResourceServerConfigurerAdapter(){

    override fun configure(resources: ResourceServerSecurityConfigurer) {
        resources.resourceId("kio-id")
            // .tokenStore(tokenStore) // Token store is only required when both apps are splitted
            // there's already a bean of tokenstore in the context that this rs will use
            //.authenticationManager(authenticationManager)
    }

    override fun configure(http: HttpSecurity) {
        http.authorizeRequests {
            it.antMatchers("/api/v1/pfp/me").authenticated()
            it.antMatchers(HttpMethod.POST, "/api/v1/users").anonymous()
            it.antMatchers(HttpMethod.GET, "/api/v1/pfp/{id}", "/api/v1/pfp/user/{id}").permitAll()
            it.anyRequest().permitAll()
        }
    }

}