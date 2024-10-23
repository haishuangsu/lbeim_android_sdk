package info.hermiths.chatapp.service.rest.service

import info.hermiths.chatapp.service.rest.RetrofitInstance
import info.hermiths.chatapp.service.rest.model.Config

object LbeIMRepository {
    private val lbeIMRepository = RetrofitInstance.apiService;

    suspend fun fetchConfig(): Config {
        return lbeIMRepository.fetchConfig();
    }
}