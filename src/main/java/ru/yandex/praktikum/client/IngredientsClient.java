package ru.yandex.praktikum.client;

import io.qameta.allure.Step;
import ru.yandex.praktikum.model.Ingredients;


import static io.restassured.RestAssured.given;

public class IngredientsClient extends StellarRestClient {
    private static final String INGREDIENTS_URI = BASE_URI + "api/ingredients";

    @Step("Get Ingredients")
    public static Ingredients getIngredients(){
        return given()
                .get(INGREDIENTS_URI)
                .body().as(Ingredients.class);
    }

}
