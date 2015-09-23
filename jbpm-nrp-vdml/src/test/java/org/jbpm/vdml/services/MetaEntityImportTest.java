package org.jbpm.vdml.services;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.jbpm.vdml.services.impl.model.meta.Measure;
import org.jbpm.vdml.services.impl.model.meta.MetaEntity;
import org.jbpm.vdml.services.impl.model.runtime.Measurement;
import org.omg.smm.*;
import org.omg.vdml.*;
import org.omg.vdml.util.VDMLResourceFactoryImpl;
import org.omg.vdml.util.VDMLResourceImpl;
import test.TestGradeMeasure;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


public abstract class MetaEntityImportTest extends AbstractVdmlServiceTest {
    protected void assertMeasurements(Set<? extends Measurement> foundSdMeasures) {
        Set<Measure> measures = new HashSet<Measure>();
        for (Measurement foundSdMeasure : foundSdMeasures) {
            measures.add(foundSdMeasure.getMeasure());
        }
        assertMeasures(measures);
    }
    protected <T extends VdmlElement> T findByName(Collection<? extends T> from, String name){
        for (T t : from) {
            if(t.getName().equals(name))
            {
                return t;
            }
        }
        return null;
    }
    protected void assertMeasures(Set<Measure> foundSdMeasures) {
        assertEquals(6, foundSdMeasures.size());
        for (Measure measure : foundSdMeasures) {
            if (measure instanceof org.jbpm.vdml.services.impl.model.meta.DirectMeasure) {
                assertEquals("DirectMeasure", measure.getName());
            } else if (measure instanceof org.jbpm.vdml.services.impl.model.meta.CountingMeasure) {
                assertEquals("CountingMeasure", measure.getName());
                org.jbpm.vdml.services.impl.model.meta.CountingMeasure found = (org.jbpm.vdml.services.impl.model.meta.CountingMeasure) measure;
                assertEquals("value > 1000", found.getValuesToCount());
                assertNotNull(found.getMeasureToCount());
            } else if (measure instanceof org.jbpm.vdml.services.impl.model.meta.BinaryMeasure) {
                assertEquals("BinaryMeasure", measure.getName());
                org.jbpm.vdml.services.impl.model.meta.BinaryMeasure found = (org.jbpm.vdml.services.impl.model.meta.BinaryMeasure) measure;
                assertNotNull(found.getMeasureA());
                assertNotNull(found.getMeasureB());
                assertEquals(org.jbpm.vdml.services.impl.model.meta.BinaryFunctor.DIVIDE, found.getFunctor());
            } else if (measure instanceof org.jbpm.vdml.services.impl.model.meta.CollectiveMeasure) {
                assertEquals("CollectiveMeasure", measure.getName());
                org.jbpm.vdml.services.impl.model.meta.CollectiveMeasure found = (org.jbpm.vdml.services.impl.model.meta.CollectiveMeasure) measure;
                assertNotNull(found.getAggregatedMeasures());
                assertEquals(org.jbpm.vdml.services.impl.model.meta.Accumulator.PRODUCT, found.getAccumulator());
            } else if (measure instanceof org.jbpm.vdml.services.impl.model.meta.EnumeratedMeasure) {
                assertEquals("TestGradeMeasure", measure.getName());
                org.jbpm.vdml.services.impl.model.meta.EnumeratedMeasure found = (org.jbpm.vdml.services.impl.model.meta.EnumeratedMeasure) measure;
                assertEquals(TestGradeMeasure.class, found.getEnumClass());
            } else if (measure instanceof org.jbpm.vdml.services.impl.model.meta.RescaledMeasure) {
                assertEquals("RescaledMeasure", measure.getName());
                org.jbpm.vdml.services.impl.model.meta.RescaledMeasure found = (org.jbpm.vdml.services.impl.model.meta.RescaledMeasure) measure;
                assertNotNull(found.getRescaledMeasure());
                assertEquals(2d, found.getMultiplier().doubleValue(), 0.0001);
                assertEquals(100d, found.getOffset().doubleValue(),0.0001);
            } else {
                fail("Unexpected Measure Type: " + measure.getClass().getSimpleName());
            }
        }

    }

