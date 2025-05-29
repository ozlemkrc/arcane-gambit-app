package com.example.arcane_gambit.data.repository

import android.content.Context
import android.content.SharedPreferences

/**
 * Repository for managing app settings including server configuration
 */
class SettingsRepository(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()

    companion object {
        private const val PREF_NAME = "ArcaneGambitSettings"
        private const val KEY_SERVER_IP = "serverIp"
        private const val KEY_SERVER_PORT = "serverPort"
        private const val KEY_SERVER_PROTOCOL = "serverProtocol"
        
        // Default values
        private const val DEFAULT_SERVER_IP = "192.168.137.1"
        private const val DEFAULT_SERVER_PORT = "3001"
        private const val DEFAULT_SERVER_PROTOCOL = "http"
    }

    /**
     * Get server IP address
     */
    fun getServerIp(): String {
        return sharedPreferences.getString(KEY_SERVER_IP, DEFAULT_SERVER_IP) ?: DEFAULT_SERVER_IP
    }

    /**
     * Set server IP address
     */
    fun setServerIp(ip: String) {
        editor.putString(KEY_SERVER_IP, ip)
        editor.apply()
    }

    /**
     * Get server port
     */
    fun getServerPort(): String {
        return sharedPreferences.getString(KEY_SERVER_PORT, DEFAULT_SERVER_PORT) ?: DEFAULT_SERVER_PORT
    }

    /**
     * Set server port
     */
    fun setServerPort(port: String) {
        editor.putString(KEY_SERVER_PORT, port)
        editor.apply()
    }

    /**
     * Get server protocol
     */
    fun getServerProtocol(): String {
        return sharedPreferences.getString(KEY_SERVER_PROTOCOL, DEFAULT_SERVER_PROTOCOL) ?: DEFAULT_SERVER_PROTOCOL
    }

    /**
     * Set server protocol
     */
    fun setServerProtocol(protocol: String) {
        editor.putString(KEY_SERVER_PROTOCOL, protocol)
        editor.apply()
    }

    /**
     * Get complete server configuration
     */
    fun getServerConfig(): ServerConfig {
        return ServerConfig(
            ip = getServerIp(),
            port = getServerPort(),
            protocol = getServerProtocol()
        )
    }

    /**
     * Set complete server configuration
     */
    fun setServerConfig(config: ServerConfig) {
        editor.putString(KEY_SERVER_IP, config.ip)
        editor.putString(KEY_SERVER_PORT, config.port)
        editor.putString(KEY_SERVER_PROTOCOL, config.protocol)
        editor.apply()
    }

    /**
     * Reset server configuration to defaults
     */
    fun resetToDefaults() {
        editor.putString(KEY_SERVER_IP, DEFAULT_SERVER_IP)
        editor.putString(KEY_SERVER_PORT, DEFAULT_SERVER_PORT)
        editor.putString(KEY_SERVER_PROTOCOL, DEFAULT_SERVER_PROTOCOL)
        editor.apply()
    }

    /**
     * Data class for server configuration
     */
    data class ServerConfig(
        val ip: String,
        val port: String,
        val protocol: String
    ) {
        fun getBaseUrl(): String {
            return "$protocol://$ip:$port/api/"
        }
        
        fun isValid(): Boolean {
            return ip.isNotBlank() && 
                   port.isNotBlank() && 
                   port.toIntOrNull() != null &&
                   port.toInt() in 1..65535 &&
                   (protocol == "http" || protocol == "https")
        }
    }
}
