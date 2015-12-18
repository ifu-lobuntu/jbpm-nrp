package org.jbpm.vdml.services.impl.model.meta;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

@Entity
@DiscriminatorValue("Enumerated")
public class EnumeratedMeasure extends Measure {
    private Class<? extends Enum<?>> enumClass;
    @Transient
    private EmfReference targetMeasure;

    @Override
    protected void readInformation(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        //TODO resolve from Name, uri/package
        enumClass = (Class<Enum<?>>) objectInputStream.readObject();
        String uri= (String) objectInputStream.readObject();
        if(uri!=null){
            targetMeasure=new EmfReference(uri);
        }
    }

    @Override
    protected void writeInformation(ObjectOutputStream oos) throws IOException {
        oos.writeObject(enumClass);
        oos.writeObject(targetMeasure==null?null:targetMeasure.getUri());

    }

    public Class<? extends Enum<?>> getEnumClass() {
        return enumClass;
    }

    public void setEnumClass(Class<? extends Enum<?>> enumClass) {
        this.enumClass = enumClass;
    }

    public void setTargetMeasure(EmfReference targetMeasure) {
        this.targetMeasure = targetMeasure;
    }

    public EmfReference getTargetMeasure() {
        return targetMeasure;
    }
}
