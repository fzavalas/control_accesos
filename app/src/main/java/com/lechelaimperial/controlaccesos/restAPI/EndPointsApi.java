package com.lechelaimperial.controlaccesos.restAPI;

import com.lechelaimperial.controlaccesos.pojo.Post;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface EndPointsApi {
    @FormUrlEncoded
    @POST(ConstantesRestApi.URL_REGISTRAR_ACCESO+"{codigo}")
    Call<Post> registrarAcceso(@Path("codigo") String codigo, @Field("token") String token);

    @POST("checador/checador.php")
    Call<Post> registrarAcceso(@Body Post codigo);

}
