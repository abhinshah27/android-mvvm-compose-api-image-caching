package com.example.images.loader.di.module

import android.content.Context
import com.example.images.loader.BuildConfig
import com.example.images.loader.data.repository.ImageRepository
import com.example.images.loader.data.source.ApiServices
import com.example.images.loader.utils.Constants
import com.example.images.loader.utils.Constants.BASE_URL
import com.example.images.loader.utils.NetworkUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Dagger module for providing dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides a singleton instance of Retrofit.
     *
     * @param okHttpClient The OkHttpClient instance.
     * @return A Retrofit instance.
     */
    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder().client(okHttpClient).baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).build()
    }

    /**
     * Provides a singleton instance of OkHttpClient with caching and logging interceptor.
     *
     * @param loggingInterceptor The HttpLoggingInterceptor instance.
     * @param cache The Cache instance.
     * @return An OkHttpClient instance.
     */
    @Singleton
    @Provides
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor, cache: Cache,@ApplicationContext context: Context): OkHttpClient {
        return OkHttpClient().newBuilder()
            .callTimeout(Constants.CALL_TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .cache(cache) // Add cache
            .addInterceptor { chain ->
                var request = chain.request()
                request = if (NetworkUtils.isInternetAvailable(context)) {
                    // If there is internet, don't read from cache
                    request.newBuilder().header("Cache-Control", "public, max-age=0").build()
                } else {
                    // If no internet, read from cache for 7 days
                    request.newBuilder().header(
                        "Cache-Control",
                        "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7 // 7 days
                    ).build()
                }
                chain.proceed(request)
            }
            .build()
    }

    /**
     * Provides a singleton instance of HttpLoggingInterceptor.
     *
     * @return A HttpLoggingInterceptor instance.
     */
    @Singleton
    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level =
                if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
    }

    /**
     * Provides a singleton instance of ApiServices.
     *
     * @param retrofit The Retrofit instance.
     * @return An ApiServices instance.
     */
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiServices {
        return retrofit.create(ApiServices::class.java)
    }

    /**
     * Provides a singleton instance of ImageRepository.
     *
     * @param apiService The ApiServices instance.
     * @param context The application context.
     * @return An ImageRepository instance.*/

    @Provides
    @Singleton
    fun provideRepository(
        apiService: ApiServices, @ApplicationContext context: Context
    ): ImageRepository {
        return ImageRepository(context = context, apiService = apiService)
    }

    /**
     * Provides a singleton instance of Cache.
     *
     * @param context The application context.
     * @return A Cache instance.
     */
    @Singleton
    @Provides
    fun provideCache(@ApplicationContext context: Context): Cache {
        val cacheSize = 10 * 1024 * 1024 // 10 MB
        return Cache(context.cacheDir, cacheSize.toLong())
    }

}