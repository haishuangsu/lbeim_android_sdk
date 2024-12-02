package info.hermiths.lbesdk.data.remote

import info.hermiths.lbesdk.service.RetrofitInstance
import info.hermiths.lbesdk.model.resp.Config
import info.hermiths.lbesdk.model.req.ConfigBody

object LbeConfigRepository {
    private val lbeIMRepository = RetrofitInstance.baseApiService;

    suspend fun fetchConfig(lbeSign: String, lbeIdentity: String, body: ConfigBody): Config {
        return lbeIMRepository.fetchConfig(
            lbeSign = lbeSign, lbeIdentity = lbeIdentity, body = body
        )
    }
}