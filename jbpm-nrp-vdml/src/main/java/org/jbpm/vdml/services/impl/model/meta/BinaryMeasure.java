package org.jbpm.vdml.services.impl.model.meta;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

@Entity
@DiscriminatorValue("Binary")
public class BinaryMeasure extends Measure {
    @Transient
    private EmfReference measureA;
    @Transient
    private EmfReference measureB;
    @Transient
    private BinaryFunctor functor;

    public EmfReference getMeasureA() {
        return measureA;
    }

    @Override
    protected void readInformation(ObjectInputStream oos) throws IOException {
        this.measureA = new EmfReference(oos.readUTF());
        this.measureB = new EmfReference(oos.readUTF());
        this.functor = BinaryFunctor.valueOf(oos.readUTF());
    }

    @Override
    protected void writeInformation(ObjectOutputStream oos) throws IOException {
        oos.writeUTF(this.measureA.getUri());
        oos.writeUTF(this.measureB.getUri());
        oos.writeUTF(this.functor.name());
    }

    public void setMeasureA(EmfReference measureA) {
        this.measureA = measureA;
    }

    public EmfReference getMeasureB() {
        return measureB;
    }

    public void setMeasureB(EmfReference measureB) {
        this.measureB = measureB;
    }

    public BinaryFunctor getFunctor() {
        return functor;
    }

    public void setFunctor(BinaryFunctor functor) {
        this.functor = functor;
    }
}
