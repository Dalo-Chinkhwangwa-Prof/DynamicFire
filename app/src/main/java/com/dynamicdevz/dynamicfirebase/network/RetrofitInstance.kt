package com.dynamicdevz.dynamicfirebase.network

import com.dynamicdevz.dynamicfirebase.model.AddressResponse
import com.dynamicdevz.dynamicfirebase.util.Constant.Companion.API_KEY
import com.dynamicdevz.dynamicfirebase.util.Constant.Companion.BASE_URL
import com.dynamicdevz.dynamicfirebase.util.Constant.Companion.END_POINT
import com.dynamicdevz.dynamicfirebase.util.Constant.Companion.KEY
import com.dynamicdevz.dynamicfirebase.util.Constant.Companion.LAT_LNG
import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class RetrofitInstance {

    private val mapService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(MapService::class.java)

    fun getLocationAddress(latLng: String) = mapService.geocodeLocation(API_KEY, latLng)

    interface MapService {

        @GET(END_POINT)
        fun geocodeLocation(@Query(KEY) apiKey: String,
        @Query(LAT_LNG) latLng: String): Single<AddressResponse>

    }
}