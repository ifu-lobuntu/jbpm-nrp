package org.jbpm.vdml.services.impl;

import org.jbpm.vdml.services.api.model.MeasurementCriterion;

import java.util.Collection;

public class CriteriaUtil {
    public static String buildCriteriaString(Collection<MeasurementCriterion> criteria) {
        StringBuilder sb = new StringBuilder();
        int i=0;
        for (MeasurementCriterion criterion : criteria) {
            switch(criterion.getOperator()){
                case BETWEEN:
                    sb.append("(m.measure.uri = '");
                    sb.append(criterion.getMeasureUri());
                    sb.append("' and m.actualValue > ");
                    sb.append(criterion.getLower());
                    sb.append(" and m.actualValue < ");
                    sb.append(criterion.getUpper());
                    sb.append(") ");
                    break;
                case GREATER_THAN:
                    sb.append("(m.measure.uri = '");
                    sb.append(criterion.getMeasureUri());
                    sb.append("' and m.actualValue > ");
                    sb.append(criterion.getLower());
                    sb.append(") ");
                    break;
                case LESS_THAN:
                    sb.append("(m.measure.uri = '");
                    sb.append(criterion.getMeasureUri());
                    sb.append("' and m.actualValue < ");
                    sb.append(criterion.getUpper());
                    sb.append(") ");
                    break;
            }
            i++;
            if(i<criteria.size()){
                sb.append(" or ");
            }
        }
        return sb.toString();
    }
}
