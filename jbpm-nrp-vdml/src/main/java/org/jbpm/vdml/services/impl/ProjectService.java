package org.jbpm.vdml.services.impl;


import org.jbpm.vdml.services.api.model.LinkedExternalObject;
import org.jbpm.vdml.services.impl.model.meta.*;
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
        CapabilityMethod collaboration=entityManager.find(CapabilityMethod.class, collaborationUri);
        Participant participant = entityManager.find(Participant.class, requestorId);
        List<RolePerformance> roles=new ArrayList<RolePerformance>();
        roles.add(findOrCreateRole(participant, collaboration.getInitiatorRole().getFulfillingNetworkRole()));
        if(collaboration.getPlannerRole()!=null && !collaboration.getInitiatorRole().equals(collaboration.getPlannerRole())){
            roles.add(findOrCreateRole(participant, collaboration.getPlannerRole().getFulfillingNetworkRole()));
        }
        return collaborationService.startCollaboration(collaboration, roles);
    }
    public CollaborationInstance initiateProjectUnderCustodyOf(Long requestorId, Long custodian, String collaborationUri) {
        CapabilityMethod collaboration=entityManager.find(CapabilityMethod.class, collaborationUri);
        RolePerformance initiator = findOrCreateRole(entityManager.find(Participant.class, requestorId), collaboration.getInitiatorRole().getFulfillingNetworkRole());
        RolePerformance planner= findOrCreateRole(entityManager.find(Participant.class, custodian), collaboration.getPlannerRole().getFulfillingNetworkRole());
        return collaborationService.startCollaboration(collaboration, Arrays.asList(initiator, planner));
    }


    public RolePerformance selectCustodianForProject(Long custodianId, Long projectId) {
        CollaborationInstance project = entityManager.find(CollaborationInstance.class, projectId);
        RolePerformance rp = findOrCreateRole(entityManager.find(Participant.class, custodianId), project.getCollaboration().getPlannerRole().getFulfillingNetworkRole());
        assignmentService.assignToRoles(project, Arrays.asList(rp));
        return rp;
    }
    public RolePerformance assignParticipantToRole(Long participantId, Long projectId, String roleUri) {
        CollaborationInstance project = entityManager.find(CollaborationInstance.class, projectId);
        RolePerformance rp = findOrCreateRole(entityManager.find(Participant.class, participantId), entityManager.find(RoleInCapabilityMethod.class,roleUri).getFulfillingNetworkRole());
        assignmentService.assignToRoles(project, Arrays.asList(rp));
        return rp;
    }
    public RolePerformance assignParticipantToActivity(Long participantId, Long activityId) {
        ActivityInstance ai = entityManager.find(ActivityInstance.class, activityId);
        Participant p = entityManager.find(Participant.class,participantId);
        RolePerformance result = assignmentService.assignActivityToParticipant(p, ai);
        entityManager.flush();
        return result;
    }
    public StorePerformance assignParticipantToSupplyingstore( Long participantId,Long supplyingStoreId) {
        SupplyingStoreInstance ssi = entityManager.find(SupplyingStoreInstance.class, supplyingStoreId);
        assignmentService.assignStoreToParticipant(entityManager.find(Participant.class, participantId), ssi);
        entityManager.flush();
        return ssi.getStore();
    }
    public ReusableBusinessItemPerformance assignReusableBusinessItemPerformance(Long projectId, Long bipId) {
        CollaborationInstance project = entityManager.find(CollaborationInstance.class, projectId);
        ReusableBusinessItemPerformance storePerformance = entityManager.find(ReusableBusinessItemPerformance.class, bipId);
        assignmentService.assignToBusinessItem(project.findFirstBusinessItem(storePerformance.getDefinition()),storePerformance);
        entityManager.flush();
        return storePerformance;
    }
    public CollaborationInstance findProject(Long id) {
        return entityManager.find(CollaborationInstance.class,id);
    }

    public ActivityInstance newActivity(Long projectId, String activityUri, String inputName, LinkedExternalObject linkedExternalObject) {
        CollaborationInstance project = entityManager.find(CollaborationInstance.class, projectId);
        Activity activity=entityManager.find(Activity.class,activityUri);
        ActivityInstance newActivity = collaborationService.newActivity(project, activity, inputName, linkedExternalObject);
        entityManager.flush();
        return newActivity;
    }
}
