package net.tcc.gae;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class AbstractEntityTest {

    @Test
    public void shouldMatchSameEntity() {
        LongEntity entity = new LongEntity(42l);

        assertThat(entity, is(equalTo(entity)));
    }

    @Test
    public void shouldNotMatchNull() {
        LongEntity entity = new LongEntity(42l);

        assertThat(entity, is(not(equalTo(null))));
    }

    @Test
    public void shouldNotMatchSomeRandomObject() {
        LongEntity entity = new LongEntity(42l);

        assertThat(entity, is(not(equalTo(new Object()))));
    }

    @Test
    public void shouldNotMatchDifferentEntityOfSameClassIfKeyIsNull() {
        LongEntity entity = new LongEntity(null);
        LongEntity otherEntity = new LongEntity(null);

        assertThat(entity, is(not(equalTo(otherEntity))));
    }

    @Test
    public void shouldNotMatchDifferentEntityOfOtherClassWithSameKey() {
        AbstractEntity<Long> entity = new LongEntity(42l);
        AbstractEntity<Long> otherEntity = new AnotherLongEntity(42l);

        assertThat(entity, is(not(equalTo(otherEntity))));
    }

    @Test
    public void shouldMatchDifferentEntityOfSameClassWithSameKey() {
        LongEntity entity = new LongEntity(42l);
        LongEntity otherEntity = new LongEntity(42l);

        assertThat(entity, is(equalTo(otherEntity)));
    }

    public static class LongEntity extends AbstractEntity<Long> {
        private final Long key;

        public LongEntity(Long key) {
            this.key = key;
        }

        @Override
        protected Long getKey() {
            return this.key;
        }
    }

    public static class AnotherLongEntity extends AbstractEntity<Long> {
        private final Long key;

        public AnotherLongEntity(Long key) {
            this.key = key;
        }

        @Override
        protected Long getKey() {
            return this.key;
        }
    }

}
