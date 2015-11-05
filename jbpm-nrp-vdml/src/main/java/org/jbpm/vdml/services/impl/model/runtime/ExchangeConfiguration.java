package org.jbpm.vdml.services.impl.model.runtime;

import org.jbpm.vdml.services.impl.model.meta.*;

import javax.persistence.*;
import java.util.concurrent.TimeUnit;

@Entity
public class ExchangeConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    private CapabilityMethod collaborationToUse;
    @ManyToOne
    private RoleInCapabilityMethod supplierRole;
    @ManyToOne
    private Milestone exchangeMilestone;
    @ManyToOne
    private ResourceUse poolBooking;


    public Long getId() {
        return id;
    }

    public CapabilityMethod getCollaborationToUse() {
        return collaborationToUse;
    }

    public void setCollaborationToUse(CapabilityMethod collaborationToUse) {
        this.collaborationToUse = collaborationToUse;
    }

    public Milestone getExchangeMilestone() {
        return exchangeMilestone;
    }

    public void setExchangeMilestone(Milestone exchangeMilestone) {
        this.exchangeMilestone = exchangeMilestone;
    }

    public ResourceUse getPoolBooking() {
        return poolBooking;
    }

    public void setPoolBooking(ResourceUse poolBooking) {
        this.poolBooking = poolBooking;
    }

    public RoleInCapabilityMethod getSupplierRole() {
        return supplierRole;
    }

    public void setSupplierRole(RoleInCapabilityMethod supplierRole) {
        this.supplierRole = supplierRole;
    }


}
