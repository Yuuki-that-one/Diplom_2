package ru.yandex.praktikum;

import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.yandex.praktikum.client.UserClient;
import ru.yandex.praktikum.model.User;
import ru.yandex.praktikum.model.UserGenerator;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

public class TestUserCreate {

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
    @DisplayName("Создание юзера с валидными параметрами")
    public void UserCanBeCreatedWithValidData() {
        User user = UserGenerator.getRandom();
        ValidatableResponse createResponse = userClient.createUser(user);
        userAccessToken = createResponse.extract().path("accessToken");

        createResponse
                .assertThat()
                .statusCode(SC_OK)
                .and()
                .body("success", is(true))
                .and()
                .body("user.email", is(user.getEmail()))
                .and()
                .body("user.name", is(user.getName()))
                .and()
                .body("accessToken", is(notNullValue()))
                .and()
                .body("refreshToken", is(notNullValue()));

    }
    @Test
    @DisplayName("Невозможно создать уже существующего юзера")
    public void SameUserCanNotBeCreatedTwice() {
        User user = UserGenerator.getRandom();
        ValidatableResponse createResponse = userClient.createUser(user);
        createResponse
                .assertThat()
                .body("success", is(true));
        userAccessToken = createResponse.extract().path("accessToken");
        userClient.createUser(user) //чтобы произошел новый вызов
                .assertThat()
                .statusCode(SC_FORBIDDEN)
                .and()
                .assertThat()
                .body("success", is(false))
                .and()
                .assertThat()
                .body("message", is("User already exists"));

    }
    @Test
    @DisplayName("Невозможно создать юзера без email")
    public void UserCanNotBeCreatedWithoutEmail() {
        User user = UserGenerator.getRandom();
        user.setEmail(null);
        userClient.createUser(user)
                .assertThat()
                .statusCode(SC_FORBIDDEN)
                .and()
                .assertThat()
                .body("success", is(false))
                .and()
                .assertThat()
                .body("message", is("Email, password and name are required fields"));
    }
    @Test
    @DisplayName("Невозможно создать юзера без пароля")
    public void UserCanNotBeCreatedWithoutPassword() {
        User user = UserGenerator.getRandom();
        user.setPassword(null);
        userClient.createUser(user)
                .assertThat()
                .statusCode(SC_FORBIDDEN)
                .and()
                .assertThat()
                .body("success", is(false))
                .and()
                .assertThat()
                .body("message", is("Email, password and name are required fields"));
    }
    @Test
    @DisplayName("Невозможно создать юзера без имени")
    public void UserCanNotBeCreatedWithoutName() {
        User user = UserGenerator.getRandom();
        user.setName(null);
        userClient.createUser(user)
                .assertThat()
                .statusCode(SC_FORBIDDEN)
                .and()
                .assertThat()
                .body("success", is(false))
                .and()
                .assertThat()
                .body("message", is("Email, password and name are required fields"));
    }



}
