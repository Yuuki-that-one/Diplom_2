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
import ru.yandex.praktikum.model.*;

import java.util.Arrays;
import java.util.List;

import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.is;

public class TestOrderCreate {
    private UserClient userClient;
    private OrderClient orderClient;
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
        orderClient = new OrderClient();
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

        Ingredients response = IngredientsClient.getIngredients();
        List<Data> CurrentData = response.getData();
        int randomIngredient1 = (int) (Math.random()*CurrentData.size());
        int randomIngredient2 = (int) (Math.random()*CurrentData.size());
        String randomIngredient1Id = Arrays.asList(CurrentData.get(randomIngredient1).get_id()).toString().replace("[", "").replace("]", "");
        String randomIngredient2Id = Arrays.asList(CurrentData.get(randomIngredient2).get_id()).toString().replace("[", "").replace("]", "");
        User user = UserGenerator.getRandom();
        ValidatableResponse createResponse = userClient.create(user);
        createResponse
                .assertThat()
                .body("success", is(true));
        userAccessToken = createResponse.extract().path("accessToken");

        String json = "{\n" +
                "    \"ingredients\": [\"" + randomIngredient1Id + "\", \"" + randomIngredient2Id + "\"]\n" +
                "}";

        // System.out.println(Arrays.asList(CurrentData.get(randomIngredient1).getName()));
        // System.out.println(Arrays.asList(CurrentData.get(randomIngredient2).getName()));
        orderClient.createOrder(userAccessToken, json)
                .assertThat()
                .statusCode(SC_OK);
    }
}
