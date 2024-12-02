package info.hermiths.lbesdk.ui.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext

import coil3.request.ImageRequest


@Composable
fun NormalDecryptedOrNotImageView(
    key: String, url: String,
    modifier: Modifier,
    imageLoader: ImageLoader
) {
    val ctx = LocalPlatformContext.current
    AsyncImage(
        model = ImageRequest.Builder(ctx).data(url)
            .decoderFactory(
                DecryptedDecoder.Factory(url = url, key = key)
            )
            .build(),
        contentDescription = "Yo",
        contentScale = ContentScale.FillWidth,
        modifier = modifier,
        imageLoader = imageLoader,
    )

//    Image(
//        painter = rememberAsyncImagePainter(
//            model = ImageRequest.Builder(ctx)
//                .data(url).decoderFactory(
//                    DecryptedDecoder.Factory(
//                        url = url,
//                        key = key
//                    )
//                ).build(),
//        ),
//        contentDescription = "Yo",
//        contentScale = ContentScale.Fit,
//        modifier = modifier,
//    )
}