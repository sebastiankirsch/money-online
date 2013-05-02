package net.tcc.money.online.server;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import org.junit.After;
import org.junit.Before;

import java.util.HashMap;

public abstract class ServerSideTest {

    public static final String DATA_SET_ID = "JUnit";

    private final LocalServiceTestHelper localServices;

    protected ServerSideTest(boolean withLoggedInUser) {
        localServices = new LocalServiceTestHelper(
                new LocalUserServiceTestConfig(),
                new LocalDatastoreServiceTestConfig(),
                new LocalTaskQueueTestConfig().setQueueXmlPath("src/main/webapp/WEB-INF/queue.xml"));
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
