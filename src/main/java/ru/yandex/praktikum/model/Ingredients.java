package ru.yandex.praktikum.model;

import io.qameta.allure.Step;
import ru.yandex.praktikum.client.IngredientsClient;

import java.util.List;

public class Ingredients {
    private String success;
    private List<Data> data;

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    @Step("Get Random Ingredient")
    public String getRandomIngredientHash(){
        Ingredients response = IngredientsClient.getIngredients();  //Получаем актуальные хеши ингридиентов
        List<Data> CurrentData = response.getData();
        int randomIngredient = (int) (Math.random()*CurrentData.size());
        return List.of(CurrentData.get(randomIngredient).get_id()).toString().replace("[", "").replace("]", "");
    }
}
