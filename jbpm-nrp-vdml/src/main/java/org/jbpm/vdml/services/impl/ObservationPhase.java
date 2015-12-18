package org.jbpm.vdml.services.impl;

import org.jbpm.nrp.common.RankingIntervalEnum;
import org.jbpm.vdml.services.impl.model.runtime.Measurement;

public enum ObservationPhase {
    PLANNING {
        @Override
        public boolean isResolved(Measurement measurement) {
            return measurement.isPlannedValueResolved();
        }

        @Override
        public void setValue(Measurement m, Object o) {
            if (o instanceof Double) {
                m.setPlannedValue((Double) o);
            } else if (o instanceof Enum) {
                m.setPlannedRating((Enum) o);
                if(o instanceof RankingIntervalEnum){
                    m.setPlannedValue(((RankingIntervalEnum) o).getValue());
                }
            }
        }
    }, EXECUTION {
        @Override
        public boolean isResolved(Measurement measurement) {
            return measurement.isActualValueResolved();
        }

        @Override
        public void setValue(Measurement m, Object o) {
            if (o instanceof Double) {
                m.setActualValue((Double) o);
            } else if (o instanceof Enum) {
                m.setActualRating((Enum) o);
                if(o instanceof RankingIntervalEnum){
                    m.setActualValue(((RankingIntervalEnum) o).getValue());
                }
            }
        }

    };

    public boolean isResolved(Measurement measurement) {
        return false;
    }

    public void setValue(Measurement measurement, Object o) {

    }
}
