package com.cheatdatabase.rest

import retrofit2.Response
import java.io.IOException

abstract class SafeApiRequest {

    suspend fun <T : Any> apiRequest(
        call: suspend () -> Response<T>
    ): T {
        val response = call.invoke()
        if (response.isSuccessful) {
            return response.body()!!
        } else {
            // TODO handle API exception
            throw ApiException(response.code().toString())
        }
    }

    suspend fun <T : Any> apiRequestTwo(
        memberId: Int,
        pw: String,
        call: suspend () -> Response<T>
    ): T {
        val response = call.invoke()
        if (response.isSuccessful) {
            return response.body()!!
        } else {
            // TODO handle API exception
            throw ApiException(response.code().toString())
        }
    }


}

class ApiException(message: String) : IOException(message)