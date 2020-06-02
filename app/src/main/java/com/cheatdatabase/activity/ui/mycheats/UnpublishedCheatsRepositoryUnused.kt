package com.cheatdatabase.activity.ui.mycheats

import com.cheatdatabase.data.model.UnpublishedCheat
import com.cheatdatabase.rest.KotlinRestApi
import com.cheatdatabase.rest.SafeApiRequest
import retrofit2.Response

class UnpublishedCheatsRepositoryUnused(val api: KotlinRestApi) : SafeApiRequest() {

//    suspend fun getMyUnpublishedCheats(memberId: Int, pw: String) =
//        apiRequest(memberId, pw) {
//            api.getMyUnpublishedCheatsSuspended(memberId, pw)
//        }


    suspend fun getTopMembers() =
        apiRequest { api.getTopMembers() }


    // TODO hier weitermachen: https://www.youtube.com/watch?v=480A5KZlRdM&list=PLk7v1Z2rk4hjtIT9TCKIcl2YJYfDlZ_4v&index=2
    // TODO hier weitermachen: https://www.youtube.com/watch?v=480A5KZlRdM&list=PLk7v1Z2rk4hjtIT9TCKIcl2YJYfDlZ_4v&index=2
    // TODO hier weitermachen: https://www.youtube.com/watch?v=480A5KZlRdM&list=PLk7v1Z2rk4hjtIT9TCKIcl2YJYfDlZ_4v&index=2
    // TODO hier weitermachen: https://www.youtube.com/watch?v=480A5KZlRdM&list=PLk7v1Z2rk4hjtIT9TCKIcl2YJYfDlZ_4v&index=2
    // TODO hier weitermachen: https://www.youtube.com/watch?v=480A5KZlRdM&list=PLk7v1Z2rk4hjtIT9TCKIcl2YJYfDlZ_4v&index=2


    suspend fun getMyUnpublishedCheats(
        memberId: Int,
        passwordMd5: String
    ): Response<List<UnpublishedCheat>> {
        return KotlinRestApi().getMyUnpublishedCheats(memberId, passwordMd5)
    }

}