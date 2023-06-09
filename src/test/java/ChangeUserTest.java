import JsonData.Generator;
import JsonData.User;
import JsonData.UserCredentials;
import RequestData.UserClient;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ChangeUserTest {
    private UserClient userClient;
    private User user;
    private String accessToken;
    private final String AUTH_ERROR_MESSAGE = "You should be authorised";
    private final String EXIST_EMAIL_ERROR = "JsonData.User with such email already exists";


    @Before
    public void setUp() {
        user = Generator.getRandomUser();
        userClient = new UserClient();
        userClient.createUser(user);
        Response loginResponse = userClient.login(UserCredentials.from(user));
        accessToken = loginResponse.body().jsonPath().getString("accessToken");
    }

    @After
    public void clearData() {
        userClient.delete(accessToken);
    }

    @Test
    @DisplayName("Изменение данных")
    public void changeUserDataWithLogin() {
        User updateUser = Generator.getRandomUser();
        Response updateUserResponse = userClient.updateUser(updateUser, accessToken);

        int statusCode = updateUserResponse.getStatusCode();
        assertThat(statusCode, equalTo(SC_OK));

        boolean isUpdateUserResponseSuccess = updateUserResponse.jsonPath().getBoolean("success");
        assertTrue(isUpdateUserResponseSuccess);

        String email = updateUserResponse.jsonPath().getString("user.email");
        assertEquals(updateUser.getEmail().toLowerCase(), email);

        String name = updateUserResponse.jsonPath().getString("user.name");
        assertEquals(updateUser.getName(), name);
    }

    @Test
    @DisplayName("Изменение данных без авторизации")
    public void changeUserDataWithoutLogin() {
        Response UpdateUserResponse = userClient.updateUser(Generator.getRandomUser(), "");

        int statusCode = UpdateUserResponse.getStatusCode();
        assertThat(statusCode, equalTo(SC_UNAUTHORIZED));

        String message = UpdateUserResponse.jsonPath().getString("message");
        assertEquals(AUTH_ERROR_MESSAGE, message);
    }

    @Test
    @DisplayName("Изменение email с авторизацией")
    public void changeUserEmailTest() {
        User updatEmailUser = new User(Generator.getRandomUser().getEmail(), user.getPassword(), user.getName());
        Response UpdateUserResponse = userClient.updateUser(updatEmailUser, accessToken);

        int statusCode = UpdateUserResponse.getStatusCode();
        assertThat(statusCode, equalTo(SC_OK));

        boolean isUpdateUserResponseSuccess = UpdateUserResponse.jsonPath().getBoolean("success");
        assertTrue(isUpdateUserResponseSuccess);

        String email = UpdateUserResponse.jsonPath().getString("user.email");
        assertEquals(updatEmailUser.getEmail().toLowerCase(), email);

    }

    @Test
    @DisplayName("Изменение password с авторизацией")
    public void changeUserPasswordTest() {
        String newPassword = Generator.getRandomUser().getPassword();
        User newUser = new User(user.getEmail(), newPassword, user.getName());
        var UpdateUserResponse = userClient.updateUser(newUser, accessToken);

        boolean responseSuccess = UpdateUserResponse.jsonPath().getBoolean("success");
        Assert.assertTrue(responseSuccess);

        int statusCode = UpdateUserResponse.getStatusCode();
        Assert.assertEquals(statusCode, SC_OK);
    }

    @Test
    @DisplayName("Передаем почту, которая уже используется")
    public void changeEmailToUsedEmail() {
        User newUser = Generator.getRandomUser();
        userClient.createUser(newUser);

        Response responseLoginNewUser = userClient.login(UserCredentials.from(newUser));
        String newUserEmail = responseLoginNewUser.body().jsonPath().getString("user.email");

        User updateExistsEmailUser = new User(newUserEmail, user.getPassword(), user.getName());
        Response responseRegUpdateUser = userClient.updateUser(updateExistsEmailUser, accessToken);

        int statusCode = responseRegUpdateUser.getStatusCode();
        assertThat(statusCode, equalTo(SC_FORBIDDEN));

        String message = responseRegUpdateUser.jsonPath().getString("message");
        assertEquals(EXIST_EMAIL_ERROR, message);
    }

}
