package core.configuration;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    protected Class<?>[] getRootConfigClasses() {
        return new Class[] {App.class};
    }

    protected Class<?>[] getServletConfigClasses() {
        return new Class[] {App.class};
    }

    protected String[] getServletMappings() {
        return new String[] {"/"};
    }
}
