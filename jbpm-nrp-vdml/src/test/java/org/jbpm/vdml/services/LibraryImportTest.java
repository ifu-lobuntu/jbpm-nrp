package org.jbpm.vdml.services;


import org.jbpm.vdml.services.impl.MetaBuilder;
import org.jbpm.vdml.services.impl.VdmlImporter;
import org.junit.Test;
import org.omg.smm.*;
import org.omg.vdml.*;
import org.omg.vdml.BusinessItemDefinition;
import org.omg.vdml.StoreDefinition;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.*;

public class LibraryImportTest extends MetaEntityImportTest {

    @Test
    public void testIt() throws Exception {
        ValueDeliveryModel vdm = buildModel();
        MeasureLibrary ml=vdm.getMetricsModel().get(0).getLibraries().get(0);
        StoreLibrary sl = vdm.getStoreLibrary().get(0);
        StoreDefinition sd = VDMLFactory.eINSTANCE.createStoreDefinition();
        sl.getStoreDefinitions().add(sd);
        sd.setName("SupplyStuffDef");
        addCharacteristics(vdm, sd.getCharacteristicDefinition());

        BusinessItemDefinition bd = VDMLFactory.eINSTANCE.createBusinessItemDefinition();
        vdm.getBusinessItemLibrary().get(0).getBusinessItemLibraryElement().add(bd);
        bd.setName("StuffDef");
        addCharacteristics(vdm, bd.getCharacteristicDefinition());

        CapabilityLibrary cl =vdm.getCapabilitylibrary().get(0);
        vdm.getCapabilitylibrary().add(cl);
        CapabilityDefinition cd = VDMLFactory.eINSTANCE.createCapabilityDefinition();
        cl.getCapability().add(cd);
        cd.setName("DoStuffDef");
        addCharacteristics(vdm, cd.getCharacteristicDefinition());


        vdm.eResource().save(new ByteArrayOutputStream(), null);
        //WHEN
        VdmlImporter importer = new VdmlImporter(getEntityManager());
        importer.buildModel(DEFAULT_DEPLOYMENT_ID, vdm);
        //THEN
        VdmlImporter importer2 = new VdmlImporter(getEntityManager());
        org.jbpm.vdml.services.impl.model.meta.StoreDefinition foundSd = importer2.findStoreDefinition(MetaBuilder.buildUri(sd));
        assertEquals("SupplyStuffDef", foundSd.getName());
        assertMeasures(foundSd.getMeasures());
        org.jbpm.vdml.services.impl.model.meta.BusinessItemDefinition foundBd = importer2.findBusinessItemDefinition(MetaBuilder.buildUri(bd));
        assertEquals("StuffDef", foundBd.getName());
        assertMeasures(foundBd.getMeasures());
        org.jbpm.vdml.services.impl.model.meta.Capability foundCd = importer2.findCapabilityDefinition(MetaBuilder.buildUri(cd));
        assertEquals("DoStuffDef", foundCd.getName());
        assertMeasures(foundCd.getMeasures());

    }

}
