package com.souschef.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Reactive view of the device's network connectivity. Emits [NetworkStatus.Available]
 * when at least one network is validated and exposes Internet capability;
 * otherwise [NetworkStatus.Unavailable].
 *
 * Registered as a process-scoped Koin `single`.
 */
enum class NetworkStatus { Available, Unavailable }

class ConnectivityObserver(context: Context) {

    private val cm = context.applicationContext.getSystemService(ConnectivityManager::class.java)

    private val _networkStatus = MutableStateFlow(currentStatus())
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus

    init {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        cm?.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _networkStatus.value = NetworkStatus.Available
            }

            override fun onLost(network: Network) {
                _networkStatus.value = currentStatus()
            }

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                _networkStatus.value = if (capabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_INTERNET
                    )
                ) NetworkStatus.Available else NetworkStatus.Unavailable
            }
        })
    }

    private fun currentStatus(): NetworkStatus {
        val active = cm?.activeNetwork ?: return NetworkStatus.Unavailable
        val caps = cm.getNetworkCapabilities(active) ?: return NetworkStatus.Unavailable
        return if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
            NetworkStatus.Available else NetworkStatus.Unavailable
    }
}
