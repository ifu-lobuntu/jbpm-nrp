package org.jbpm.vdml.services.impl.model.meta;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

@Entity
@DiscriminatorValue("Counting")
public class CountingMeasure extends Measure {
    @Transient
    private EmfReference measureToCount;
    @Transient
    private String valuesToCount;



    @Override
    protected void readInformation(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        this.measureToCount=new EmfReference(objectInputStream.readUTF());
        this.valuesToCount= objectInputStream.readUTF();
    }

    @Override
    protected void writeInformation(ObjectOutputStream oos) throws IOException {
        oos.writeUTF(measureToCount.getUri());
        oos.writeUTF(valuesToCount);
    }

    public EmfReference getMeasureToCount() {
        return measureToCount;
    }

    public void setMeasureToCount(EmfReference measureToCount) {
        this.measureToCount = measureToCount;
    }

    public String getValuesToCount() {
        return valuesToCount;
    }

    public void setValuesToCount(String valuesToCount) {
        this.valuesToCount = valuesToCount;
    }
}
