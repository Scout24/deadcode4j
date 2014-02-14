import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Set;

@SuppressWarnings("UnusedDeclaration")
public class SomeServletInitializer implements ServletContainerInitializer {
    @Override
    public void onStartup(Set<Class<?>> classes, ServletContext servletContext) throws ServletException {

    }
}
