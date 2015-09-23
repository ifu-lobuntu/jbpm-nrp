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
        return addSupplyingStore(cp, account, capabilityProvider, storeName, inventoryLevelName);
    }



    protected SupplyingStore createFromAccount(StoreDefinition account,CapabilityMethod cp, Role consumer) {
        return addSupplyingStore(cp, account, consumer, "FromAccount", "amount");
    }

    protected Characteristic createAmount(ValueDeliveryModel vdm) {
        Characteristic amount = buildDirectMeasure(vdm, "Amount");
        return amount;
    }

    protected StoreDefinition createAccount(ValueDeliveryModel vdm, BusinessItemDefinition money) {
        String name = "Account";
        return createStore(vdm, money, name);
    }


    protected BusinessItemDefinition createMoney(ValueDeliveryModel vdm) {
        return createBusinessItemDefinition(vdm, "Money");
    }

}
