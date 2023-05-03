import JsonData.Generator;
import JsonData.User;
import JsonData.UserCredentials;
import RequestData.UserClient;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LoginUserTest {
    private User user;
    private UserClient userClient;
    private String accessToken;
    private final String LOGIN_ERROR_MESSAGE = "email or password are incorrect";

    @Before
    public void setUp() {
        user = Generator.getRandomUser();
        userClient = new UserClient();
        userClient.createUser(user);
    }

    @After
    public void tearDown() {
        userClient.delete(accessToken);
    }

    @Test
    @DisplayName("Логин под существующим пользователем")
    public void userSuccessLoginTest() {
        Response loginResponse = userClient.login(UserCredentials.from(user));

        int statusCode = loginResponse.getStatusCode();
        assertThat(statusCode, equalTo(SC_OK));

        boolean successLogin = loginResponse.body().jsonPath().getBoolean("success");
        assertTrue(successLogin);

        accessToken = loginResponse.body().jsonPath().getString("accessToken");
        assertNotNull(accessToken);

        String refreshToken = loginResponse.body().jsonPath().getString("accessToken");
        assertNotNull(refreshToken);
    }

    @Test
    @DisplayName("логин с неверным логином и паролем")
    public void loginWithInvalidFieldTest() {
        Response loginResponse = userClient.login(Generator.getRandomUserCredentials());

        int statusCode = loginResponse.getStatusCode();
        assertThat(statusCode, equalTo(SC_UNAUTHORIZED));

        String message = loginResponse.body().jsonPath().getString("message");
        assertThat(message, equalTo(LOGIN_ERROR_MESSAGE));
    }

}
