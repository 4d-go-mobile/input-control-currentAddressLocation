package ___PACKAGE___

import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.qmobile.qmobiledatasync.utils.BaseKotlinInputControl
import com.qmobile.qmobiledatasync.utils.KotlinInputControl
import com.qmobile.qmobileui.ui.SnackbarHelper
import com.qmobile.qmobileui.utils.PermissionChecker
import timber.log.Timber
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

@KotlinInputControl
class CurrentLocationAddress(private val view: View) : BaseKotlinInputControl {

    override val autocomplete: Boolean = true

    private val rationaleString = "Permission required to access current location address"

    private lateinit var outputCallback: (outputText: String) -> Unit

    private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(view.context as FragmentActivity)

    private lateinit var locationCallback: LocationCallback

    @Suppress("MissingPermission")
    private fun getLocation() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, TimeUnit.SECONDS.toMillis(2))
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(3))
            .setMaxUpdateDelayMillis(TimeUnit.SECONDS.toMillis(3))
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.lastLocation?.let {
                    getAddress(it, false)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        fusedLocationClient.lastLocation.addOnSuccessListener { lastKnownLocation: Location? ->
            if (lastKnownLocation != null) {
                getAddress(lastKnownLocation, true)
            }
        }
    }

    private fun getAddress(location: Location, isLastKnownLocation: Boolean) {
        val geocoder = Geocoder(view.context, Locale.getDefault())
        val addresses: List<Address>? = try {
            geocoder.getFromLocation(location.latitude, location.longitude, 1)
        } catch (e: IOException) {
            Timber.e(e.message.orEmpty())
            null
        }
        if (!addresses.isNullOrEmpty()) {
            outputCallback(addresses.first().getAddressLine(0))
        } else {
            SnackbarHelper.show(view.context as FragmentActivity, "Could not get current address")
            outputCallback("")
        }
        if (!isLastKnownLocation) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun process(inputValue: Any?, outputCallback: (output: Any) -> Unit) {
        requestPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) {
            requestPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) {
                this.outputCallback = outputCallback
                getLocation()
            }
        }
    }

    private fun requestPermission(permission: String, canGoOn: () -> Unit) {
        (view.context as? PermissionChecker)?.askPermission(
            permission = permission,
            rationale = rationaleString
        ) { isGranted ->
            if (isGranted) {
                canGoOn()
            }
        }
    }
}