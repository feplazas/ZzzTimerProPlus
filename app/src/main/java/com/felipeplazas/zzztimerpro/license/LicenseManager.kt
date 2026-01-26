package com.felipeplazas.zzztimerpro.license

import android.content.Context

/**
 * Simplified License Manager for PAID app model.
 * 
 * This app is sold on Play Store for $0.99 (one-time purchase).
 * All features are always unlocked - no in-app purchases or restrictions.
 * 
 * This class is kept for compatibility with existing code that checks isPremium().
 */
class LicenseManager(private val context: Context) {
    
    companion object {
        const val PRODUCT_ID = "zzz_timer_pro_license" // Keep for reference if needed
    }
    
    /**
     * Always returns true - this is a PAID app.
     * Everyone who downloads the app has paid for it.
     */
    fun isPremium(): Boolean = true
    
    /**
     * Always returns true - all features unlocked.
     */
    fun isLicensePurchased(): Boolean = true
    
    /**
     * Status message for settings screen.
     */
    fun getLicenseStatusMessage(): String = "PREMIUM"
    
    // No-op methods for compatibility
    fun initializeBillingClient() {}
    fun queryPurchases() {}
    fun endConnection() {}
}
