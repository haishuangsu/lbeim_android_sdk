package info.hermiths.chatapp.data.remote

import info.hermiths.chatapp.service.RetrofitInstance
import info.hermiths.chatapp.model.resp.Config
import info.hermiths.chatapp.model.req.ConfigBody

object LbeConfigRepository {
    private val lbeIMRepository = RetrofitInstance.baseApiService;

    suspend fun fetchConfig(lbeSign: String, body: ConfigBody): Config {
        return lbeIMRepository.fetchConfig(lbeSign = lbeSign, body)
    }
}