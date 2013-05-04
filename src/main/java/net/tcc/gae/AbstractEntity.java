package net.tcc.gae;

public abstract class AbstractEntity<Key> {

    protected abstract Key getKey();

    @Override
    public String toString() {
        return getClass().getSimpleName() + "#" + getKey();
    }

    @Override
    @SuppressWarnings("SimplifiableIfStatement")
    public boolean equals(Object o) {
        Key key = getKey();
        if (o == null || key == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (o.getClass() != getClass()) {
            return false;
        }
        return key.equals(((AbstractEntity<?>) o).getKey());
    }

    @Override
    public int hashCode() {
        Key key = getKey();
        return key == null ? Integer.MIN_VALUE : key.hashCode();
    }

}
