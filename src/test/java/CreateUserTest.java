import JsonData.Generator;
import JsonData.User;
import RequestData.UserClient;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CreateUserTest {
    private UserClient userClient;
    private User user;
    private String accessToken;

    private final String someUserErrorMessage = "JsonData.User already exists";

    @Before
    public void setUp() {
        user = Generator.getRandomUser();
        userClient = new UserClient();
    }

    @After
    public void clearData() {
        userClient.delete(accessToken);
    }

    @Test
    @DisplayName("Создать уникального пользователя")
    public void userSuccessCreateTest() {
        Response responseReg = userClient.createUser(user);

        int statusCode = responseReg.getStatusCode();
        assertThat(statusCode, equalTo(SC_OK));

        boolean successCreated = responseReg.jsonPath().getBoolean("success");
        assertTrue(successCreated);

        accessToken = responseReg.body().jsonPath().getString("accessToken");
        assertNotNull(accessToken);

        String refreshToken = responseReg.body().jsonPath().getString("accessToken");
        assertNotNull(refreshToken);
    }

    @Test
    @DisplayName("Cоздать пользователя, который уже зарегистрирован")
    public void createAnExistingUserTest() {
        userClient.createUser(user);
        Response doubleReg = userClient.createUser(user);

        String message = doubleReg.jsonPath().getString("message");
        accessToken = doubleReg.body().jsonPath().getString("accessToken");

        int statusCode = doubleReg.getStatusCode();
        assertThat(statusCode, equalTo(SC_FORBIDDEN));

        accessToken = doubleReg.body().jsonPath().getString("accessToken");
        assertThat(message, equalTo(someUserErrorMessage));
    }

}

