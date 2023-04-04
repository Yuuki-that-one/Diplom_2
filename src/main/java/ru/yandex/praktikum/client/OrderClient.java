package ru.yandex.praktikum.client;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;

import static io.restassured.RestAssured.given;

public class OrderClient extends StellarRestClient {
    private static final String ORDER_URI = BASE_URI + "api/orders";

    @Step("Create order with Authorization")
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
    @Step("Create order without Authorization")
    public ValidatableResponse createOrderWithoutAuth(String json){
        return given()
                .spec(getBaseReqSpec())
                .and()
                .body(json)
                .when()
                .post(ORDER_URI)
                .then();
    }
    @Step("Get order with Authorization")
    public ValidatableResponse getOrderList(String accessToken){
        return given()
                .spec(getBaseReqSpec())
                .header("Authorization", accessToken)
                .when()
                .get(ORDER_URI)
                .then();
    }
    @Step("Get order without Authorization")
    public ValidatableResponse getOrderListWithoutAuth(){
        return given()
                .spec(getBaseReqSpec())
                .when()
                .get(ORDER_URI)
                .then();
    }
}
