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

import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.is;

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
        ValidatableResponse createResponse = userClient.create(user);
        userAccessToken = createResponse.extract().path("accessToken");

        createResponse
                .assertThat()
                .statusCode(SC_OK)
                .and()
                .body("success", is(true));

    }
    @Test
    @DisplayName("Невозможно создать уже существующего юзера")
    public void SameUserCanNotBeCreatedTwice() {
        User user = UserGenerator.getRandom();
        ValidatableResponse createResponse = userClient.create(user);
        createResponse
                .assertThat()
                .body("success", is(true));
        userClient.create(user) //чтобы произошел новый вызов
                .assertThat()
                .body("success", is(false));
        userAccessToken = createResponse.extract().path("accessToken");
    }
    @Test
    @DisplayName("Невозможно создать юзера без email")
    public void UserCanNotBeCreatedWithoutEmail() {
        User user = UserGenerator.getRandom();
        user.setEmail(null);
        userClient.create(user)
                .assertThat()
                .body("success", is(false));
    }
    @Test
    @DisplayName("Невозможно создать юзера без пароля")
    public void UserCanNotBeCreatedWithoutPassword() {
        User user = UserGenerator.getRandom();
        user.setPassword(null);
        userClient.create(user)
                .assertThat()
                .body("success", is(false));
    }
    @Test
    @DisplayName("Невозможно создать юзера без имени")
    public void UserCanNotBeCreatedWithoutName() {
        User user = UserGenerator.getRandom();
        user.setName(null);
        userClient.create(user)
                .assertThat()
                .body("success", is(false));
    }



}
