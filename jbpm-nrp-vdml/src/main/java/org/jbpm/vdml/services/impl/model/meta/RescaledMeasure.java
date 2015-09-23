package org.jbpm.vdml.services.impl.model.meta;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

@Entity
@DiscriminatorValue("Rescaled")
public class RescaledMeasure extends Measure {
    @Transient
    private Double offset;
    @Transient
    private Double multiplier;
    @Transient
    private EmfReference rescaledMeasure;

    public Double getOffset() {
        return offset;
    }

    public void setOffset(Double offset) {
        this.offset = offset;
    }

    public Double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(Double multiplier) {
        this.multiplier = multiplier;
    }

    public EmfReference getRescaledMeasure() {
        return rescaledMeasure;
    }

    public void setRescaledMeasure(EmfReference rescaledMeasure) {
        this.rescaledMeasure = rescaledMeasure;
    }

    @Override
    protected void readInformation(ObjectInputStream objectInputStream) throws IOException {
        this.rescaledMeasure=new EmfReference(objectInputStream.readUTF());
        this.offset = objectInputStream.readDouble();
        this.multiplier = objectInputStream.readDouble();
    }

    @Override
    protected void writeInformation(ObjectOutputStream oos) throws IOException {
        oos.writeUTF(rescaledMeasure.getUri());
        oos.writeDouble(offset);
        oos.writeDouble(multiplier);
    }
}
