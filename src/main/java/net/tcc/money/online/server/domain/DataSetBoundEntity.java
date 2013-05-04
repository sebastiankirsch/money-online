package net.tcc.money.online.server.domain;

import net.tcc.gae.AbstractEntity;
import net.tcc.money.online.shared.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jdo.annotations.*;
import java.io.Serializable;

@Inheritance(strategy = InheritanceStrategy.SUBCLASS_TABLE)
@PersistenceCapable
public abstract class DataSetBoundEntity extends AbstractEntity<Long> implements Serializable {

    private static final long serialVersionUID = Constants.SERIAL_VERSION;

    @Nullable
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    @PrimaryKey
    @SuppressWarnings("unused") // written by JDO
    private Long key;

    @Nonnull
    @Persistent(nullValue = NullValue.EXCEPTION)
    @SuppressWarnings({"unused", "FieldCanBeLocal"}) // used in queries etc
    private String dataSetId;

    @Deprecated
    protected DataSetBoundEntity() { // for JDO
        super();
    }

    protected DataSetBoundEntity(@Nonnull String dataSetId) {
        this.dataSetId = dataSetId;
    }

    @Nullable
    @Override
    public Long getKey() {
        return key;
    }

    @Nonnull
    public Long getKeyOrThrow() throws IllegalStateException {
        Long key = getKey();
        if (key == null)
            throw new IllegalStateException("No key is set!");
        return key;
    }

}
