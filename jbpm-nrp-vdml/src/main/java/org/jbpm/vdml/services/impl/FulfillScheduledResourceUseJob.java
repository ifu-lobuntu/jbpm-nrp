package org.jbpm.vdml.services.impl;

import org.drools.core.time.Job;
import org.drools.core.time.JobContext;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * Created by ampie on 2015/09/23.
 */
public class FulfillScheduledResourceUseJob implements Job {
    @Override
    public void execute(JobContext jobContext) {
        ScheduledResourceUseJobContext c = (ScheduledResourceUseJobContext) jobContext;
        EntityManagerFactory emf = EntityManagerFactoryManager.get().getOrCreate(c.getEntityManagerFactoryName());
        EntityManager em = emf.createEntityManager();
        new ExchangeService(em).fulfillResourceUse(c.getResourceUseObservationId());
        em.close();
    }
}
