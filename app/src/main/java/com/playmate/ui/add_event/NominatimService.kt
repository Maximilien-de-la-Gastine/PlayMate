package com.playmate.ui.add_event

import org.osmdroid.util.GeoPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface NominatimService {
    @GET("search")
    fun searchPlace(
        @Query("q") query: String,
        @Query("format") format: String = "json"
    ): Call<NominatimResponse>

    @GET("search") // Modifier le chemin en fonction de votre API
    fun getSuggestions(
        @Query("query") query: String
    ): Call<List<String>>
}

fun performSearch(query: String, fragment: AddEventFragment) {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://nominatim.openstreetmap.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service = retrofit.create(NominatimService::class.java)
    val call = service.searchPlace(query)

    call.enqueue(object : Callback<NominatimResponse> {
        override fun onResponse(
            call: Call<NominatimResponse>,
            response: Response<NominatimResponse>
        ) {
            if (response.isSuccessful) {
                val places = response.body()
                if (!places.isNullOrEmpty()) {
                    // Récupérez les coordonnées du premier endroit trouvé
                    val place = places[0]
                    val latitude = place.lat.toDouble()
                    val longitude = place.lon.toDouble()

                    // Créez un objet GeoPoint avec les coordonnées
                    val geoPoint = GeoPoint(latitude, longitude)

                    // Ouvrir le formulaire avec les coordonnées du lieu sélectionné
                    fragment.showAddEventForm(geoPoint)
                } else {
                    // Gérer le cas où aucun résultat n'a été trouvé
                }
            } else {
                // Gérer les erreurs de réponse ici
            }
        }

        override fun onFailure(call: Call<NominatimResponse>, t: Throwable) {
            // Gérer les échecs de requête ici
        }
    })
}
