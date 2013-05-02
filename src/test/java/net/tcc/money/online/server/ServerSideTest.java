package net.tcc.money.online.server;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import org.junit.After;
import org.junit.Before;

import java.util.HashMap;

public abstract class ServerSideTest {

    private final LocalServiceTestHelper localServices;

    protected ServerSideTest(boolean withLoggedInUser) {
        localServices = new LocalServiceTestHelper(
                new LocalUserServiceTestConfig(),
                new LocalDatastoreServiceTestConfig());
        if (withLoggedInUser) {
            localServices.setEnvIsLoggedIn(true)
                    .setEnvAuthDomain("tcc.net")
                    .setEnvEmail("junit@tcc.net")
                    .setEnvAttributes(new HashMap<String, Object>() {
                        {
                            put("com.google.appengine.api.users.UserService.user_id_key", "JUnit");
                        }
                    });
        }
    }

    @Before
    public final void setUpLocalServices() {
        this.localServices.setUp();
    }

    @After
    public final void tearDownLocalServices() {
        this.localServices.tearDown();
    }

}
