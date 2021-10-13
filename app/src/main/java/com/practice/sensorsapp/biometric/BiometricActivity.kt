package com.practice.sensorsapp.biometric

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.practice.sensorsapp.MainActivity
import com.practice.sensorsapp.R
import com.practice.sensorsapp.util.AuthenticationError
import com.practice.sensorsapp.util.navigateTo

class BiometricActivity : AppCompatActivity() {

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var biometricManager: BiometricManager

    private val biometricCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            navigateTo<MainActivity>()
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)

            if (errorCode != AuthenticationError.AUTHENTICATION_DIALOG_DISMISSED.errorCode && errorCode != AuthenticationError.CANCELLED.errorCode) {
                setErrorNotice(errString.toString())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biometric)
        setupBiometricAuthentication()
        checkBiometricFeatureState()

        val ivAuthenticate = findViewById<ImageView>(R.id.ivAuthenticate)
        ivAuthenticate.setOnClickListener {
            if (isBiometricFeatureAvailable()) {
                biometricPrompt.authenticate(buildBiometricPrompt())
            }
        }

    }

    private fun setupBiometricAuthentication() {
        biometricManager = BiometricManager.from(this)
        val executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, biometricCallback)
    }

    private fun checkBiometricFeatureState() {
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> setErrorNotice("Sorry. It seems your device has no biometric hardware")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> setErrorNotice("Biometric features are currently unavailable.")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> setErrorNotice("You have not registered any biometric credentials")
            BiometricManager.BIOMETRIC_SUCCESS -> {
            }
        }
    }

    private fun buildBiometricPrompt(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle("Verify your identity")
            .setDescription("Confirm your identity so we can verify it's you")
            .setNegativeButtonText("Cancel")
            .setConfirmationRequired(false) //Allows user to authenticate without performing an action, such as pressing a button, after their biometric credential is accepted.
            .build()
    }

    private fun isBiometricFeatureAvailable(): Boolean {
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
    }

    private fun setErrorNotice(errorMessage: String) {
        val tvErrorNotice = findViewById<TextView>(R.id.tvErrorNotice)
        tvErrorNotice.text = errorMessage
    }
}