package cli;

import common.CurrencyEnum;
import static common.Utils.*;

public class EcomPaymentInput extends PaymentInput {

    private String paymentReference;
    private CurrencyEnum currency;
    private String orderId;

    public String getPaymentReference() {
        return paymentReference == null ? generatePaymentReference() : paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public String getCurrency() {
        return currency.name();
    }

    public void setCurrency(CurrencyEnum currency) {
        this.currency = currency;
    }

    public String getOrderId() {
        return orderId == null ? generateOrderId() : orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
