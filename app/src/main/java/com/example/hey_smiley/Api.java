package com.example.hey_smiley;

import com.example.hey_smiley.ui.Anek;

import retrofit2.Call;
import retrofit2.http.GET;


public interface Api {

    String BASE_URL = "http://rzhunemogu.ru/";

    @GET("RandJSON.aspx?CType=1")
    Call<Anek> getAnek();

}