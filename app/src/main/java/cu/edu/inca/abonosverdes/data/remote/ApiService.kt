package cu.edu.inca.abonosverdes.data.remote

import cu.edu.inca.abonosverdes.data.remote.models.DbUpdateResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("db-updates")
    suspend fun getDbUpdates(
        @Query("current_version") currentVersion: Int,
        @Query("platform") platform: String = "android"
    ): DbUpdateResponse

    companion object {
        const val BASE_URL = "https://av-cerai.inca.edu.cu/apiv2/"
    }
}
