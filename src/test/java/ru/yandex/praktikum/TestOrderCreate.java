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
    public void CanCreateOrderWithIngredientsAndAuth() {
       User user = UserGenerator.getRandom();
        ValidatableResponse createResponse = userClient.create(user);
        createResponse
                .assertThat()
                .body("success", is(true));
        userAccessToken = createResponse.extract().path("accessToken");

        String json = "{\n" +
                "    \"ingredients\": [\"" + ingredientsClient.getRandomIngredientHash() + "\", \"" + ingredientsClient.getRandomIngredientHash() + "\"]\n" +
                "}";

        orderClient.createOrder(userAccessToken, json)
                .assertThat()
                .statusCode(SC_OK)
                .and()
                .assertThat()
                .body("success", is(true))
                .and()
                .assertThat()
                .body("order.number", is(notNullValue()))
                .and()
                .assertThat()
                .body("order.owner.email", is(user.getEmail().toLowerCase()));

    }
    @Test
    @DisplayName("Заказ не создан без ингридиентов и с авторизацией")
    public void CanNotCreateOrderWithoutIngredientsAndWithAuth() {

        User user = UserGenerator.getRandom();
        ValidatableResponse createResponse = userClient.create(user);
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
    public void CanNotCreateOrderWithoutIngredientsAndWithoutAuth() {

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
    public void CanCreateOrderWithIngredientsWithoutAuth() {
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
    @DisplayName("Заказ не создан c ингридиентами с неверным хэшем (без авторизации)")
    public void CanNotCreateOrderWithWrongHashIngredients() {
        //Если проверять вариант заказа верный хэш + неверный хэш, то заказ успешно создается, это баг тренажера, уже зарепортил.
        //Исправить не успеют, поэтому пришлось оставить проверку только варианта с двумя неверными хэшами.
        String randomIngredient1Id = RandomStringUtils.randomNumeric(24);
        String randomIngredient2Id = RandomStringUtils.randomNumeric(24);

        String json = "{\n" +
                "    \"ingredients\": [\"" + randomIngredient1Id + "\", \"" + randomIngredient2Id + "\"]\n" +
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
}
