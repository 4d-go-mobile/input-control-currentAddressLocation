package ___PACKAGE___

import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.qmobile.qmobiledatasync.utils.BaseInputControl
import com.qmobile.qmobiledatasync.utils.InputControl
import com.qmobile.qmobileui.ui.SnackbarHelper
import com.qmobile.qmobileui.utils.PermissionChecker
import java.util.*

@InputControl
class CurrentLocationAddress(private val view: View) : BaseInputControl {

    override val autocomplete: Boolean = true

    private val rationaleString = "Permission required to access current location address"

    private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(view.context as FragmentActivity)

    private lateinit var outputCallback: (outputText: String) -> Unit

    @Suppress("MissingPermission")
    private fun getLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val address = getAddress(location)
                outputCallback(address)
            } else {
                SnackbarHelper.show(view.context as FragmentActivity, "Could not get current location")
                outputCallback("")
            }
        }
    }

    private fun getAddress(location: Location): String {
        val geocoder = Geocoder(view.context, Locale.getDefault())
        val addresses: List<Address>? = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        if (!addresses.isNullOrEmpty()) {
            return addresses.first().getAddressLine(0)
        } else {
            SnackbarHelper.show(view.context as FragmentActivity, "Could not get current address")
        }
        return ""
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
        (view.context as PermissionChecker?)?.askPermission(
            permission = permission,
            rationale = rationaleString
        ) { isGranted ->
            if (isGranted) {
                canGoOn()
            }
        }
    }
}