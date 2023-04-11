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

public class TestUserEdit {

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
    @DisplayName("Получение данных пользователя с авторизацией")
    public void canReceiveUserInfoWithAuth() {
        User user = UserGenerator.getRandom();
        ValidatableResponse createResponse = userClient.createUser(user);
        createResponse
                .assertThat()
                .body("success", is(true));
        userAccessToken = createResponse.extract().path("accessToken");
        userClient.getUserInfo(userAccessToken)
                .assertThat()
                .statusCode(SC_OK)
                .and()
                .assertThat()
                .body("success", is(true))
                .and()
                .assertThat()
                .body("user.email", is(user.getEmail().toLowerCase()))
                .and()
                .assertThat()
                .body("user.name", is(user.getName()));
    }
    @Test
    @DisplayName("Неудачное получение данных пользователя без авторизации")
    public void canNotReceiveUserInfoWithoutAuth() {
        User user = UserGenerator.getRandom();
        ValidatableResponse createResponse = userClient.createUser(user);
        createResponse
                .assertThat()
                .body("success", is(true));
        userAccessToken = createResponse.extract().path("accessToken");
        userClient.getUserInfoWithoutAuth()
                .assertThat()
                .statusCode(SC_UNAUTHORIZED)
                .and()
                .assertThat()
                .body("message", is("You should be authorised"));

    }
    @Test
    @DisplayName("Редактирование данных пользователя с авторизацией")
    public void canEditUserInfoWithAuth() {
        User user = UserGenerator.getRandom();
        ValidatableResponse createResponse = userClient.createUser(user);
        createResponse
                .assertThat()
                .body("success", is(true));
        userAccessToken = createResponse.extract().path("accessToken");
        String newEmail = faker.internet().safeEmailAddress();
        String newPassword = RandomStringUtils.randomAlphabetic(10);
        String newName = RandomStringUtils.randomAlphabetic(10);
        String json = "{\n" +
                "    \"email\": \"" + newEmail + "\",\n" +
                "    \"password\": \"" + newPassword + "\",\n" +
                "    \"name\": \"" + newName + "\"\n" +
                "}";
        userClient.editUserInfo(userAccessToken, json)  //проверяем, что после редактирования возвращаются новые email и имя (пароль тут не приходит)
                .assertThat()
                .statusCode(SC_OK)
                .and()
                .assertThat()
                .body("success", is(true))
                .and()
                .assertThat()
                .body("user.email", is(newEmail.toLowerCase()))
                .and()
                .assertThat()
                .body("user.name", is(newName));
        userClient.login(UserCredentials.fromDirect(newEmail, newPassword)) //проверяем, что новый логин и пароль подходят
                .assertThat()
                .body("success", is(true));
    }
    @Test
    @DisplayName("Редактирование данных пользователя без авторизации")
    public void canNotEditUserInfoWithoutAuth() {
        User user = UserGenerator.getRandom();
        ValidatableResponse createResponse = userClient.createUser(user);
        createResponse
                .assertThat()
                .body("success", is(true));
        userAccessToken = createResponse.extract().path("accessToken");
        String newEmail = faker.internet().safeEmailAddress();
        String newPassword = RandomStringUtils.randomAlphabetic(10);
        String newName = RandomStringUtils.randomAlphabetic(10);
        String json = "{\n" +
                "    \"email\": \"" + newEmail + "\",\n" +
                "    \"password\": \"" + newPassword + "\",\n" +
                "    \"name\": \"" + newName + "\"\n" +
                "}";
        userClient.editUserInfoWithoutAuth(json)  //отправляем json методом patch без токена
                .statusCode(SC_UNAUTHORIZED)
                .and()
                .assertThat()
                .body("success", is(false))
                .and()
                .assertThat()
                .body("message", is("You should be authorised"));
    }
}
