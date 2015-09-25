package org.jbpm.vdml.services.impl.model.runtime;

public interface DirectlyExchangable extends Measurand {
    ExchangeConfiguration getExchangeConfiguration();
    Participant getSupplier();
}
