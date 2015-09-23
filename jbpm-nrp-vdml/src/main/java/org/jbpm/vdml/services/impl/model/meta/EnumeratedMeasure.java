package org.jbpm.vdml.services.impl.model.meta;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

@Entity
@DiscriminatorValue("Enumerated")
public class EnumeratedMeasure extends Measure {
    private Class<? extends Enum<?>> enumClass;

    @Override
    protected void readInformation(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        //TODO resolve from Name, uri/package
        enumClass = (Class<Enum<?>>) objectInputStream.readObject();
    }

    @Override
    protected void writeInformation(ObjectOutputStream oos) throws IOException {
        oos.writeObject(enumClass);
    }

    public Class<? extends Enum<?>> getEnumClass() {
        return enumClass;
    }

    public void setEnumClass(Class<? extends Enum<?>> enumClass) {
        this.enumClass = enumClass;
    }
}
