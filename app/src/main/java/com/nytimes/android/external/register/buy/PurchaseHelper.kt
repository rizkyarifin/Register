package com.nytimes.android.external.register.buy

import com.nytimes.android.external.register.APIOverrides
import com.nytimes.android.external.register.Purchases
import com.nytimes.android.external.register.model.Config
import com.nytimes.android.external.registerlib.InAppPurchaseData
import java.util.*
import javax.inject.Inject

class PurchaseHelper @Inject constructor(
        @JvmField private val config: Config?,
        private val apiOverrides: APIOverrides,
        private val purchases: Purchases
) {

    fun onBuy(purchaseData: PurchaseData, currentTimeMillis: Long): PurchaseResult {
        val newReceipt = String.format(Locale.getDefault(), BuyFragment.RECEIPT_FMT,
                apiOverrides.usersResponse, currentTimeMillis)
        val skuToPurchase = if (purchaseData.isReplace) purchaseData.newSku else purchaseData.sku
        val inAppPurchaseData = InAppPurchaseData.Builder()
                .orderId(java.lang.Long.toString(currentTimeMillis))
                .packageName(config!!.skus[skuToPurchase]!!.packageName)
                .productId(skuToPurchase)
                .purchaseTime(java.lang.Long.toString(currentTimeMillis))
                .developerPayload(purchaseData.developerPayload)
                .purchaseToken(newReceipt)
                .purchaseState("0")
                .build()
        val inAppPurchaseDataStr = InAppPurchaseData.toJson(inAppPurchaseData)
        val result: Boolean = if (purchaseData.isReplace) {
            purchases.replacePurchase(inAppPurchaseDataStr, purchaseData.oldSkus!!)
        } else {
            purchases.addPurchase(inAppPurchaseDataStr, purchaseData.itemtype!!)
        }
        return PurchaseResult(result, inAppPurchaseData)
    }
}