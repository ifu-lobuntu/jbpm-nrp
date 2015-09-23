package org.jbpm.vdml.services;

import org.omg.smm.Characteristic;
import org.omg.vdml.*;

/**
 * Created by ampie on 2015/09/17.
 */
public class AbstractExchangeTest extends MetaEntityImportTest {
    protected SupplyingStore createToAccount(StoreDefinition account, CapabilityMethod cp, Role capabilityProvider) {
        String storeName = "ToAccount";
        String inventoryLevelName = "amount";
        return createSupplyingStore(account, cp, capabilityProvider, storeName, inventoryLevelName);
    }

    protected SupplyingStore createSupplyingStore(StoreDefinition storeDef, CapabilityMethod cp, Role storeOwner, String storeName, String inventoryLevelName) {
        SupplyingStore toAccount = VDMLFactory.eINSTANCE.createSupplyingStore();
        cp.getSupplyingStore().add(toAccount);
        toAccount.setName(storeName);
        toAccount.setStoreRequirement(storeDef);
        toAccount.setSupplyingRole(storeOwner);
        toAccount.setInventoryLevel(VDMLFactory.eINSTANCE.createMeasuredCharacteristic());
        toAccount.getInventoryLevel().setName(inventoryLevelName);
        toAccount.getInventoryLevel().setCharacteristicDefinition(storeDef.getInventoryLevel());
        return toAccount;
    }
    protected SupplyingPool createSupplyingPool(PoolDefinition storeDef, CapabilityMethod cp, Role storeOwner, String storeName, String poolSize) {
        SupplyingPool toAccount = VDMLFactory.eINSTANCE.createSupplyingPool();
        cp.getSupplyingStore().add(toAccount);
        toAccount.setName(storeName);
        toAccount.setStoreRequirement(storeDef);
        toAccount.setSupplyingRole(storeOwner);
        toAccount.setInventoryLevel(VDMLFactory.eINSTANCE.createMeasuredCharacteristic());
        toAccount.getInventoryLevel().setName("InventoryLevel");
        toAccount.getInventoryLevel().setCharacteristicDefinition(storeDef.getInventoryLevel());
        toAccount.setPoolSize(VDMLFactory.eINSTANCE.createMeasuredCharacteristic());
        toAccount.getPoolSize().setName(poolSize);
        toAccount.getPoolSize().setCharacteristicDefinition(storeDef.getInventoryLevel());
        return toAccount;
    }

    protected SupplyingStore createFromAccount(StoreDefinition account,CapabilityMethod cp, Role consumer) {
        return createSupplyingStore(account, cp, consumer, "FromAccount", "amount");
    }

    protected Characteristic createAmount(ValueDeliveryModel vdm) {
        Characteristic amount = buildDirectMeasure(vdm,"Amount");
        return amount;
    }

    protected Characteristic buildDirectMeasure(ValueDeliveryModel vdm, String name) {
        Characteristic characteristic = buildDirectMeasure(vdm);
        characteristic.setName(name);
        characteristic.getMeasure().get(0).setName(name);
        return characteristic;
    }

    protected StoreDefinition createAccount(ValueDeliveryModel vdm, BusinessItemDefinition money) {
        String name = "Account";
        return createStore(vdm, money, name);
    }

    protected StoreDefinition createStore(ValueDeliveryModel vdm, BusinessItemDefinition resource, String name) {
        StoreLibrary sl = vdm.getStoreLibrary().get(0);
        StoreDefinition sd = VDMLFactory.eINSTANCE.createStoreDefinition();
        sl.getStoreDefinitions().add(sd);
        sd.setName(name);
        sd.setResource(resource);
        return sd;
    }
    protected PoolDefinition createPool(ValueDeliveryModel vdm, BusinessItemDefinition reusableResource, String name) {
        StoreLibrary sl = vdm.getStoreLibrary().get(0);
        PoolDefinition pd= VDMLFactory.eINSTANCE.createPoolDefinition();
        sl.getStoreDefinitions().add(pd);
        pd.setName(name);
        pd.setResource(reusableResource);
        return pd;
    }

    protected BusinessItemDefinition createMoney(ValueDeliveryModel vdm) {
        return createBusinessItemDefinition(vdm, "Money");
    }

    protected BusinessItemDefinition createBusinessItemDefinition(ValueDeliveryModel vdm, String s) {
        BusinessItemDefinition money = VDMLFactory.eINSTANCE.createBusinessItemDefinition();
        vdm.getBusinessItemLibrary().get(0).getBusinessItemLibraryElement().add(money);
        money.setName(s);
        return money;
    }

    protected Activity addActivity(CapabilityDefinition requestDef, CapabilityMethod cp, Role consumer, String s) {
        Activity requestProduct = VDMLFactory.eINSTANCE.createActivity();
        requestProduct.setName(s);
        requestProduct.setCapabilityRequirement(requestDef);
        cp.getActivity().add(requestProduct);
        requestProduct.setPerformingRole(consumer);
        return requestProduct;
    }

    protected Role createRole(CapabilityMethod cp, String name) {
        Role consumer= VDMLFactory.eINSTANCE.createPerformer();
        cp.getCollaborationRole().add(consumer);
        consumer.setName(name);
        return consumer;
    }

    protected CapabilityDefinition createCapabilityDefinition(ValueDeliveryModel vdm, String s) {
        CapabilityDefinition requestDef = VDMLFactory.eINSTANCE.createCapabilityDefinition();
        vdm.getCapabilitylibrary().get(0).getCapability().add(requestDef);
        requestDef.setName(s);
        return requestDef;
    }
}
