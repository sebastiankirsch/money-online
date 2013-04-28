package net.tcc.money.online.server.domain;

import net.tcc.money.online.shared.Constants;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
@Inheritance(strategy = InheritanceStrategy.SUBCLASS_TABLE)
public abstract class DataSetBoundEntity implements Serializable {

	private static final long serialVersionUID = Constants.SERIAL_VERSION;

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long key;

	@Persistent(nullValue = NullValue.EXCEPTION)
	private String dataSetId;

	@Deprecated
	protected DataSetBoundEntity() { // for JDO
		super();
	}

	protected DataSetBoundEntity(@Nonnull String dataSetId) {
		this.dataSetId = dataSetId;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || key == null) {
			return false;
		}
		if (o == this) {
			return true;
		}
		if (o.getClass() != getClass()) {
			return false;
		}
		DataSetBoundEntity other = (DataSetBoundEntity) o;
		return key.equals(other.key);
	}

	@Override
	public int hashCode() {
		return key == null ? Integer.MIN_VALUE : key.hashCode();
	}

	public Long getKey() {
		return key;
	}

}
