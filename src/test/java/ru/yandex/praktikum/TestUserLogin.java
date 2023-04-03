package ru.yandex.praktikum;

import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.yandex.praktikum.client.UserClient;
import ru.yandex.praktikum.model.User;
import ru.yandex.praktikum.model.UserCredentials;
import ru.yandex.praktikum.model.UserGenerator;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.CoreMatchers.is;

public class TestUserLogin {

    private UserClient userClient;
    private String userAccessToken;

    @BeforeClass
    public static void globalSetUp() {
        RestAssured.filters(
                new RequestLoggingFilter(), new ResponseLoggingFilter(),
                new AllureRestAssured()
        );
    }

    @Before
    public void setUp() {
        userClient = new UserClient();
        userAccessToken = null;
    }
    @After
    public void cleanUp(){
        if (userAccessToken != null){
            userClient.delete(userAccessToken);
        }
    }

    @Test
    @DisplayName("Логин под уже существующим юзером")
    public void UserCanBeLoggedInWithValidData() {
        User user = UserGenerator.getRandom();

        userClient.create(user)
                .assertThat()
                .body("success", is(true));
        userClient.login(UserCredentials.from(user))
                .assertThat()
                .statusCode(SC_OK);

        userAccessToken = userClient.login(UserCredentials.from(user))
                .extract().path("accessToken");

    }
    @Test
    @DisplayName("Невозможность логина с неверным паролем")
    public void UserCanNotBeLoggedInWithWrongPassword() {
        User user = UserGenerator.getRandom();

        userClient.create(user)
                .assertThat()
                .body("success", is(true));
        userAccessToken = userClient.login(UserCredentials.from(user))
                .extract().path("accessToken");

        user.setPassword(RandomStringUtils.randomAlphabetic(10));

        userClient.login(UserCredentials.from(user))
                .assertThat()
                .statusCode(SC_UNAUTHORIZED)
                .and()
                .assertThat()
                .body("success", is(false));
    }
    @Test
    @DisplayName("Невозможность логина с неверным email")
    public void UserCanNotBeLoggedInWithWrongEmail() {
        User user = UserGenerator.getRandom();

        userClient.create(user)
                .assertThat()
                .body("success", is(true));
        userAccessToken = userClient.login(UserCredentials.from(user))
                .extract().path("accessToken");

        user.setEmail(RandomStringUtils.randomAlphabetic(10) + "@yandex.ru");

        userClient.login(UserCredentials.from(user))
                .assertThat()
                .statusCode(SC_UNAUTHORIZED)
                .and()
                .assertThat()
                .body("success", is(false));



    }

}
