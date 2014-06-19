import java.util.Map;
import org.junit.Test;
public class SystemEnvironmentTest {

    @Test
    public void printSystemEnvironment() {
        System.err.println("********************************************************************************");
        System.err.println("** SYSTEM ENVIRONMENT **********************************************************");
        System.err.println("********************************************************************************");
        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            System.err.println(entry.getKey() + ": " + entry.getValue());
        }
        System.err.println("********************************************************************************");
        System.err.println("");
        System.err.println("********************************************************************************");
        System.err.println("** SYSTEM PROPERTIES ***********************************************************");
        System.err.println("********************************************************************************");
        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            System.err.println(entry.getKey() + ": " + entry.getValue());
        }
        System.err.println("********************************************************************************");
    }

}
