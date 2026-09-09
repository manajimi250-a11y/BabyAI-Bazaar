package com.newlifetech.babyhey.billing

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import ir.cafebazaar.poolakey.Connection
import ir.cafebazaar.poolakey.Payment
import ir.cafebazaar.poolakey.config.PaymentConfiguration
import ir.cafebazaar.poolakey.config.SecurityCheck
import ir.cafebazaar.poolakey.request.PurchaseRequest

/**
 * مدیریت خرید درون‌برنامه‌ای «باز کردن کامل محتوا» با استفاده از Poolakey (کافه‌بازار).
 * این کلاس باید فقط یک‌بار، در MainActivity، ساخته بشه (چون به ComponentActivity نیاز داره).
 */
class PurchaseManager(private val activity: ComponentActivity) {

    companion object {
        const val PRODUCT_ID_FULL_UNLOCK = "full_unlock"
        private const val PURCHASE_REQUEST_CODE = 5000

        // کلید عمومی RSA اپ از پنل کافه‌بازار (بخش «کلید ورود به برنامه» / RSA)
        private const val RSA_PUBLIC_KEY =
            "MIHNMA0GCSqGSIb3DQEBAQUAA4G7ADCBtwKBrwCwWQdRYYJS8hEzae9tAK3QrXzjC4JvDiY/A+Dt/pVRiL+WKNn7npIE3J9JaO+64PciwjRXWCqK+XBSbCqwVoS0y+ZXtzIoiVu80guFVJaox9OtMixRRy/VvvKDPVlpjpQQlv/mbGIUDmxuMHwcqe2McuQIVSdyPkvXEvayFwT9l00QsXxJWyHMlYlqndOPeDQUnoPBZaSGp8l3bWPbYTZb49mU+3IFHTtjsUOHQbMCAwEAAQ=="
    }

    private val paymentConfiguration = PaymentConfiguration(
        localSecurityCheck = SecurityCheck.Enable(rsaPublicKey = RSA_PUBLIC_KEY)
    )

    private val payment = Payment(context = activity, config = paymentConfiguration)
    private var paymentConnection: Connection? = null

    /** true اگه کاربر قبلاً محصول رو خریده باشه. UI باید این‌رو observe کنه. */
    val isUnlocked = mutableStateOf(false)

    /** true وقتی اتصال به سرویس پرداخت بازار برقرار شده باشه. */
    val isConnected = mutableStateOf(false)

    fun connect() {
        paymentConnection = payment.connect {
            connectionSucceed {
                isConnected.value = true
                checkPurchaseStatus()
            }
            connectionFailed {
                isConnected.value = false
            }
            disconnected {
                isConnected.value = false
            }
        }
    }

    fun disconnect() {
        paymentConnection?.disconnect()
    }

    /** چک می‌کنه آیا کاربر قبلاً محصول رو خریده یا نه (باید موقع باز شدن اپ صدا زده بشه). */
    fun checkPurchaseStatus() {
        payment.getPurchasedProducts {
            querySucceed { purchasedProducts ->
                isUnlocked.value = purchasedProducts.any { it.productId == PRODUCT_ID_FULL_UNLOCK }
            }
            queryFailed {
                // خطا در بررسی؛ وضعیت قبلی حفظ می‌شه
            }
        }
    }

    /** شروع فرآیند خرید محصول «باز کردن کامل محتوا». */
    fun purchaseFullUnlock(onResult: (success: Boolean, message: String?) -> Unit) {
        val purchaseRequest = PurchaseRequest(
            productId = PRODUCT_ID_FULL_UNLOCK,
            requestCode = PURCHASE_REQUEST_CODE,
            payload = "",
            dynamicPriceToken = null
        )
        payment.purchaseProduct(
            registry = activity.activityResultRegistry,
            request = purchaseRequest
        ) {
            purchaseFlowBegan {
                // صفحه‌ی خرید بازار با موفقیت باز شد
            }
            failedToBeginFlow { throwable ->
                onResult(false, throwable.message)
            }
            purchaseSucceed {
                isUnlocked.value = true
                onResult(true, null)
            }
            purchaseCanceled {
                onResult(false, null)
            }
            purchaseFailed { throwable ->
                onResult(false, throwable.message)
            }
        }
    }
}
