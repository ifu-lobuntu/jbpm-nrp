package org.jbpm.vdml.services.impl.model.meta;


public enum BinaryFunctor {
    PLUS {
        public double apply(double valueA, double valueB) {
            return valueA + valueB;
        }
    }, MINUS {
        public double apply(double valueA, double valueB) {
            return valueA - valueB;
        }
    }, MULTIPLY {
        public double apply(double valueA, double valueB) {
            return valueA * valueB;
        }
    }, DIVIDE {
        public double apply(double valueA, double valueB) {
            return valueA / valueB;
        }
    };

    public double apply(double valueA, double valueB) {
        return 0.0;
    }
}