    protected void addMeasuredCharacteristics(ValueDeliveryModel l, List<MeasuredCharacteristic> measuredCharacteristics) {
        ArrayList<Characteristic> characteristics = new ArrayList<Characteristic>();
        addCharacteristics(l, characteristics);
        for (Characteristic characteristic : characteristics) {
            MeasuredCharacteristic mc = VDMLFactory.eINSTANCE.createMeasuredCharacteristic();
            mc.setName(characteristic.getName());
            mc.setCharacteristicDefinition(characteristic);
            measuredCharacteristics.add(mc);
        }
    }

    protected void addCharacteristics(ValueDeliveryModel l, List<Characteristic> characteristicDefinition) {
        Characteristic e = buildDirectMeasure(l);
        characteristicDefinition.add(e);

        CountingMeasure countingMeasure = SMMFactory.eINSTANCE.createCountingMeasure();
        countingMeasure.setName("CountingMeasure");
        countingMeasure.setOperation(SMMFactory.eINSTANCE.createOperation());
        countingMeasure.getOperation().setBody("value > 1000");
        CountingMeasureRelationship countingMeasureRelationship = SMMFactory.eINSTANCE.createCountingMeasureRelationship();
        countingMeasure.setCountedMeasureTo(countingMeasureRelationship);
        countingMeasure.getCountedMeasureTo().setToCountedMeasure(countingMeasure);
        l.getMetricsModel().get(0).getLibraries().get(0).getMeasureElements().add(countingMeasure.getOperation());
        characteristicDefinition.add(addToLibrary(l, countingMeasure));


        BinaryMeasure binaryMeasure = SMMFactory.eINSTANCE.createBinaryMeasure();
        binaryMeasure.setName("BinaryMeasure");
        Base1MeasureRelationship base1 = SMMFactory.eINSTANCE.createBase1MeasureRelationship();
        Base2MeasureRelationship base2 = SMMFactory.eINSTANCE.createBase2MeasureRelationship();
        binaryMeasure.setBaseMeasure1To(base1);
        binaryMeasure.setBaseMeasure2To(base2);
        binaryMeasure.setFunctor(BinaryFunctor.DIVIDE);
        base1.setToDimensionalMeasure((DirectMeasure) e.getMeasure().get(0));
        base2.setToDimensionalMeasure(countingMeasure);
        characteristicDefinition.add(addToLibrary(l, binaryMeasure));

        Characteristic e1 = buildCollectiveMeasure(l, e);
        characteristicDefinition.add(e1);

        GradeMeasure gradeMeasure = SMMFactory.eINSTANCE.createGradeMeasure();
        gradeMeasure.setName("TestGradeMeasure");
        characteristicDefinition.add(addToLibrary(l, gradeMeasure));

        RescaledMeasure rescaledMeasure = SMMFactory.eINSTANCE.createRescaledMeasure();
        RescaledMeasureRelationship rescaledMeasureRelationship = SMMFactory.eINSTANCE.createRescaledMeasureRelationship();
        rescaledMeasureRelationship.setFromDimensionalMeasure((DirectMeasure) e.getMeasure().get(0));
        rescaledMeasure.getRescalesFrom().add(rescaledMeasureRelationship);
        rescaledMeasure.setName("RescaledMeasure");
        rescaledMeasure.setMultiplier(2d);
        rescaledMeasure.setOffset(100d);
        characteristicDefinition.add(addToLibrary(l, rescaledMeasure));
    }

    protected Characteristic buildCollectiveMeasure(ValueDeliveryModel l, Characteristic e) {
        CollectiveMeasure collectiveMeasure = SMMFactory.eINSTANCE.createCollectiveMeasure();
        collectiveMeasure.setName("CollectiveMeasure");
        BaseNMeasureRelationship baseN = SMMFactory.eINSTANCE.createBaseNMeasureRelationship();
        collectiveMeasure.getBaseMeasureTo().add(baseN);
        baseN.setToDimensionalMeasure((DirectMeasure) e.getMeasure().get(0));
        collectiveMeasure.setAccumulator(Accumulator.PRODUCT);
        return addToLibrary(l, collectiveMeasure);
    }

