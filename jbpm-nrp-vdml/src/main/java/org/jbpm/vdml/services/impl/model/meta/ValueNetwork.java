package org.jbpm.vdml.services.impl.model.meta;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static org.jbpm.vdml.services.impl.model.meta.MetaEntityUtil.findByName;

@Entity
@DiscriminatorValue("ValueNetwork")
public class ValueNetwork extends Collaboration{

    public ValueNetwork() {
    }

    public ValueNetwork(String uri) {
        super(uri);
    }

}
