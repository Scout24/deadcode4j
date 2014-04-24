import java.io.Externalizable;
import java.io.ObjectInput;
import java.io.ObjectOutput;

@SuppressWarnings("UnusedDeclaration")
public class ClassImplementingExternalizable implements Externalizable {
    @Override
    public void writeExternal(ObjectOutput out) {
    }

    @Override
    public void readExternal(ObjectInput in) {
    }

}
