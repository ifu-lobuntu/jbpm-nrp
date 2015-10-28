package org.jbpm.vdml.services.impl.model.runtime;

import org.hibernate.annotations.Type;
import org.jbpm.vdml.services.impl.model.meta.DirectedFlow;
import org.jbpm.vdml.services.impl.model.meta.MetaEntity;
import org.jbpm.vdml.services.impl.model.meta.Port;
import org.joda.time.DateTime;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class PortInstance implements RuntimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    private PortContainerInstance portContainer;
    @ManyToOne
    private Port port;
    @OneToMany(mappedBy = "deliverableFlow")
    private Set<PortMeasurement> measurements=new HashSet<PortMeasurement>();

    public PortInstance() {
    }

    public PortInstance(PortContainerInstance portContainer, Port port) {
        this.portContainer = portContainer;
        this.port = port;
        this.portContainer.getContainedPorts().add(this);
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public MetaEntity getMetaEntity() {
        return port;
    }
}
