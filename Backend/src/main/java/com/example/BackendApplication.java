package com.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//告诉Spring Boot，在启动时要扫描的包,将其注册为Bean(Bean是Spring的核心概念,可以理解为一个实例,可以被Spring管理,通过Bean操作数据库)
@MapperScan("com.example.mapper")
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
