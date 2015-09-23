package org.jbpm.vdml.services.impl.model.meta;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.*;

@Entity
public class BusinessItemDefinition implements MetaEntity{
    @Id
    private String uri;
    private String name;
    @ManyToOne
    private BusinessItemDefinition extendedBusinessItemDefinition;
    @ManyToMany(mappedBy = "businessItemDefinitions")
    private Set<Collaboration> collaborations=new HashSet<Collaboration>();
    @ManyToMany
    private Set<Measure> measures=new HashSet<Measure>();
    private boolean fungible;
    private Boolean shareable;

    public BusinessItemDefinition() {
    }

    public BusinessItemDefinition(String uri, Collaboration collaboration) {
        this.uri = uri;
        this.collaborations.add(collaboration);
        collaboration.getBusinessItemDefinitions().add(this);
    }
    public BusinessItemDefinition(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Measure> getMeasures() {
        return measures;
    }

    public BusinessItemDefinition getExtendedBusinessItemDefinition() {
        return extendedBusinessItemDefinition;
    }

    public void setExtendedBusinessItemDefinition(BusinessItemDefinition extendedBusinessItemDefinition) {
        this.extendedBusinessItemDefinition = extendedBusinessItemDefinition;
    }
    public Set<Measure> getAggregatingMeasures(){
        Set<Measure> result =new HashSet<Measure>();
        separateMeasures(getMeasures(),result, new HashSet<Measure>());
        return result;
    }
    public Set<Measure> getImmediateMeasures(){
        Set<Measure> result =new HashSet<Measure>();
        separateMeasures(getMeasures(),new HashSet<Measure>(),result);
        return result;
    }
    private static void separateMeasures(Collection<Measure> source, Collection<Measure> multipleInstanceMeasures, Collection<Measure> singleInstanceMeasures) {
        Collection<Measure> temp = new HashSet<Measure>(source);
        Map<String, Measure> singleInstanceMap = new HashMap<String, Measure>();
        Map<String, Measure> multipleInstanceMap = new HashMap<String, Measure>();
        Iterator<Measure> iterator = temp.iterator();
        while (iterator.hasNext()) {
            Measure measure = iterator.next();
            if (measure instanceof DirectMeasure || measure instanceof EnumeratedMeasure) {
                iterator.remove();
                singleInstanceMap.put(measure.getUri(), measure);
            } else if (measure instanceof CollectiveMeasure || measure instanceof CountingMeasure) {
                iterator.remove();
                multipleInstanceMap.put(measure.getUri(), measure);
            }
        }
        while (temp.size() > 0) {
            Iterator<Measure> iterator2 = temp.iterator();
            while (iterator2.hasNext()) {
                Measure measure = iterator2.next();
                if (measure instanceof BinaryMeasure) {
                    BinaryMeasure bm = (BinaryMeasure) measure;
                    if (singleInstanceMap.containsKey(bm.getMeasureA().getUri()) && singleInstanceMap.containsKey(bm.getMeasureB())) {
                        singleInstanceMap.put(bm.getUri(), bm);
                        iterator2.remove();
                    } else if (multipleInstanceMap.containsKey(bm.getMeasureA().getUri()) && multipleInstanceMap.containsKey(bm.getMeasureB())) {
                        multipleInstanceMap.put(bm.getUri(), bm);
                        iterator2.remove();
                    } else {
                        //TODO validate against the 2 invalid combinations
                    }

                } else if (measure instanceof RescaledMeasure) {
                    RescaledMeasure rm = (RescaledMeasure) measure;
                    if (singleInstanceMap.containsKey(rm.getRescaledMeasure().getUri())) {
                        singleInstanceMap.put(rm.getUri(), rm);
                        iterator2.remove();
                    } else if (multipleInstanceMap.containsKey(rm.getRescaledMeasure().getUri())) {
                        multipleInstanceMap.put(rm.getUri(), rm);
                        iterator2.remove();
                    }
                }
            }
        }
        multipleInstanceMeasures.addAll(multipleInstanceMap.values());
        singleInstanceMeasures.addAll(singleInstanceMap.values());
    }

    public boolean isFungible() {
        return fungible;
    }

    public void setFungible(boolean fungible) {
        this.fungible = fungible;
    }

    public void setShareable(Boolean shareable) {
        this.shareable = shareable;
    }

    public Boolean getShareable() {
        return shareable;
    }

    public Set<Collaboration> getCollaborations() {
        return collaborations;
    }
}
