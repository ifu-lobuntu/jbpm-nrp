package org.jbpm.vdml.services.impl.model.runtime;

import org.hibernate.annotations.Type;
import org.jbpm.vdml.services.impl.model.meta.DirectedFlow;
import org.jbpm.vdml.services.impl.model.meta.MetaEntity;
import org.jbpm.vdml.services.impl.model.meta.Port;
import org.joda.time.DateTime;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.runtime.RuntimeEntityUtil.findMatchingRuntimeEntity;

@Entity
@DiscriminatorColumn(name="type")
public class PortInstance implements RuntimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    private PortContainerInstance portContainer;
    @ManyToOne
    private Port port;
    @OneToMany(mappedBy = "port",cascade = CascadeType.ALL)
    private Set<PortMeasurement> measurements=new HashSet<PortMeasurement>();
    @OneToMany(mappedBy = "source")
    private Set<DeliverableFlowInstance> outflow=new HashSet<DeliverableFlowInstance>();
    @OneToMany(mappedBy = "target")
    private Set<DeliverableFlowInstance> inflow=new HashSet<DeliverableFlowInstance>();

    public PortInstance() {
    }

    public PortInstance(Port port, PortContainerInstance portContainer) {
        this.portContainer = portContainer;
        this.port = port;
        this.portContainer.getContainedPorts().add(this);
    }

    public PortContainerInstance getPortContainer() {
        return portContainer;
    }

    public Port getPort() {
        return port;
    }

    public Set<DeliverableFlowInstance> getOutflow() {
        return outflow;
    }

    public Set<DeliverableFlowInstance> getInflow() {
        return inflow;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public MetaEntity getMetaEntity() {
        return port;
    }

    public Set<PortMeasurement> getMeasurements() {
        return measurements;
    }

    public PortMeasurement getBatchSize() {
        return findMatchingRuntimeEntity(getMeasurements(),getPort().getBatchSize());

    }
}
