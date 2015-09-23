package org.jbpm.vdml.services.impl.model.runtime;

public interface DirectlyExchangable {
    ExchangeConfiguration getExchangeConfiguration();
    Participant getSupplier();
}
