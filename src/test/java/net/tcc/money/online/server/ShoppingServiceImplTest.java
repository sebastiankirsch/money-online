package net.tcc.money.online.server;

import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
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

public class ShoppingServiceImplTest {

    private final LocalServiceTestHelper localServices = new LocalServiceTestHelper(
            new LocalUserServiceTestConfig(),
            new LocalDatastoreServiceTestConfig().setDefaultHighRepJobPolicyUnappliedJobPercentage(100))
            .setEnvIsLoggedIn(true)
            .setEnvAuthDomain("tcc.net")
            .setEnvEmail("junit@tcc.net")
            .setEnvAttributes(new HashMap<String, Object>() {
                {
                    put("com.google.appengine.api.users.UserService.user_id_key", "JUnit");
                }
            });

    private final ShoppingServiceImpl objectUnderTest = new ShoppingServiceImpl();

    @Before
    public final void setUpLocalServices() {
        this.localServices.setUp();
    }

    @After
    public final void tearDownLocalServices() {
        this.localServices.tearDown();
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
