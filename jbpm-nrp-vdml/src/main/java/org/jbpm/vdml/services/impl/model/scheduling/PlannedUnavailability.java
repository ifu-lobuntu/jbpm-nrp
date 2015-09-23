package org.jbpm.vdml.services.impl.model.scheduling;

import com.vividsolutions.jts.geom.Point;
import org.hibernate.annotations.Type;
import org.jbpm.vdml.services.impl.model.runtime.Address;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.base.BaseDateTime;

import javax.persistence.*;

@Entity
public class PlannedUnavailability {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    private Schedule schedule;
    @ManyToOne
    private Address address;
    @Column(name="`from`")
    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private BaseDateTime from;
    @Column(name="`to`")
    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private BaseDateTime to;

    public PlannedUnavailability() {
    }

    public PlannedUnavailability(Schedule schedule) {
        this.schedule = schedule;
        this.schedule.getPlannedUnavailability().add(this);
    }

    public Long getId() {
        return id;
    }

    public Schedule getSchedule() {
        return schedule;
    }


    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public BaseDateTime getFrom() {
        return from;
    }

    public void setFrom(BaseDateTime from) {
        this.from = from;
    }

    public BaseDateTime getTo() {
        return to;
    }

    public void setTo(BaseDateTime to) {
        this.to = to;
    }

    public Point getLocation() {
        return address==null?null:address.getLocation();
    }

}
