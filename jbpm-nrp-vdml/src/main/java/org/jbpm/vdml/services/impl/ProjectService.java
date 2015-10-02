package org.jbpm.vdml.services.impl;


import org.jbpm.vdml.services.impl.model.meta.Collaboration;
import org.jbpm.vdml.services.impl.model.meta.Role;
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
        CollaborationInstance collaborationInstance = entityManager.find(CollaborationInstance.class, collaborationId);
        MilestoneInstance mo = collaborationInstance.findMilestone(collaborationInstance.getCollaboration().findMilestone(milestoneName));
        collaborationService.commitToMilestone(mo);
    }

    public void fulfillMilestone(Long collaborationId, String milestoneName) {
        //TODO reevaluate use of directedFlows rather than deliverableFlows
        CollaborationInstance collaborationInstance = entityManager.find(CollaborationInstance.class, collaborationId);
        MilestoneInstance mo = collaborationInstance.findMilestone(collaborationInstance.getCollaboration().findMilestone(milestoneName));
        collaborationService.fulfillMilestone(mo);
    }


    public CollaborationInstance initiateProject(Long requestorId, String collaborationUri) {
        Collaboration collaboration=entityManager.find(Collaboration.class, collaborationUri);
        Participant participant = entityManager.find(Participant.class, requestorId);
        List<RolePerformance> roles=new ArrayList<RolePerformance>();
        roles.add(findOrCreateRole(participant, collaboration.getInitiatorRole()));
        if(collaboration.getPlannerRole()!=null && !collaboration.getInitiatorRole().equals(collaboration.getPlannerRole())){
            roles.add(findOrCreateRole(participant, collaboration.getPlannerRole()));
        }
        return collaborationService.startCollaboration(collaboration, roles);
    }
    public CollaborationInstance initiateProjectUnderCustodyOf(Long requestorId, Long custodian, String collaborationUri) {
        Collaboration collaboration=entityManager.find(Collaboration.class, collaborationUri);
        RolePerformance initiator = findOrCreateRole(entityManager.find(Participant.class, requestorId), collaboration.getInitiatorRole());
        RolePerformance planner= findOrCreateRole(entityManager.find(Participant.class, custodian), collaboration.getPlannerRole());
        return collaborationService.startCollaboration(collaboration, Arrays.asList(initiator, planner));
    }


    public RolePerformance selectCustodianForProject(Long custodianId, Long projectId) {
        CollaborationInstance project = entityManager.find(CollaborationInstance.class, projectId);
        RolePerformance rp = findOrCreateRole(entityManager.find(Participant.class, custodianId), project.getCollaboration().getPlannerRole());
        assignmentService.assignToRoles(project, Arrays.asList(rp));
        return rp;
    }
    public RolePerformance assignParticipantToRole(Long participantId, Long projectId, String roleUri) {
        CollaborationInstance project = entityManager.find(CollaborationInstance.class, projectId);
        RolePerformance rp = findOrCreateRole(entityManager.find(Participant.class, participantId), entityManager.find(Role.class,roleUri));
        assignmentService.assignToRoles(project, Arrays.asList(rp));
        return rp;
    }
    public StorePerformance assignStorePerformance(Long projectId, Long spId) {
        CollaborationInstance project = entityManager.find(CollaborationInstance.class, projectId);
        StorePerformance storePerformance = entityManager.find(StorePerformance.class, spId);
        assignmentService.assignToSupplyingStores(project.findSupplyingStore(storePerformance.getStoreDefinition()),storePerformance);
        entityManager.flush();
        return storePerformance;
    }
    public ReusableBusinessItemPerformance assignReusableBusinessItemPerformance(Long projectId, Long bipId) {
        CollaborationInstance project = entityManager.find(CollaborationInstance.class, projectId);
        ReusableBusinessItemPerformance storePerformance = entityManager.find(ReusableBusinessItemPerformance.class, bipId);
        assignmentService.assignToBusinessItem(project.findBusinessItem(storePerformance.getDefinition()),storePerformance);
        entityManager.flush();
        return storePerformance;
    }
    public CollaborationInstance findProject(Long id) {
        return entityManager.find(CollaborationInstance.class,id);
    }
}
