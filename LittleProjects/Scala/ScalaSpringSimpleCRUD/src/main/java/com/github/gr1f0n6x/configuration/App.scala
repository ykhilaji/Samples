package com.github.gr1f0n6x.configuration

import org.springframework.context.annotation.{ComponentScan, Configuration, Import}
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@Configuration
@Import(Array(classOf[Source]))
@ComponentScan(basePackages = Array("com.github.gr1f0n6x"))
@EnableWebMvc
class App {

}
