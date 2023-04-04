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
import ru.yandex.praktikum.client.IngredientsClient;
import ru.yandex.praktikum.client.OrderClient;
import ru.yandex.praktikum.client.UserClient;
import ru.yandex.praktikum.model.User;
import ru.yandex.praktikum.model.UserGenerator;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

public class TestOrderReceive {
    private UserClient userClient;
    private OrderClient orderClient;
    private String userAccessToken;
    private IngredientsClient ingredientsClient;


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
        orderClient = new OrderClient();
        ingredientsClient = new IngredientsClient();
    }
    @After
    public void cleanUp(){
        if (userAccessToken != null){
            userClient.delete(userAccessToken);
        }
    }
    @Test
    @DisplayName("Получение списка заказов с авторизацией")
    public void CanReceiveOrderListWithAuth() {
        User user = UserGenerator.getRandom();
        ValidatableResponse createResponse = userClient.create(user);
        createResponse
                .assertThat()
                .body("success", is(true));
        userAccessToken = createResponse.extract().path("accessToken");
        String testIngredientHash = ingredientsClient.getRandomIngredientHash();

        String json = "{\n" +
                "    \"ingredients\": [\"" + testIngredientHash + "\"]\n" +
                "}";

        orderClient.createOrder(userAccessToken, json) //создаем первый заказ
                .assertThat()
                .body("success", is(true));
        orderClient.createOrder(userAccessToken, json) //создаем второй заказ
                .assertThat()
                .body("success", is(true));
        orderClient.getOrderList(userAccessToken)
                .assertThat()
                .statusCode(SC_OK)
                .and()
                .assertThat()
                .body("success", is(true))
                .and()
                .assertThat()
                .body("orders.size()", is(2))  //проверяем, что в списке заказов ровно два элемента
                .and()
                .assertThat()
                .body("orders[0]._id", is(notNullValue()))
                .and()
                .assertThat()
                .body("orders[0].ingredients[0]", is(testIngredientHash))
                .and()
                .assertThat()
                .body("orders[0].status", is(notNullValue()))
                .and()
                .assertThat()
                .body("orders[0].name", is(notNullValue()))
                .and()
                .assertThat()
                .body("orders[0].createdAt", is(notNullValue()))
                .and()
                .assertThat()
                .body("orders[0].updatedAt", is(notNullValue()))
                .and()
                .assertThat()
                .body("orders[0].number", is(notNullValue()))
                .and()
                .assertThat()
                .body("total", is(notNullValue()))
                .and()
                .assertThat()
                .body("totalToday", is(notNullValue()));
    }
    @Test
    @DisplayName("Ошибка получения списка заказов без авторизации")
    public void CanReceiveOrderListWithoutAuth() {

        String json = "{\n" +
                "    \"ingredients\": [\"" + ingredientsClient.getRandomIngredientHash() + "\"]\n" +
                "}";

        orderClient.createOrderWithoutAuth(json) //создаем первый заказ
                .assertThat()
                .body("success", is(true));
        orderClient.getOrderListWithoutAuth()
                .assertThat()
                .statusCode(SC_UNAUTHORIZED)
                .and()
                .assertThat()
                .body("success", is(false))
                .and()
                .assertThat()
                .body("message", is("You should be authorised"));
    }
    @Test
    @DisplayName("(Падение ожидаемо)Ограничение на 50 элементов списка заказов, с авторизацией")
    public void CanReceiveOrderList50Limit() {
        //Этот тест ожидаемо падает, ограничение на 50 заказов не работает
        User user = UserGenerator.getRandom();
        ValidatableResponse createResponse = userClient.create(user);
        createResponse
                .assertThat()
                .body("success", is(true));
        userAccessToken = createResponse.extract().path("accessToken");

        String json = "{\n" +
                "    \"ingredients\": [\"" + ingredientsClient.getRandomIngredientHash() + "\"]\n" +
                "}";
        for (int i = 0; i < 60; i++) {
            orderClient.createOrder(userAccessToken, json); //создаем 60 заказов
        }

        orderClient.getOrderList(userAccessToken)
                .assertThat()
                .statusCode(SC_OK)
                .and()
                .assertThat()
                .body("orders.size()", is(50));  //проверяем, что в списке заказов только 50 элементов (тест падает, фактически возвращается 60)
    }
}