    Characteristic buildDirectMeasure(ValueDeliveryModel l) {
        DirectMeasure directMeasure1 = SMMFactory.eINSTANCE.createDirectMeasure();
        directMeasure1.setName("DirectMeasure");
        return addToLibrary(l, directMeasure1);
    }

    private Characteristic addToLibrary(ValueDeliveryModel l, org.omg.smm.Measure m) {
        Characteristic characteristic = SMMFactory.eINSTANCE.createCharacteristic();
        characteristic.setName(m.getName());
        characteristic.getMeasure().add(m);
        l.getMetricsModel().get(0).getLibraries().get(0).getMeasureElements().add(characteristic);
        l.getMetricsModel().get(0).getLibraries().get(0).getMeasureElements().add(m);
        ((VDMLResourceImpl)m.eResource()).setID(m, EcoreUtil.generateUUID());
        return characteristic;
    }

    protected ValueDeliveryModel buildModel() {
        ResourceSet rst = new ResourceSetImpl();
        rst.getResourceFactoryRegistry().getExtensionToFactoryMap().put("vdml", new VDMLResourceFactoryImpl());
        VDMLResourceImpl resource = (VDMLResourceImpl) rst.createResource(URI.createPlatformResourceURI("test/test/test.vdml", true));
        ValueDeliveryModel vdm = VDMLFactory.eINSTANCE.createValueDeliveryModel();
        resource.getContents().add(vdm);
        MeasureLibrary ml = SMMFactory.eINSTANCE.createMeasureLibrary();
        SmmModel smm = SMMFactory.eINSTANCE.createSmmModel();
        vdm.getMetricsModel().add(smm);
        smm.getLibraries().add(ml);
        vdm.getBusinessItemLibrary().add(VDMLFactory.eINSTANCE.createBusinessItemLibrary());
        vdm.getCapabilitylibrary().add(VDMLFactory.eINSTANCE.createCapabilityLibrary());
        vdm.getStoreLibrary().add(VDMLFactory.eINSTANCE.createStoreLibrary());
        vdm.getScenario().add(VDMLFactory.eINSTANCE.createScenario());
        vdm.getScenario().get(0).setIsCommon(true);
        return vdm;
    }

    protected DeliverableFlow addDeliverableFlow(Collaboration cp, BusinessItem businessItem, PortContainer from, PortContainer to, String fromPortName, String toPortName) {
        DeliverableFlow flow = VDMLFactory.eINSTANCE.createDeliverableFlow();
        cp.getFlow().add(flow);
        flow.setDeliverable(businessItem);
        flow.setName("From" + to.getName() + "To" + from.getName());
        flow.setProvider(VDMLFactory.eINSTANCE.createOutputPort());
        flow.getProvider().setName(fromPortName);
        from.getContainedPort().add(flow.getProvider());
        flow.setRecipient(VDMLFactory.eINSTANCE.createInputPort());
        flow.getRecipient().setName(toPortName);
        to.getContainedPort().add(flow.getRecipient());
        Characteristic characteristic=null;
        if(to instanceof SupplyingStore){
            characteristic=((SupplyingStore)to).getInventoryLevel().getCharacteristicDefinition();
        }else if(from instanceof  SupplyingStore){
            characteristic=((SupplyingStore)from).getInventoryLevel().getCharacteristicDefinition();
        }
        if(characteristic!=null){
             flow.getProvider().setBatchSize(VDMLFactory.eINSTANCE.createMeasuredCharacteristic());
            flow.getProvider().getBatchSize().setCharacteristicDefinition(characteristic);
            flow.getRecipient().setBatchSize(VDMLFactory.eINSTANCE.createMeasuredCharacteristic());
            flow.getRecipient().getBatchSize().setCharacteristicDefinition(characteristic);
        }
        return flow;
    }

    protected BusinessItem addBusinessItem(BusinessItemDefinition workDefinition, CapabilityMethod cp) {
        BusinessItem workBusinessItem = VDMLFactory.eINSTANCE.createBusinessItem();
        cp.getBusinessItem().add(workBusinessItem);
        workBusinessItem.setDefinition(workDefinition);
        workBusinessItem.setName(workDefinition.getName());
        return workBusinessItem;
    }}
