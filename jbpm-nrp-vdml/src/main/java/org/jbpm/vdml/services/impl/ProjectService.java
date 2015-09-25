package org.jbpm.vdml.services.impl;


import org.jbpm.vdml.services.impl.model.meta.Collaboration;
import org.jbpm.vdml.services.impl.model.runtime.*;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProjectService extends AbstractRuntimeService{
    private CollaborationService collaborationService;
    private AssignmentService assignmentService;

    public ProjectService() {
    }

    public ProjectService(EntityManager em) {
        super(em);
        this.collaborationService=new CollaborationService(em);
        this.assignmentService=new AssignmentService(em);
    }
    public void commitToMilestone(Long collaborationId, String milestoneName) {
        //TODO reevaluate use of directedFlows rather than deliverableFlows
        CollaborationObservation collaborationObservation = entityManager.find(CollaborationObservation.class, collaborationId);
        MilestoneObservation mo = collaborationObservation.findMilestone(collaborationObservation.getCollaboration().findMilestone(milestoneName));
        collaborationService.commitToMilestone(mo);
    }

    public void fulfillMilestone(Long collaborationId, String milestoneName) {
        //TODO reevaluate use of directedFlows rather than deliverableFlows
        CollaborationObservation collaborationObservation = entityManager.find(CollaborationObservation.class, collaborationId);
        MilestoneObservation mo = collaborationObservation.findMilestone(collaborationObservation.getCollaboration().findMilestone(milestoneName));
        collaborationService.fulfillMilestone(mo);
    }


    public CollaborationObservation initiateProject(Long requestorId, String collaborationUri) {
        Collaboration collaboration=entityManager.find(Collaboration.class, collaborationUri);
        Participant participant = entityManager.find(Participant.class, requestorId);
        List<RolePerformance> roles=new ArrayList<RolePerformance>();
        roles.add(findOrCreateRole(participant, collaboration.getInitiatorRole()));
        if(collaboration.getPlannerRole()!=null && !collaboration.getInitiatorRole().equals(collaboration.getPlannerRole())){
            roles.add(findOrCreateRole(participant, collaboration.getPlannerRole()));
        }
        return collaborationService.startCollaboration(collaboration, roles);
    }
    public CollaborationObservation initiateProjectUnderCustodyOf(Long requestorId, Long custodian, String collaborationUri) {
        Collaboration collaboration=entityManager.find(Collaboration.class, collaborationUri);
        RolePerformance initiator = findOrCreateRole(entityManager.find(Participant.class, requestorId), collaboration.getInitiatorRole());
        RolePerformance planner= findOrCreateRole(entityManager.find(Participant.class, custodian), collaboration.getPlannerRole());
        return collaborationService.startCollaboration(collaboration, Arrays.asList(initiator, planner));
    }


    public RolePerformance selectCustodianForProject(Long custodianId, Long projectId) {
        CollaborationObservation project = entityManager.find(CollaborationObservation.class, projectId);
        RolePerformance rp = findOrCreateRole(entityManager.find(Participant.class, custodianId), project.getCollaboration().getPlannerRole());
        assignmentService.assignToRoles(project, Arrays.asList(rp));
        return rp;
    }
    public StorePerformance assignStorePerformance(Long projectId, Long spId) {
        CollaborationObservation project = entityManager.find(CollaborationObservation.class, projectId);
        StorePerformance storePerformance = entityManager.find(StorePerformance.class, spId);
        assignmentService.assignToSupplyingStores(project.findSupplyingStore(storePerformance.getStoreDefinition()),storePerformance);
        entityManager.flush();
        return storePerformance;
    }
    public ReusableBusinessItemPerformance assignReusableBusinessItemPerformance(Long projectId, Long bipId) {
        CollaborationObservation project = entityManager.find(CollaborationObservation.class, projectId);
        ReusableBusinessItemPerformance storePerformance = entityManager.find(ReusableBusinessItemPerformance.class, bipId);
        assignmentService.assignToBusinessItem(project.findBusinessItem(storePerformance.getDefinition()),storePerformance);
        entityManager.flush();
        return storePerformance;
    }
    public CollaborationObservation findProject(Long id) {
        return entityManager.find(CollaborationObservation.class,id);
    }
}
