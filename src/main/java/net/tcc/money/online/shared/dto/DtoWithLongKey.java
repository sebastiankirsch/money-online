package net.tcc.money.online.shared.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

public abstract class DtoWithLongKey implements IsSerializable {

    private Long key;


    @Deprecated
    @SuppressWarnings("unused") // for GWT
    protected DtoWithLongKey() {
        this(null);
    }

    protected DtoWithLongKey(Long key) {
        this.key = key;
    }

    @Override
    public int hashCode() {
        return key != null ? key.intValue():0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || key == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DtoWithLongKey other = (DtoWithLongKey) obj;
        return key.equals(other.key);
    }

    @Override
    public String toString() {
        String className = getClass().getName();
        className = className.substring(className.lastIndexOf('.') + 1);
        return className + " #" + getKey();
    }

    public long getKey() {
        return key;
    }

}
