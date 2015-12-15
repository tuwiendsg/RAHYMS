package at.ac.tuwien.dsg.hcu.rest.resource.simulation;

import java.util.List;

/**
 * Created by karaoglan on 22/10/15.
 */
public class Unit {

    private Integer seed;
    private Integer numberOfElements;
    private String  namePrefix;
    private Boolean singleElementSingleServices;
    private Connectedness connectedness;
    private List<ProvidedService> providedServices;
    private List<CommonProperties> commonProperties;

    public Unit() {}

    public Integer getSeed() {
        return seed;
    }

    public void setSeed(Integer seed) {
        this.seed = seed;
    }

    public Integer getNumberOfElements() {
        return numberOfElements;
    }

    public void setNumberOfElements(Integer numberOfElements) {
        this.numberOfElements = numberOfElements;
    }

    public String getNamePrefix() {
        return namePrefix;
    }

    public void setNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    public Boolean getSingleElementSingleServices() {
        return singleElementSingleServices;
    }

    public void setSingleElementSingleServices(Boolean singleElementSingleServices) {
        this.singleElementSingleServices = singleElementSingleServices;
    }

    public Connectedness getConnectedness() {
        return connectedness;
    }

    public void setConnectedness(Connectedness connectedness) {
        this.connectedness = connectedness;
    }

    public List<ProvidedService> getProvidedServices() {
        return providedServices;
    }

    public void setProvidedServices(List<ProvidedService> providedServices) {
        this.providedServices = providedServices;
    }

    public List<CommonProperties> getCommonProperties() {
        return commonProperties;
    }

    public void setCommonProperties(List<CommonProperties> commonProperties) {
        this.commonProperties = commonProperties;
    }

    private static class Connectedness {
        private Double probabilityToConnect;
        private Weight weight;

        public Connectedness() {}

        public Double getProbabilityToConnect() {
            return probabilityToConnect;
        }

        public void setProbabilityToConnect(Double probabilityToConnect) {
            this.probabilityToConnect = probabilityToConnect;
        }

        public Weight getWeight() {
            return weight;
        }

        public void setWeight(Weight weight) {
            this.weight = weight;
        }

        private static class Weight {

            private String clazz;
            private List<String> params;

            public Weight(){}

            public String getClazz() {
                return clazz;
            }

            public void setClazz(String clazz) {
                this.clazz = clazz;
            }

            public List<String> getParams() {
                return params;
            }

            public void setParams(List<String> params) {
                this.params = params;
            }
        }
    }

    private static class ProvidedService {

        private String functionality;
        private Double probabilityToHave;
        private List<Properties> properties;

        public ProvidedService() {}

        public String getFunctionality() {
            return functionality;
        }

        public void setFunctionality(String functionality) {
            this.functionality = functionality;
        }

        public Double getProbabilityToHave() {
            return probabilityToHave;
        }

        public void setProbabilityToHave(Double probabilityToHave) {
            this.probabilityToHave = probabilityToHave;
        }

        public List<Properties> getProperties() {
            return properties;
        }

        public void setProperties(List<Properties> properties) {
            this.properties = properties;
        }

        private static class Properties {

            private String type;
            private String name;
            private Value value;
            private Double probabilityToHave;

            public Properties() {}

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public Value getValue() {
                return value;
            }

            public void setValue(Value value) {
                this.value = value;
            }

            public Double getProbabilityToHave() {
                return probabilityToHave;
            }

            public void setProbabilityToHave(Double probabilityToHave) {
                this.probabilityToHave = probabilityToHave;
            }

            private static class Value {

                private String clazz;
                private List<String> params;

                public Value() {}

                public String getClazz() {
                    return clazz;
                }

                public void setClazz(String clazz) {
                    this.clazz = clazz;
                }

                public List<String> getParams() {
                    return params;
                }

                public void setParams(List<String> params) {
                    this.params = params;
                }
            }

        }
    }

    private static class CommonProperties {

        private String type;
        private String name;
        private Value value;
        private Integer _value;
        private Integer integerValue;
        private String interfaceClass;
        private Double probabilityToHave;

        public CommonProperties() {}

        public Integer get_value() {
            return _value;
        }

        public void set_value(Integer _value) {
            this._value = _value;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Value getValue() {
            return value;
        }

        public void setValue(Value value) {
            this.value = value;
        }

        public Integer getIntegerValue() {
            return integerValue;
        }

        public void setIntegerValue(Integer integerValue) {
            this.integerValue = integerValue;
        }

        public String getInterfaceClass() {
            return interfaceClass;
        }

        public void setInterfaceClass(String interfaceClass) {
            this.interfaceClass = interfaceClass;
        }

        public Double getProbabilityToHave() {
            return probabilityToHave;
        }

        public void setProbabilityToHave(Double probabilityToHave) {
            this.probabilityToHave = probabilityToHave;
        }

        private static class Value {

            private String clazz;
            private List<String> params;
            private Mapping mapping;

            public Value() {}

            public String getClazz() {
                return clazz;
            }

            public void setClazz(String clazz) {
                this.clazz = clazz;
            }

            public List<String> getParams() {
                return params;
            }

            public void setParams(List<String> params) {
                this.params = params;
            }

            public Mapping getMapping() {
                return mapping;
            }

            public void setMapping(Mapping mapping) {
                this.mapping = mapping;
            }

            private static class Mapping {

                private String first;
                private String second;
                private String third;

                public Mapping() {}

                public String getFirst() {
                    return first;
                }

                public void setFirst(String first) {
                    this.first = first;
                }

                public String getSecond() {
                    return second;
                }

                public void setSecond(String second) {
                    this.second = second;
                }

                public String getThird() {
                    return third;
                }

                public void setThird(String third) {
                    this.third = third;
                }
            }
        }
    }
}
