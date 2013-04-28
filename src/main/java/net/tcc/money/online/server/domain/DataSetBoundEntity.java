package net.tcc.money.online.server.domain;

import net.tcc.gae.AbstractEntity;
import net.tcc.money.online.shared.Constants;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
@Inheritance(strategy = InheritanceStrategy.SUBCLASS_TABLE)
public abstract class DataSetBoundEntity extends AbstractEntity<Long> implements Serializable {

	private static final long serialVersionUID = Constants.SERIAL_VERSION;

    @Nullable
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    @PrimaryKey
    private Long key;

    @Nonnull
	@Persistent(nullValue = NullValue.EXCEPTION)
	private String dataSetId;

	@Deprecated
	protected DataSetBoundEntity() { // for JDO
		super();
	}

	protected DataSetBoundEntity(@Nonnull String dataSetId) {
		this.dataSetId = dataSetId;
	}

    @Nonnull
    @Override
	public Long getKey() {
		return key;
	}

}
