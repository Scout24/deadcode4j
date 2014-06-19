package de.is24.deadcode4j.analyzer.constants;
import java.util.Random;
import static de.is24.deadcode4j.analyzer.constants.Constants.BAR;
@SuppressWarnings("UnusedDeclaration")
public class ClassUsingConstantViaStaticImportInSwitch {
    @Override
    public String toString() {
        int random = new Random().nextInt();
        switch (random) {
            case BAR: return "bar";
            default: return "foo";
        }
    }
}
