package core.configuration;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    protected Class<?>[] getRootConfigClasses() {
        return new Class[] {AppConfiguration.class, SecurityConfiguration.class};
    }

    protected Class<?>[] getServletConfigClasses() {
        return new Class[] {AppConfiguration.class, SecurityConfiguration.class};
    }

    protected String[] getServletMappings() {
        return new String[] {"/"};
    }
}
