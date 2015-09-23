package org.jbpm.vdml.services.impl;

import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.time.JobContext;
import org.drools.core.time.JobHandle;
import org.jbpm.process.core.timer.NamedJobContext;
import org.jbpm.vdml.services.impl.model.runtime.ResourceUseObservation;

public class ScheduledResourceUseJobContext implements JobContext, NamedJobContext {

    private static final long serialVersionUID = -6838102884655245L;
    private final ResourceUseObservation resourceUse;
    private JobHandle jobHandle;
    private Long resourceUseObservationId;
    private String entityManagerFactoryName;
    private String id;

    public ScheduledResourceUseJobContext(String jobType, ResourceUseObservation resourceUse) {
        this.resourceUse = resourceUse;
        id = jobType + "_" + resourceUse.getActivity().getCollaboration().getId() + "_" + resourceUse.getActivity().getId() + "_" + resourceUse.getId();
        resourceUseObservationId = resourceUse.getId();
        entityManagerFactoryName = "org.jbpm.vdml.jpa";
    }

    @Override
    public void setJobHandle(JobHandle jobHandle) {
        this.jobHandle = jobHandle;
    }

    @Override
    public JobHandle getJobHandle() {
        return jobHandle;
    }

    public Long getResourceUseObservationId() {
        return resourceUseObservationId;
    }

    public String getEntityManagerFactoryName() {
        return entityManagerFactoryName;
    }

    @Override
    public InternalWorkingMemory getWorkingMemory() {
        return null;
    }

    @Override
    public String getJobName() {
        return id;
    }

    @Override
    public Long getProcessInstanceId() {
        return null;
    }
}
