package ru.yandex.praktikum.client;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import ru.yandex.praktikum.model.User;
import ru.yandex.praktikum.model.UserCredentials;

import static io.restassured.RestAssured.given;
public class UserClient extends StellarRestClient{

    private static final String USER_URI = BASE_URI + "api/auth/";

    @Step("Create user {user}")
    public ValidatableResponse createUser(User user) {
        return given()
                .spec(getBaseReqSpec())
                .body(user)
                .when()
                .post(USER_URI + "register")
                .then();
    }
    @Step("Login as {userCredentials}")
    public ValidatableResponse login(UserCredentials userCredentials) {
        return given()
                .spec(getBaseReqSpec())
                .body(userCredentials)
                .when()
                .post(USER_URI + "login/")
                .then();
    }
    @Step("Delete user")
    public ValidatableResponse delete(String accessToken) {
        return given()
                .spec(getBaseReqSpec())
                .header("Authorization", accessToken)
                .when()
                .delete(USER_URI + "user/")
                .then();
    }
    @Step("Get user info from server")
    public ValidatableResponse getUserInfo(String accessToken) {
        return given()
                .spec(getBaseReqSpec())
                .header("Authorization", accessToken)
                .when()
                .get(USER_URI + "user/")
                .then();
    }
    @Step("Get user info from server without authorization")
    public ValidatableResponse getUserInfoWithoutAuth() {
        return given()
                .spec(getBaseReqSpec())
                .when()
                .get(USER_URI + "user/")
                .then();
    }
    @Step("Edit user info from server")
    public ValidatableResponse editUserInfo(String accessToken, String json) {
        return given()
                .spec(getBaseReqSpec())
                .header("Authorization", accessToken)
                .and()
                .body(json)
                .when()
                .patch(USER_URI + "user/")
                .then();
    }
    @Step("Edit user info from server without Authorization")
    public ValidatableResponse editUserInfoWithoutAuth(String json) {
        return given()
                .spec(getBaseReqSpec())
                .and()
                .body(json)
                .when()
                .patch(USER_URI + "user/")
                .then();
    }
}
