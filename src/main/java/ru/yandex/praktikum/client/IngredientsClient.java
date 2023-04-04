package ru.yandex.praktikum.client;

import io.qameta.allure.Step;
import ru.yandex.praktikum.model.Data;
import ru.yandex.praktikum.model.Ingredients;


import java.util.List;

import static io.restassured.RestAssured.given;

public class IngredientsClient extends StellarRestClient {
    private static final String INGREDIENTS_URI = BASE_URI + "api/ingredients";

    @Step("Get Ingredients")
    public static Ingredients getIngredients(){
        return given()
                .get(INGREDIENTS_URI)
                .body().as(Ingredients.class);
    }
    @Step("Get Random Ingredient Hash")
    public String getRandomIngredientHash(){
        Ingredients response = IngredientsClient.getIngredients();  //Получаем актуальные хеши ингридиентов
        List<Data> CurrentData = response.getData();
        int randomIngredient = (int) (Math.random()*CurrentData.size());
        return List.of(CurrentData.get(randomIngredient).get_id()).toString().replace("[", "").replace("]", "");
    }
}
