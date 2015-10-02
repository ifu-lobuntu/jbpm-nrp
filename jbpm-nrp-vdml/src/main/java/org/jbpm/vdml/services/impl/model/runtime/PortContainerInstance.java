package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.DeliverableFlow;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class PortContainerInstance implements RuntimeEntity, Measurand{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    private RolePerformance responsibleRolePerformance;


    @OneToMany(mappedBy = "targetPortContainer")
    private Set<DeliverableFlowInstance> concludedFlow = new HashSet<DeliverableFlowInstance>();
    @OneToMany(mappedBy = "sourcePortContainer")
    private Set<DeliverableFlowInstance> commencedFlow = new HashSet<DeliverableFlowInstance>();

    public PortContainerInstance() {
    }


    @Override
    public Long getId() {
        return id;
    }
    public Set<DeliverableFlowInstance> getConcludedFlow() {
        return concludedFlow;
    }

    public Set<DeliverableFlowInstance> getInputDeliverableFlows() {
        Set<DeliverableFlowInstance> result = new HashSet<DeliverableFlowInstance>();
        for (DeliverableFlowInstance f : concludedFlow) {
            if(f.getDirectedFlow() instanceof DeliverableFlow){
                result.add(f);
            }
        }
        return result;
    }

    public Set<DeliverableFlowInstance> getCommencedFlow() {
        return commencedFlow;
    }

    protected RolePerformance getResponsibleRolePerformance() {
        return responsibleRolePerformance;
    }

    protected void setResponsibleRolePerformance(RolePerformance responsibleRolePerformance) {
        this.responsibleRolePerformance = responsibleRolePerformance;
    }
}
