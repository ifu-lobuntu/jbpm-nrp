package org.jbpm.vdml.services.impl.model.meta;

import javax.persistence.*;
import java.io.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class Measure implements  MetaEntity{
    @Id
    private String uri;
    @Lob
    private byte[] information;

    private String name;

    @PostLoad
    public void loadInformation() {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(information));
            readInformation(objectInputStream);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
        }
    }

    @PrePersist
    @PreUpdate
    public void saveInformation() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(baos);
            writeInformation(objectOutputStream);
            objectOutputStream.flush();
            this.information=baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    protected abstract void readInformation(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public byte[] getInformation() {
        return information;
    }

    public void setInformation(byte[] information) {
        this.information = information;
    }

    protected abstract void writeInformation(ObjectOutputStream oos) throws IOException;
}
