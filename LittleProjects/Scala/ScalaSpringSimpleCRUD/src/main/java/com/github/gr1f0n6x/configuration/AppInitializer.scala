package com.github.gr1f0n6x.configuration

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer

class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
  override def getServletConfigClasses = Array(classOf[App], classOf[Source])

  override def getRootConfigClasses = Array(classOf[App], classOf[Source])

  override def getServletMappings = Array("/")
}
