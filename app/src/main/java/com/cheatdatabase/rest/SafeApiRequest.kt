package com.cheatdatabase.rest

import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException

abstract class SafeApiRequest {

    suspend fun <T : Any> apiRequest(call: suspend () -> Response<T>): T {
        val response = call.invoke()
        if (response.isSuccessful) {
            return response.body()!!
        } else {
            val error = response.errorBody()?.string()

            val message = StringBuilder()

            error.let {
                try {
                    message.append(JSONObject(it).getString("message"))
                } catch (e: JSONException) {
                }

                message.append("\n")
            }

            message.append("Cheat-Database API Error")
            throw  ApiException(message.toString())
        }
    }

//    suspend fun <T : Any> apiRequestWithParameters(memberId: Int, pw: String, call: suspend () -> Response<T>): T {
//        val response = call.invoke()
//        if (response.isSuccessful) {
//            return response.body()!!
//        } else {
//            val error = response.errorBody()?.string()
//
//            val message = StringBuilder()
//
//            error.let {
//                try {
//                    message.append(JSONObject(it).getString("message"))
//                } catch (e: JSONException) {
//
//                }
//                message.append("\n")
//            }
//
//            message.append("Cheat-Database API Error")
//            throw  ApiException(message.toString())
//        }
//    }

}

class ApiException(message: String) : IOException(message)