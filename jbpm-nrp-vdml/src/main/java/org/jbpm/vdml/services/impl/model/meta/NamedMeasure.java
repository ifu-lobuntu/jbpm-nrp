package org.jbpm.vdml.services.impl.model.meta;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

@Entity
@DiscriminatorValue("Named")
public class NamedMeasure extends Measure{
    @Override
    protected void readInformation(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {

    }

    @Override
    protected void writeInformation(ObjectOutputStream oos) throws IOException {

    }
}
