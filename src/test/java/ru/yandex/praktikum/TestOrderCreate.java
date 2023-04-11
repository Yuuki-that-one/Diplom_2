package ru.yandex.praktikum;

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

import ru.yandex.praktikum.client.IngredientsClient;
import ru.yandex.praktikum.client.OrderClient;
import ru.yandex.praktikum.client.UserClient;
import ru.yandex.praktikum.model.*;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

public class TestOrderCreate {
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
    @DisplayName("Создание заказа с 2 ингридиентами и авторизацией")
    public void canCreateOrderWithIngredientsAndAuth() {
       User user = UserGenerator.getRandom();
        ValidatableResponse createResponse = userClient.createUser(user);
        createResponse
                .assertThat()
                .body("success", is(true));
        userAccessToken = createResponse.extract().path("accessToken");
        String randomIngredientHash1 = ingredientsClient.getRandomIngredientHash();
        String randomIngredientHash2 = ingredientsClient.getRandomIngredientHash();

        String json = "{\n" +
                "    \"ingredients\": [\"" + randomIngredientHash1 + "\", \"" + randomIngredientHash2 + "\"]\n" +
                "}";

        orderClient.createOrder(userAccessToken, json)
                .assertThat()
                .statusCode(SC_OK)
                .and()
                .assertThat()
                .body("success", is(true))
                .and()
                .assertThat()
                .body("name", is(notNullValue()))
                .and()
                .assertThat()
                .body("order.ingredients[0]._id", is(randomIngredientHash1))
                .and()
                .assertThat()
                .body("order.ingredients[0].type", is(notNullValue()))
                .and()
                .assertThat()
                .body("order.ingredients[0].proteins", is(notNullValue()))
                .and()
                .assertThat()
                .body("order.ingredients[0].fat", is(notNullValue()))
                .and()
                .assertThat()
                .body("order.ingredients[0].carbohydrates", is(notNullValue()))
                .and()
                .assertThat()
                .body("order.ingredients[0].calories", is(notNullValue()))
                .and()
                .assertThat()
                .body("order.ingredients[0].price", is(notNullValue()))
                .and()
                .assertThat()
                .body("order.ingredients[0].image", is(notNullValue()))
                .and()
                .assertThat()
                .body("order.ingredients[0].image_mobile", is(notNullValue()))
                .and()
                .assertThat()
                .body("order.ingredients[0].image_large", is(notNullValue()))
                .and()
                .assertThat()
                .body("order.ingredients[0].__v", is(notNullValue()))
                .and()
                .assertThat()
                .body("order.ingredients[1]._id", is(randomIngredientHash2))
                .and()
                .assertThat()
                .body("order.number", is(notNullValue()))
                .and()
                .assertThat()
                .body("order._id", is(notNullValue()))
                .and()
                .assertThat()
                .body("order.owner.name", is(notNullValue()))
                .and()
                .assertThat()
                .body("order.owner.email", is(user.getEmail().toLowerCase()))
                .and()
                .assertThat()
                .body("order.owner.createdAt", is(notNullValue()))
                .and()
                .assertThat()
                .body("order.owner.updatedAt", is(notNullValue()))
                .and()
                .assertThat()
                .body("order.status", is(notNullValue()))
                .and()
                .assertThat()
                .body("order.name", is(notNullValue()))
                .and()
                .assertThat()
                .body("order.createdAt", is(notNullValue()))
                .and()
                .assertThat()
                .body("order.updatedAt", is(notNullValue()))
                .and()
                .assertThat()
                .body("order.number", is(notNullValue()))
                .and()
                .assertThat()
                .body("order.price", is(notNullValue()));


    }
    @Test
    @DisplayName("Заказ не создан без ингридиентов и с авторизацией")
    public void canNotCreateOrderWithoutIngredientsAndWithAuth() {

        User user = UserGenerator.getRandom();
        ValidatableResponse createResponse = userClient.createUser(user);
        createResponse
                .assertThat()
                .body("success", is(true));
        userAccessToken = createResponse.extract().path("accessToken");

        String json = "{\n" +
                "    \"ingredients\": []\n" +
                "}";

        orderClient.createOrder(userAccessToken, json)
                .assertThat()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .assertThat()
                .body("success", is(false))
                .and()
                .assertThat()
                .body("message", is("Ingredient ids must be provided"));
    }
    @Test
    @DisplayName("Заказ не создан без ингридиентов и без авторизации")
    public void canNotCreateOrderWithoutIngredientsAndWithoutAuth() {

        String json = "{\n" +
                "    \"ingredients\": []\n" +
                "}";

        orderClient.createOrderWithoutAuth(json)
                .assertThat()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .assertThat()
                .body("success", is(false))
                .and()
                .assertThat()
                .body("message", is("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Заказ создан с 2 ингридиентами, без авторизации")
    public void canCreateOrderWithIngredientsWithoutAuth() {
        String json = "{\n" +
                "    \"ingredients\": [\"" + ingredientsClient.getRandomIngredientHash() + "\", \"" + ingredientsClient.getRandomIngredientHash() + "\"]\n" +
                "}";

        orderClient.createOrderWithoutAuth(json)
                .assertThat()
                .statusCode(SC_OK)
                .and()
                .assertThat()
                .body("success", is(true))
                .and()
                .assertThat()
                .body("order.number", is(notNullValue()));
    }
    @Test
    @DisplayName("(Падение ожидаемо)Заказ не создан c двумя ингридиентами, один из них с неверным хэшем (без авторизации)")
    public void canNotCreateOrderWithWrongAndRightHashIngredients() {
        //Этот тест ожидаемо падает. Если проверять вариант заказа верный хэш + неверный хэш, то заказ успешно создается, хотя не должен, как следует из текста ожидаемой ошибки.

        String randomIngredientId = RandomStringUtils.randomNumeric(24);

        String json = "{\n" +
                "    \"ingredients\": [\"" + randomIngredientId + "\", \"" +  ingredientsClient.getRandomIngredientHash() + "\"]\n" +
                "}";

        orderClient.createOrderWithoutAuth(json)
                .assertThat()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .assertThat()
                .body("success", is(false))
                .and()
                .assertThat()
                .body("message", is("One or more ids provided are incorrect"));
    }
    @Test
    @DisplayName("Заказ не создан c одним ингридиентом с неверным хэшем (с авторизацией)")
    public void canNotCreateOrderWithWrongHashIngredient() {
        User user = UserGenerator.getRandom();
        ValidatableResponse createResponse = userClient.createUser(user);
        createResponse
                .assertThat()
                .body("success", is(true));
        userAccessToken = createResponse.extract().path("accessToken");

        String randomIngredientId = RandomStringUtils.randomNumeric(24);

        String json = "{\n" +
                "    \"ingredients\": [\"" + randomIngredientId + "\"]\n" +
                "}";

        orderClient.createOrder(userAccessToken,json)
                .assertThat()
                .statusCode(SC_BAD_REQUEST)
                .and()
                .assertThat()
                .body("success", is(false))
                .and()
                .assertThat()
                .body("message", is("One or more ids provided are incorrect"));
    }
}
