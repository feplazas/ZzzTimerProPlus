package com.felipeplazas.zzztimerpro.utils

import android.Manifest
import android.content.pm.PackageManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.robolectric.annotation.Config

class PermissionManagerTest {

    @Test
    @Config(sdk = [32])
    fun `pre Android 13 shouldRequestPostNotifications false`() {
        assertFalse(PermissionManager.shouldRequestPostNotifications())
    }

    @Test
    @Config(sdk = [33])
    fun `Android 13+ shouldRequestPostNotifications true`() {
        assertTrue(PermissionManager.shouldRequestPostNotifications())
    }
}

