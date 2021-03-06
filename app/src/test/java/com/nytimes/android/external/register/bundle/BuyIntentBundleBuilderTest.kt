package com.nytimes.android.external.register.bundle

import android.app.PendingIntent
import android.content.Intent
import android.os.Parcelable
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.nytimes.android.external.register.APIOverrides
import com.nytimes.android.external.register.bundle.BuyIntentBundleBuilder.Companion.EX_DEVELOPER_PAYLOAD
import com.nytimes.android.external.register.bundle.BuyIntentBundleBuilder.Companion.EX_ITEM_TYPE
import com.nytimes.android.external.register.bundle.BuyIntentBundleBuilder.Companion.EX_PACKAGE_NAME
import com.nytimes.android.external.register.bundle.BuyIntentBundleBuilder.Companion.EX_SKU
import com.nytimes.android.external.registerlib.GoogleUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify
import org.robolectric.RuntimeEnvironment
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowPendingIntent

@RunWith(AndroidJUnit4::class)
class BuyIntentBundleBuilderTest {

    private lateinit var testObject: BuyIntentBundleBuilder

    private val apiOverrides: APIOverrides = mock()

    @Before
    fun setUp() {
        testObject = BuyIntentBundleBuilder(RuntimeEnvironment.application, apiOverrides)
    }

    @Test
    fun testBundleOK() {
        whenever(apiOverrides.getBuyIntentResponse).thenReturn(GoogleUtil.RESULT_OK)

        val bundle = testObject.newBuilder()
                .packageName(PACKAGE_NAME)
                .sku(SKU)
                .type(TYPE)
                .developerPayload(DEVELOPER_PAYLOAD)
                .build()

        assertThat(bundle.getInt(GoogleUtil.RESPONSE_CODE))
                .isEqualTo(GoogleUtil.RESULT_OK)
        val pendingIntent = bundle.getParcelable<PendingIntent>(GoogleUtil.BUY_INTENT)
        val intent = getIntent(pendingIntent)
        assertThat(intent.getStringExtra(EX_PACKAGE_NAME))
                .isEqualTo(PACKAGE_NAME)
        assertThat(intent.getStringExtra(EX_SKU))
                .isEqualTo(SKU)
        assertThat(intent.getStringExtra(EX_ITEM_TYPE))
                .isEqualTo(TYPE)
        assertThat(intent.getStringExtra(EX_DEVELOPER_PAYLOAD))
                .isEqualTo(DEVELOPER_PAYLOAD)
    }

    @Test
    fun testBundleNotOK() {
        whenever(apiOverrides.getBuyIntentResponse).thenReturn(GoogleUtil.RESULT_ERROR)

        val bundle = testObject.newBuilder()
                .packageName(PACKAGE_NAME)
                .sku(SKU)
                .type(TYPE)
                .developerPayload(DEVELOPER_PAYLOAD)
                .build()

        assertThat(bundle.getInt(GoogleUtil.RESPONSE_CODE))
                .isEqualTo(GoogleUtil.RESULT_ERROR)
        assertThat(bundle.getParcelable<Parcelable>(GoogleUtil.BUY_INTENT) as? Intent)
                .isNull()
    }

    @Test
    fun testRawResponseCode() {
        testObject.rawResponseCode()
        verify<APIOverrides>(apiOverrides).getBuyIntentResponse
    }

    companion object {

        private const val PACKAGE_NAME = "com.my.pkg"
        private const val SKU = "sku1"
        private const val TYPE = GoogleUtil.BILLING_TYPE_SUBSCRIPTION
        private const val DEVELOPER_PAYLOAD = "devPayload"

        fun getIntent(pendingIntent: PendingIntent?): Intent {
            return (Shadow.extract<Any>(pendingIntent) as ShadowPendingIntent).savedIntent
        }
    }
}

