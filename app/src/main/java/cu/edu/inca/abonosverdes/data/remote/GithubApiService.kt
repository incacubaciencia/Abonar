package cu.edu.inca.abonosverdes.data.remote

import cu.edu.inca.abonosverdes.data.remote.models.GithubRelease
import retrofit2.http.GET

interface GithubApiService {

    @GET("repos/incacubaciencia/Abonar/releases/latest")
    suspend fun getLatestRelease(): GithubRelease

    companion object {
        const val BASE_URL = "https://api.github.com/"
    }
}
