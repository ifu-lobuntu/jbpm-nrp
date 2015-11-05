package org.jbpm.vdml.services.impl.model.meta;

import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity

@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
@org.hibernate.annotations.DiscriminatorOptions(force=true)
public abstract class Port implements MetaEntity,MeasurableElement{
    @Id
    private String uri;
    private String name;
    @ManyToOne
    private Measure batchSize;
    @ManyToOne
    private PortContainer portContainer;
    @ManyToMany
    @JoinTable(name="port_measure")
    private Collection<Measure> measures=new HashSet<Measure>();
    @OneToMany(mappedBy = "source",cascade = CascadeType.ALL)
    private Set<DirectedFlow> outflows=new HashSet<DirectedFlow>();
    @OneToMany(mappedBy = "target")
    private Set<DirectedFlow> inflows=new HashSet<DirectedFlow>();

    public Port() {
    }

    protected  Port(String uri, PortContainer portContainer){
        this.uri = uri;
        this.portContainer=portContainer;
        this.portContainer.getContainedPorts().add(this);

    }
    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Collection<Measure> getMeasures() {
        return measures;
    }

    public Measure getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Measure batchSize) {
        this.batchSize = batchSize;
    }

    public Set<DirectedFlow> getOutflows() {
        return outflows;
    }

    public Set<DirectedFlow> getInflows() {
        return inflows;
    }

    public  PortContainer getPortContainer(){
        return portContainer;
    }


}
