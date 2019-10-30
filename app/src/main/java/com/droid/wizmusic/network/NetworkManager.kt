package com.droid.wizmusic


import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

interface NetworkManager {

    @GET
    fun getAllTracksAsync(
        @Url url: String
    ): Deferred<Response<List<Track>>>


    companion object HTTPService {
        private var create: NetworkManager? = null
        private var retrofit: Retrofit? = null

        fun getInstance(): NetworkManager? {
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl("http://starlord.hackerearth.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(CoroutineCallAdapterFactory())
                    .client(getHTTPClient())
                    .build()
                create = retrofit?.create(
                    NetworkManager::class.java
                )
            }
            return create
        }

        private fun getHTTPClient(): OkHttpClient {
            val builder = OkHttpClient.Builder()

            builder.connectTimeout(30, TimeUnit.SECONDS)
            builder.readTimeout(30, TimeUnit.SECONDS)
            builder.writeTimeout(30, TimeUnit.SECONDS)

            if (BuildConfig.DEBUG)
                builder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))

            return builder.build()
        }
    }
}