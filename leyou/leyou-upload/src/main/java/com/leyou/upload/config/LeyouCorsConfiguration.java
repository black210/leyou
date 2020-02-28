package com.leyou.upload.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class LeyouCorsConfiguration {
    @Bean
    public CorsFilter corsFilter(){
        //1.添加CORS配置信息
        CorsConfiguration config =new CorsConfiguration();
        //允许的域，不要写*否则cookie就无法使用了
        config.addAllowedOrigin("http://manage.leyou.com");
        //是否发送Cookie信息
        config.setAllowCredentials(true);
        //允许的请求方式
//        config.addAllowedMethod("OPTION");
//        config.addAllowedMethod("HEAD");
//        config.addAllowedMethod("GET");
//        config.addAllowedMethod("PUT");
//        config.addAllowedMethod("POST");
//        config.addAllowedMethod("DELETE");
//        config.addAllowedMethod("PATCH");
        config.addAllowedMethod("*");
        //允许的头信息
        config.addAllowedHeader("*");
        //2.cors配置源对象。添加映射路径。拦截一切请求
        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
        configSource.registerCorsConfiguration("/**",config);
        //3.返回新的corsFilter
        return new CorsFilter(configSource);
    }
}
