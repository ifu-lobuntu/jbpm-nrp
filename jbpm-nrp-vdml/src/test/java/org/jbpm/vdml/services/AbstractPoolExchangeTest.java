package org.jbpm.vdml.services;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.jbpm.vdml.services.api.model.LinkedExternalObject;
import org.jbpm.vdml.services.api.model.ReusableBusinessItemRequirement;
import org.jbpm.vdml.services.impl.ParticipantService;
import org.jbpm.vdml.services.impl.VdmlImporter;
import org.jbpm.vdml.services.impl.model.meta.*;
import org.jbpm.vdml.services.impl.model.runtime.Address;
import org.jbpm.vdml.services.impl.model.runtime.IndividualParticipant;
import org.jbpm.vdml.services.scheduling.SchedulingUtilTest;
import org.joda.time.DateTime;
import org.omg.smm.Characteristic;
import org.omg.vdml.*;
import org.omg.vdml.Activity;
import org.omg.vdml.BusinessItemDefinition;
import org.omg.vdml.CapabilityMethod;
import org.omg.vdml.InputPort;
import org.omg.vdml.Milestone;
import org.omg.vdml.PoolDefinition;
import org.omg.vdml.ResourceUse;
import org.omg.vdml.ResourceUseLocation;
import org.omg.vdml.Role;
import org.omg.vdml.StoreDefinition;
import org.omg.vdml.SupplyingStore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AbstractPoolExchangeTest extends MetaEntityImportTest {
    GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

    protected IndividualParticipant createSupplier1(ParticipantService participantService, List<String> storeDefIds, String tukTukDefinitionId) {
        IndividualParticipant supplier1 = participantService.createIndividualParticipant("BestMach");
        participantService.setAddress(supplier1.getId(), new Address(geometryFactory.createPoint(new Coordinate(0d, 0d))));
        participantService.setStores(supplier1.getId(), storeDefIds);
        Long poolId = participantService.findParticipant(supplier1.getId()).getOfferedStores().iterator().next().getId();
        Long bipId = participantService.addResourceToStore(poolId, new LinkedExternalObject(tukTukDefinitionId, "TukTuk", "BestMach"));
        participantService.setResourceSchedule(bipId, SchedulingUtilTest.buildSchedule());
        participantService.setPoolSchedule(poolId, SchedulingUtilTest.buildSchedule());
        return supplier1;
    }

    protected ReusableBusinessItemRequirement buildRequirement(String tukTukDefinitionId, int offset) {
        ReusableBusinessItemRequirement r = new ReusableBusinessItemRequirement();
        r.setBusinessItemDefinitionId(tukTukDefinitionId);
        r.setDuration(3d);
        r.setDurationTimeUnit(TimeUnit.HOURS);
        r.setMaxDistanceInMeter(15000d);
        r.setNotBefore(new DateTime(2015, 10, 1, 9 + offset, 0, 0, 0));
        r.setNotAfter(new DateTime(2015, 10, 1, 12 + offset, 0, 0, 0));
        r.setLattitude(0d);
        r.setLongitude(0d);
        return r;
    }


    protected org.jbpm.vdml.services.impl.model.meta.CapabilityMethod buildDefaultStoreExchange(ValueDeliveryModel vdm) throws IOException {
        BusinessItemDefinition money = createBusinessItemDefinition(vdm, "Money");


        StoreDefinition account = createStore(vdm, money, "Account");
        Characteristic amount = buildDirectMeasure(vdm, "Amount");
        account.setInventoryLevel(amount);

        BusinessItemDefinition productDefinition = createBusinessItemDefinition(vdm, "TukTukDefinition");

        PoolDefinition pool = createPool(vdm, productDefinition, "ResourcePool");
        pool.setInventoryLevel(buildDirectMeasure(vdm, "InventoryLevel"));
        pool.setPoolSize(buildDirectMeasure(vdm, "PoolSize"));

        BusinessItemDefinition orderDefinition = createBusinessItemDefinition(vdm, "OrderDefinition");

        CapabilityDefinition requestDef = createCapabilityDefinition(vdm, "Request");

        CapabilityDefinition provideDef = createCapabilityDefinition(vdm, "Provide");

        CapabilityDefinition receiveDef = createCapabilityDefinition(vdm, "Use");

        CapabilityMethod cp = VDMLFactory.eINSTANCE.createCapabilityMethod();
        vdm.getCollaboration().add(cp);
        cp.setName("ProvidePoolResourceExchange");
        BusinessItem productBusinessItem = addBusinessItem(productDefinition, cp);
        BusinessItem orderBusinessItem = addBusinessItem(orderDefinition, cp);
        BusinessItem moneyBusinessItem = addBusinessItem(money, cp);
        OrgUnit network=createValueNetwork(vdm, "Network");

        Role storeOwner = createRole(cp,network, "Provider");

        Role consumer = createRole(cp, network,"Consumer");

        SupplyingStore fromAccount = addSupplyingStore(cp, account, consumer, "FromAccount", "amount");

        SupplyingStore toAccount = addSupplyingStore(cp, account, storeOwner, "ToAccount", "amount");

        SupplyingStore productStore = addSupplyingPool(cp, pool, storeOwner, "ResourcePool", "inventoryLevel");

        Activity requestProduct = addActivity(requestDef, cp, consumer, "Request");

        Activity provideProduct = addActivity(provideDef, cp, storeOwner, "Provide");


        Activity useProduct = addActivity(receiveDef, cp, consumer, "Use");

        Milestone productComplete = VDMLFactory.eINSTANCE.createMilestone();
        productComplete.setName("ProductComplete");
        cp.getMilestone().add(productComplete);


        addDeliverableFlow(cp, orderBusinessItem, requestProduct, provideProduct, "providedWorkDefinition", "receivedWorkDefinition");
        addDeliverableFlow(cp, moneyBusinessItem, fromAccount, provideProduct, "paidMoney", "receivedMoney").setMilestone(productComplete);
        addDeliverableFlow(cp, moneyBusinessItem, provideProduct, toAccount, "receivedMoney", "savedMoney").setMilestone(productComplete);
        addDeliverableFlow(cp, productBusinessItem, productStore, useProduct, "productSold", "productUsed").setMilestone(productComplete);

        ResourceUse resourceUse = VDMLFactory.eINSTANCE.createResourceUse();
        useProduct.getResourceUse().add(resourceUse);
        resourceUse.setName("UseTukTuk");
        resourceUse.getResource().add((InputPort) findByName(useProduct.getContainedPort(), "productUsed"));
        resourceUse.setLocation(ResourceUseLocation.ROLE_PARTICIPANT);
        resourceUse.setResourceIsConsumed(false);

        pool.setExchangeConfiguration(VDMLFactory.eINSTANCE.createExchangeConfiguration());
        pool.getExchangeConfiguration().setExchangeMethod(cp);
        pool.getExchangeConfiguration().setExchangeMilestone(productComplete);
        pool.getExchangeConfiguration().setSupplierRole(storeOwner);
        pool.getExchangeConfiguration().setResourceUseFromPool(resourceUse);

        vdm.eResource().save(new ByteArrayOutputStream(), null);
        VdmlImporter vi = new VdmlImporter(getEntityManager());
        vi.buildModel(DEFAULT_DEPLOYMENT_ID, vdm);
        org.jbpm.vdml.services.impl.model.meta.CapabilityMethod collaboration = (org.jbpm.vdml.services.impl.model.meta.CapabilityMethod) vi.buildCollaboration(DEFAULT_DEPLOYMENT_ID, cp);
        return collaboration;
    }
}
