package at.ac.tuwien.dsg.hcu.rest.resource.simulation;


import java.util.List;

/**
 * Created by karaoglan on 03/12/15.
 */
public class Task {
    private Integer seed;
    private TaskTypes taskTypes;

    public Integer getSeed() {
        return seed;
    }

    public void setSeed(Integer seed) {
        this.seed = seed;
    }

    public TaskTypes getTaskTypes() {
        return taskTypes;
    }

    public void setTaskTypes(TaskTypes taskTypes) {
        this.taskTypes = taskTypes;
    }

    public static class TaskTypes {
        private String name;
        private String description;
        private Boolean isRootTask;
        private TasksOccurance tasksOccurance;
        private Load load;
        private List<Role> roles;
        private List<SubTaskType> subTaskTypes;
        private List<Specification> specification;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Boolean getIsRootTask() {
            return isRootTask;
        }

        public void setIsRootTask(Boolean isRootTask) {
            this.isRootTask = isRootTask;
        }

        public TasksOccurance getTasksOccurance() {
            return tasksOccurance;
        }

        public void setTasksOccurance(TasksOccurance tasksOccurance) {
            this.tasksOccurance = tasksOccurance;
        }

        public Load getLoad() {
            return load;
        }

        public void setLoad(Load load) {
            this.load = load;
        }

        public List<Role> getRoles() {
            return roles;
        }

        public void setRoles(List<Role> roles) {
            this.roles = roles;
        }

        public List<SubTaskType> getSubTaskTypes() {
            return subTaskTypes;
        }

        public void setSubTaskTypes(List<SubTaskType> subTaskTypes) {
            this.subTaskTypes = subTaskTypes;
        }

        public List<Specification> getSpecification() {
            return specification;
        }

        public void setSpecification(List<Specification> specification) {
            this.specification = specification;
        }

        public static class TasksOccurance {
            private String clazz;
            private List<String> params;
            private String sampleMethod;

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

            public String getSampleMethod() {
                return sampleMethod;
            }

            public void setSampleMethod(String sampleMethod) {
                this.sampleMethod = sampleMethod;
            }
        }

        public static class Load {
            private String clazz;
            private List<String> params;

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

        public static class Role {
            private String functionality;
            private Double probabilityToHave;
            private Double relativeLoadRatio;
            private List<String> dependsOn;
            private List<Specification> specification;
            private List<Specification> _specification; //todo @karaoglan ask specification param needed?

            public List<String> getDependsOn() {
                return dependsOn;
            }

            public void setDependsOn(List<String> dependsOn) {
                this.dependsOn = dependsOn;
            }

            public List<Specification> getSpecification() {
                return specification;
            }

            public void setSpecification(List<Specification> specification) {
                this.specification = specification;
            }

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

            public Double getRelativeLoadRatio() {
                return relativeLoadRatio;
            }

            public void setRelativeLoadRatio(Double relativeLoadRatio) {
                this.relativeLoadRatio = relativeLoadRatio;
            }

            public List<Specification> get_specification() {
                return _specification;
            }

            public void set_specification(List<Specification> _specification) {
                this._specification = _specification;
            }


        }

        public static class Specification {
            private String type;
            private String name;
            private Value value;
            private Double probabilityToHave;
            private String comparator;

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

            public String getComparator() {
                return comparator;
            }

            public void setComparator(String comparator) {
                this.comparator = comparator;
            }

            public static class Value {
                private String clazz;
                private List<String> params;
                private Mapping mapping;

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

                public static class Mapping {
                    private String first;
                    private String second;
                    private String third;
                    private String fourth;

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

                    public String getFourth() {
                        return fourth;
                    }

                    public void setFourth(String fourth) {
                        this.fourth = fourth;
                    }
                }
            }
        }

        public static class SubTaskType {
            //todo @karaoglan no information saved in json ? needed?
        }
    }


}
