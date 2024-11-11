package info.hermiths.chatapp.ui.presentation.components

import android.content.Context
import androidx.collection.LruCache
import coil3.Image
import coil3.decode.DataSource
import coil3.intercept.Interceptor
import coil3.request.ImageResult
import coil3.request.SuccessResult

//class CustomCacheInterceptor(
//    private val context: Context,
//    private val cache: LruCache<String, Image>,
//) : Interceptor {
//
//    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
//        val value = cache[chain.request.data.toString()]
//        if (value != null) {
//            return SuccessResult(
//                image = value.bitmap.toImage(),
//                request = chain.request,
//                dataSource = DataSource.MEMORY_CACHE,
//            )
//        }
//        return chain.proceed()
//    }
//}