package com.soumyajit.E_Grocery.Shop.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class Configuration {
    @Bean
    ModelMapper getBean(){
        return new ModelMapper();
    }
}
