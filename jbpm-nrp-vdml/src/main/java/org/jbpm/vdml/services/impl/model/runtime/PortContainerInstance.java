package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.runtime.RuntimeEntityUtil.findMatchingRuntimeEntity;

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
    @OneToMany(mappedBy = "portContainer",cascade = CascadeType.ALL)
    private Set<PortInstance> containedPorts= new HashSet<PortInstance>();

    public PortContainerInstance() {
    }


    @Override
    public Long getId() {
        return id;
    }

    public Set<PortInstance> getContainedPorts() {
        return containedPorts;
    }
    public Set<OutputPortInstance> getOutputPorts(){
        return findPorts(OutputPortInstance.class);
    }
    public Set<InputPortInstance> getInputPorts(){
        return findPorts(InputPortInstance.class);
    }

    protected <T extends PortInstance> Set<T> findPorts(Class<T> aClass) {
        Set<T> result = new HashSet<T>();
        for (PortInstance port : containedPorts) {
            if(aClass.isInstance(port)){
                result.add((T) port);
            }
        }
        return result;
    }

    public Set<DeliverableFlowInstance> getConcludedFlow() {
        return concludedFlow;
    }

    public Set<DeliverableFlowInstance> getInputDeliverableFlows() {
        Set<DeliverableFlowInstance> result = new HashSet<DeliverableFlowInstance>();
        for (DeliverableFlowInstance f : concludedFlow) {
            if(f.getDeliverableFlow() instanceof DeliverableFlow){
                result.add(f);
            }
        }
        return result;
    }

    public Set<DeliverableFlowInstance> getCommencedFlow() {
        return commencedFlow;
    }

    public RolePerformance getResponsibleRolePerformance() {
        return responsibleRolePerformance;
    }

    protected void setResponsibleRolePerformance(RolePerformance responsibleRolePerformance) {
        this.responsibleRolePerformance = responsibleRolePerformance;
    }

    public OutputPortInstance findOutputPort(OutputPort port) {
        return (OutputPortInstance) findMatchingRuntimeEntity(getContainedPorts(),port);
    }
    public InputPortInstance findInputPort(InputPort port) {
        return (InputPortInstance)findMatchingRuntimeEntity(getContainedPorts(), port);
    }

    public Set<ValueAddInstance> findValueAdds(Set<ValueAdd> valueAddsAggregatedFrom) {
        Set<ValueAddInstance> result=new HashSet<ValueAddInstance>();
        for (PortInstance pi : containedPorts) {
            if(pi instanceof OutputPortInstance){
                for (ValueAddInstance valueAddInstance : ((OutputPortInstance) pi).getValueAdds()) {
                    if(valueAddsAggregatedFrom.contains(valueAddInstance.getValueAdd())){
                        result.add(valueAddInstance);
                    }
                }
            }
        }
        return result;
    }
}
