package com.olivadevelop.kore.ads;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;

import java.util.List;

public class BillingManager implements PurchasesUpdatedListener {
    public interface Callback {
        void onPurchase();
        void onPurchaseError(String error, String idProduct);
    }

    private final Context context;
    private final Callback callback;
    private BillingClient billingClient;
    private String requestPurchaseIdProduct;
    public BillingManager(Context context, Callback callback) {
        this.context = context.getApplicationContext();
        this.callback = callback;
        init();
    }
    private void init() {
        billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
                .build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult result) {
                if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) { queryExistingPurchases(); }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Se reconectará automáticamente
            }
        });
    }

    /**
     * Restauración automática
     */
    private void queryExistingPurchases() {
        QueryPurchasesParams params = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build();
        billingClient.queryPurchasesAsync(params, (result, purchases) -> {
            if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) { for (Purchase purchase : purchases) { handlePurchase(purchase); } }
        });
    }

    /**
     * Lanzar compra
     */
    public void launchPurchase(Activity activity, String idProduct) {
        if (billingClient == null || !billingClient.isReady()) {
            callback.onPurchaseError("Billing not available", idProduct);
            return;
        }
        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(List.of(QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(idProduct)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build())
                ).build();
        billingClient.queryProductDetailsAsync(params, (billingResult, productDetailsList) -> {
            if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK || productDetailsList.getProductDetailsList().isEmpty()) {
                callback.onPurchaseError("Product not available", idProduct);
                return;
            }
            this.requestPurchaseIdProduct = idProduct;
            ProductDetails productDetails = productDetailsList.getProductDetailsList().get(0);
            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(List.of(BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build()))
                    .build();
            billingClient.launchBillingFlow(activity, flowParams);
        });
    }

    /**
     * Resultado de compra
     */
    @Override
    public void onPurchasesUpdated(BillingResult result, List<Purchase> purchases) {
        if (result.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) { handlePurchase(purchase); }
        } else if (result.getResponseCode() != BillingClient.BillingResponseCode.USER_CANCELED) {
            callback.onPurchaseError(result.getDebugMessage(), this.requestPurchaseIdProduct);
        }
    }

    /**
     * Procesar compra
     */
    private void handlePurchase(Purchase purchase) {
        if (!purchase.getProducts().contains(this.requestPurchaseIdProduct)) { return; }
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            // Acknowledge obligatorio
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
                billingClient.acknowledgePurchase(params, result -> {
                    if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) { grantPurchasedItem(); }
                });
            } else {
                grantPurchasedItem();
            }
        }
    }
    private void grantPurchasedItem() { callback.onPurchase(); }
    public void destroy() { if (billingClient != null) { billingClient.endConnection(); } }
}