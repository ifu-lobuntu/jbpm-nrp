package org.jbpm.vdml.services.impl.model.runtime;


import com.vividsolutions.jts.geom.Point;
import org.jbpm.vdml.services.impl.model.scheduling.SchedulableObject;
import org.jbpm.vdml.services.impl.model.scheduling.Schedule;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;


@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Participant implements SchedulableObject {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    private Address address;
    @ManyToOne
    private Schedule schedule;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "participant")
    private Set<RolePerformance> rolePerformances =new HashSet<RolePerformance>();
    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL)
    private Set<CapabilityPerformance> capabilityOffers =new HashSet<CapabilityPerformance>();

    @OneToMany(mappedBy = "owner")
    private Set<StorePerformance> offeredStores =new HashSet<StorePerformance>();

    public Participant() {
    }

    public Long getId() {
        return id;
    }

    public Set<StorePerformance> getOfferedStores() {
        return offeredStores;
    }

    public Set<RolePerformance> getRolePerformances() {
        return rolePerformances;
    }

    public Set<CapabilityPerformance> getCapabilityOffers() {
        return capabilityOffers;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    @Override
    public Point getLocation() {
        return getAddress()==null?null:getAddress().getLocation();
    }

    @Override
    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }
}
