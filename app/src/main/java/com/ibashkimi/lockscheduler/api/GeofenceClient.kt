package com.ibashkimi.lockscheduler.api

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.ibashkimi.lockscheduler.service.TransitionsIntentService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class GeofenceClient(val context: Context) {

    private val geofencingClient = LocationServices.getGeofencingClient(context)

    suspend fun addGeofences(geofenceList: List<Geofence>): Boolean =
        addGeofencesAsync(geofenceList).await()

    fun addGeofencesFlow(geofenceList: List<Geofence>): Flow<Boolean> = flow {
        emit(addGeofencesAsync(geofenceList).await())
    }.catch { emit(false) }

    suspend fun removeGeofence(geofenceId: String) = removeGeofences(listOf(geofenceId))

    suspend fun removeGeofences(geofenceIds: List<String>) =
        removeGeofencesAsync(geofenceIds).await()

    fun removeGeofencesFlow(geofenceIds: List<String>): Flow<Unit> = flow {
        emit(removeGeofencesAsync(geofenceIds).await())
    }

    private fun addGeofencesAsync(geofenceList: List<Geofence>): Deferred<Boolean> {
        val result = CompletableDeferred<Boolean>()
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("GeofenceClient", "location permission is needed")
            throw IllegalStateException("Location permission is needed")
        }
        geofencingClient.addGeofences(getGeofencingRequest(geofenceList), geofencePendingIntent)
            .run {
                addOnSuccessListener {
                    Log.d("GeofenceClient", "Geofences added")
                    result.complete(true)
                }
                addOnFailureListener {
                    Log.d("GeofenceClient", "Failed to add geofences")
                    result.completeExceptionally(it)
                }
            }
        return result
    }

    private fun removeGeofencesAsync(geofenceIds: List<String>): Deferred<Unit> {
        val result = CompletableDeferred<Unit>()
        geofencingClient.removeGeofences(geofenceIds).run {
            addOnSuccessListener {
                // Geofences removed
                Log.d("GeofenceClient", "Geofences removed")
                result.complete(Unit)
            }
            addOnFailureListener {
                // Failed to remove geofences
                Log.d("GeofenceClient", "Geofences remove failed")
                result.completeExceptionally(it)
            }
        }
        return result
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, TransitionsIntentService::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun getGeofencingRequest(geofenceList: List<Geofence>): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER or GeofencingRequest.INITIAL_TRIGGER_EXIT)
            addGeofences(geofenceList)
        }.build()
    }
}