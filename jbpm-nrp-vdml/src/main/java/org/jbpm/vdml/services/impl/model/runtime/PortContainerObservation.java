package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.DeliverableFlow;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class PortContainerObservation implements RuntimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    @OneToMany(mappedBy = "targetPortContainer")
    private Set<DirectedFlowObservation> concludedFlow = new HashSet<DirectedFlowObservation>();
    @OneToMany(mappedBy = "sourcePortContainer")
    private Set<DirectedFlowObservation> commencedFlow = new HashSet<DirectedFlowObservation>();

    public PortContainerObservation() {
    }


    @Override
    public Long getId() {
        return id;
    }
    public Set<DirectedFlowObservation> getConcludedFlow() {
        return concludedFlow;
    }

    public Set<DirectedFlowObservation> getInputDeliverableFlows() {
        Set<DirectedFlowObservation> result = new HashSet<DirectedFlowObservation>();
        for (DirectedFlowObservation f : concludedFlow) {
            if(f.getDirectedFlow() instanceof DeliverableFlow){
                result.add(f);
            }
        }
        return result;
    }


    public Set<DirectedFlowObservation> getCommencedFlow() {
        return commencedFlow;
    }


}
