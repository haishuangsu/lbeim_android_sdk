import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ExoPlayerController(context: Context, url: String) {
    val player: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        setMediaItem(MediaItem.fromUri(url))
        prepare()
    }

    var isPlaying by mutableStateOf(false)
        private set

    var currentSecond by mutableStateOf(0)
        private set

    var durationInSeconds by mutableStateOf(0)
        private set

    var bufferedPositionInSeconds by mutableStateOf(0)
        private set

    var playbackSpeed by mutableStateOf(1.0f)
        private set

    private var progressJob: Job? = null

    init {
        startProgressUpdater()
    }

    private fun startProgressUpdater() {
        progressJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                if (isPlaying) {
                    currentSecond = (player.currentPosition / 1000).toInt()
                    durationInSeconds = (player.duration / 1000).toInt()
                    bufferedPositionInSeconds = (player.bufferedPosition / 1000).toInt()
                }
                delay(500)
            }
        }
    }

    fun togglePlayPause() {
        if (isPlaying) {
            player.pause()
        } else {
            player.play()
        }
        isPlaying = !isPlaying
    }

    fun seekTo(position: Int) {
        player.seekTo((position * 1000).toLong())
    }

    fun changePlaybackSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
        playbackSpeed = speed
    }

    fun release() {
        progressJob?.cancel()
        player.release()
    }
}
