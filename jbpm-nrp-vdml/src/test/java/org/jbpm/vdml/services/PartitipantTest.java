package org.jbpm.vdml.services;

import org.jbpm.vdml.services.impl.MetaBuilder;
import org.jbpm.vdml.services.impl.ParticipantService;
import org.jbpm.vdml.services.impl.VdmlImporter;
import org.jbpm.vdml.services.impl.model.runtime.CapabilityPerformance;
import org.jbpm.vdml.services.impl.model.runtime.IndividualParticipant;
import org.jbpm.vdml.services.impl.model.runtime.RolePerformance;
import org.jbpm.vdml.services.impl.model.runtime.StorePerformance;
import org.junit.Test;
import org.omg.vdml.*;

import javax.persistence.EntityManager;
import java.io.ByteArrayOutputStream;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class PartitipantTest extends MetaEntityImportTest {
    @Test
    public void testCapability() throws Exception{
        //Given
        ValueDeliveryModel vdm = buildModel();
        CapabilityDefinition capability=VDMLFactory.eINSTANCE.createCapabilityDefinition();
        capability.setName("MyCapability");
        super.addCharacteristics(vdm,capability.getCharacteristicDefinition());
        vdm.getCapabilitylibrary().get(0).getCapability().add(capability);

        vdm.eResource().save(new ByteArrayOutputStream(), null);
        new VdmlImporter(getEntityManager()).buildModel(DEFAULT_DEPLOYMENT_ID, vdm);
        ParticipantService participantService = new ParticipantService(getEntityManager());
        IndividualParticipant ekke = participantService.createIndividualParticipant("ekke");
        //When
        participantService.setCapabilities(ekke.getId(), Collections.singleton(MetaBuilder.buildUri(capability)));
        //And it is idempotent
        participantService.setCapabilities(ekke.getId(), Collections.singleton(MetaBuilder.buildUri(capability)));
        //Then
        CapabilityPerformance capabilityPerformance=new ParticipantService(getEntityManager()).findIndividualParticipant("ekke").getCapabilityOffers().iterator().next();
        super.assertMeasurements(capabilityPerformance.getMeasurements());
        assertEquals(ekke.getId(), capabilityPerformance.getParticipant().getId());

    }


    @Test
    public void testStore() throws Exception{
        //Given
        ValueDeliveryModel vdm = buildModel();
        StoreDefinition storeDef=VDMLFactory.eINSTANCE.createStoreDefinition();
        storeDef.setName("MyStore");
        super.addCharacteristics(vdm, storeDef.getCharacteristicDefinition());
        vdm.getStoreLibrary().get(0).getStoreDefinitions().add(storeDef);

        vdm.eResource().save(new ByteArrayOutputStream(), null);
        new VdmlImporter(getEntityManager()).buildModel(DEFAULT_DEPLOYMENT_ID, vdm);
        ParticipantService participantService = new ParticipantService(getEntityManager());
        IndividualParticipant ekke = participantService.createIndividualParticipant("ekke");
        //When
        participantService.setStores(ekke.getId(), Collections.singleton(MetaBuilder.buildUri(storeDef)));
        //And it is idempotent
        participantService.setStores(ekke.getId(), Collections.singleton(MetaBuilder.buildUri(storeDef)));
        //Then
        StorePerformance storePerformance=new ParticipantService(getEntityManager()).findIndividualParticipant("ekke").getOfferedStores().iterator().next();
        super.assertMeasurements(storePerformance.getMeasurements());
        assertEquals(ekke.getId(), storePerformance.getOwner().getId());

    }
    @Test
    public void testRole() throws Exception{
        //Given
        ValueDeliveryModel vdm = buildModel();
        CapabilityMethod cm = VDMLFactory.eINSTANCE.createCapabilityMethod();
        vdm.getCollaboration().add(cm);
        cm.setName("MyCapabilityMethod");
        Performer performer=VDMLFactory.eINSTANCE.createPerformer();
        cm.getPerformer().add(performer);
        performer.setName("MyRole");
        Performer otherPerformer=VDMLFactory.eINSTANCE.createPerformer();
        cm.getPerformer().add(otherPerformer);
        otherPerformer.setName("YourRole");
        ValueProposition vp=VDMLFactory.eINSTANCE.createValueProposition();
        performer.getProvidedProposition().add(vp);
        vp.setName("MyValueToYou");
        vp.setRecipient(otherPerformer);
        vdm.eResource().save(new ByteArrayOutputStream(), null);
        ValuePropositionComponent vpc=VDMLFactory.eINSTANCE.createValuePropositionComponent();
        vp.getComponent().add(vpc);
        vpc.setName("MyVPCToYou");
        super.addMeasuredCharacteristics(vdm, vpc.getMeasuredCharacteristic());
        new VdmlImporter(getEntityManager()).buildModel(DEFAULT_DEPLOYMENT_ID, vdm);
        ParticipantService participantService = new ParticipantService(getEntityManager());
        IndividualParticipant ekke = participantService.createIndividualParticipant("ekke");
        //When
        participantService.setRoles(ekke.getId(), Collections.singleton(MetaBuilder.buildUri(performer)));
        //And it is idempotent
        participantService.setRoles(ekke.getId(), Collections.singleton(MetaBuilder.buildUri(performer)));
        //Then
        RolePerformance rolePerformance=new ParticipantService(getEntityManager()).findIndividualParticipant("ekke").getRolePerformances().iterator().next();
        assertEquals(ekke.getId(), rolePerformance.getParticipant().getId());
        assertEquals("MyRole", rolePerformance.getRole().getName());
        assertEquals(1, rolePerformance.getOverallProvidedValuePropositions().size());
        assertEquals("MyValueToYou", rolePerformance.getOverallProvidedValuePropositions().iterator().next().getValueProposition().getName());
        super.assertMeasurements(rolePerformance.getOverallProvidedValuePropositions().iterator().next().getComponents().iterator().next().getMeasurements());
    }
}
