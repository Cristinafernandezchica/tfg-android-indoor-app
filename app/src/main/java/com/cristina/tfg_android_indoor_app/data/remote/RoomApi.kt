import com.cristina.tfg_android_indoor_app.data.model.OccupancyResponse
import com.cristina.tfg_android_indoor_app.data.model.TrainingRequest
import com.cristina.tfg_android_indoor_app.data.model.dto.RoomDto
import com.cristina.tfg_android_indoor_app.data.model.dto.ZoneDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RoomApi {

    @GET("rooms")
    suspend fun getRooms(): Response<List<RoomDto>>

    @GET("rooms/{roomId}/zones")
    suspend fun getZones(@Path("roomId") roomId: String): Response<List<ZoneDto>>

    @POST("sensors/training_data")
    suspend fun sendTrainingData(@Body body: TrainingRequest): Response<Unit>

    @GET("rooms/visits/current")
    suspend fun getVisitsCurrent(): Response<Map<String, Int>>

    @GET("rooms/visits/at")
    suspend fun getVisitsAt(
        @Query("room_id") roomId: String,
        @Query("at") timestamp: String
    ): Response<Map<String, Int>>

    @GET("rooms/occupancy")
    suspend fun getOccupancy(): Response<Map<String, Int>>

    @GET("rooms/occupancy/at")
    suspend fun getOccupancyAt(
        @Query("room_id") roomId: String,
        @Query("at") timestamp: String
    ): Response<OccupancyResponse>

}
