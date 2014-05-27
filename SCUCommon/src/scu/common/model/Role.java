package scu.common.model;

import scu.common.sla.Specification;

public class Role {

    protected Functionality functionality;
    protected Specification specification;
    
    public Role(Functionality functionality) {
        this(functionality, null);
    }    

    public Role(Functionality functionality, Specification specification) {
        this.functionality = functionality;
        this.specification = specification;
    }

    public Functionality getFunctionality() {
        return functionality;
    }

    public void setFunctionality(Functionality functionality) {
        this.functionality = functionality;
    }

    public Specification getSpecification() {
        return specification;
    }

    public void setSpecification(Specification specification) {
        this.specification = specification;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((functionality == null) ? 0 : functionality.hashCode());
        return result;
    }

    // Two roles with the same functionality are considered equal
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Role other = (Role) obj;
        if (functionality == null) {
            if (other.functionality != null)
                return false;
        } else if (!functionality.equals(other.functionality))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "[func=" + functionality + ", spec="
                + specification + "]";
    }
    
    
    
}
