package ru.yandex.praktikum.client;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;

import static io.restassured.RestAssured.given;

public class OrderClient extends StellarRestClient {
    private static final String ORDER_URI = BASE_URI + "api/orders";

    @Step("Create order")
    public ValidatableResponse createOrder(String accessToken, String json){
        return given()
                .spec(getBaseReqSpec())
                .header("Authorization", accessToken)
                .and()
                .body(json)
                .when()
                .post(ORDER_URI)
                .then();
    }
}
