package net.tcc.money.online.server;

import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import net.tcc.money.online.shared.dto.Category;
import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public final class ShoppingServiceImplTest extends ServerSideTest {

    private final ShoppingServiceImpl objectUnderTest = new ShoppingServiceImpl();

    public ShoppingServiceImplTest(){
        super(true);
    }

    @Test
    public final void canCreateCategory() {
        String name = "ACME";
        Category category = objectUnderTest.createCategory(name, null);

        assertThat("Key is NOT set!", category.getKey(), is(notNullValue()));
        assertThat("Name should be stored!", category.getName(), is(name));
        assertThat("A parent is set!", category.getParent(), is(nullValue()));
    }

}
