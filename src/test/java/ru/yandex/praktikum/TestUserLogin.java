package ru.yandex.praktikum;

import com.github.javafaker.Faker;
import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.ValidatableResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.yandex.praktikum.client.UserClient;
import ru.yandex.praktikum.model.User;
import ru.yandex.praktikum.model.UserCredentials;
import ru.yandex.praktikum.model.UserGenerator;

import java.util.Locale;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

public class TestUserLogin {

    private UserClient userClient;
    private String userAccessToken;
    static Faker faker = new Faker(new Locale("en"));


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
    public void userCanBeLoggedInWithValidData() {
        User user = UserGenerator.getRandom();
        ValidatableResponse createResponse = userClient.createUser(user);
        createResponse
                .assertThat()
                .body("success", is(true));
        userAccessToken = createResponse.extract().path("accessToken");
        System.out.println(userAccessToken);
        userClient.login(UserCredentials.from(user))
                .assertThat()
                .statusCode(SC_OK)
                .and()
                .assertThat()
                .body("success", is(true))
                .and()
                .assertThat()
                .body("accessToken", is(notNullValue())) //после логина токен изменяется
                .and()
                .assertThat()
                .body("refreshToken", is(notNullValue()))
                .and()
                .assertThat()
                .body("user.email", is(user.getEmail().toLowerCase()))
                .and()
                .assertThat()
                .body("user.name", is(user.getName()));



    }
    @Test
    @DisplayName("Невозможность логина с неверным паролем")
    public void userCanNotBeLoggedInWithWrongPassword() {
        User user = UserGenerator.getRandom();
        ValidatableResponse createResponse = userClient.createUser(user);
        createResponse
                .assertThat()
                .body("success", is(true));
        userAccessToken = createResponse.extract().path("accessToken");

        user.setPassword(RandomStringUtils.randomAlphabetic(10));

        userClient.login(UserCredentials.from(user))
                .assertThat()
                .statusCode(SC_UNAUTHORIZED)
                .and()
                .assertThat()
                .body("success", is(false))
                .and()
                .assertThat()
                .body("message", is("email or password are incorrect"));
    }
    @Test
    @DisplayName("Невозможность логина с неверным email")
    public void userCanNotBeLoggedInWithWrongEmail() {
        User user = UserGenerator.getRandom();
        ValidatableResponse createResponse = userClient.createUser(user);
        createResponse
                .assertThat()
                .body("success", is(true));
        userAccessToken = createResponse.extract().path("accessToken");

        user.setEmail(faker.internet().safeEmailAddress());

        userClient.login(UserCredentials.from(user))
                .assertThat()
                .statusCode(SC_UNAUTHORIZED)
                .and()
                .assertThat()
                .body("success", is(false))
                .and()
                .assertThat()
                .body("message", is("email or password are incorrect"));



    }

}
