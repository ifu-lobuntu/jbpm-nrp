package org.jbpm.vdml.services.impl.model.meta;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("Collective")
public class CollectiveMeasure extends Measure {
    @Transient
    private Set<EmfReference> aggregatedMeasures = new HashSet<EmfReference>();
    @Transient
    private Accumulator accumulator;


    public Set<EmfReference> getAggregatedMeasures() {
        return aggregatedMeasures;
    }

    public Accumulator getAccumulator() {
        return accumulator;
    }

    public void setAccumulator(Accumulator accumulator) {
        this.accumulator = accumulator;
    }

    @Override
    protected void readInformation(ObjectInputStream objectInputStream) throws IOException {
        int size = objectInputStream.readInt();
        for (int i = 0; i < size; i++) {
            aggregatedMeasures.add(new EmfReference(objectInputStream.readUTF()));
        }
        accumulator = Accumulator.valueOf(objectInputStream.readUTF());
    }

    @Override
    protected void writeInformation(ObjectOutputStream oos) throws IOException {
        oos.writeInt(aggregatedMeasures.size());
        for (EmfReference measure : aggregatedMeasures) {
            oos.writeUTF(measure.getUri());
        }
        oos.writeUTF(accumulator.name());

    }
}
