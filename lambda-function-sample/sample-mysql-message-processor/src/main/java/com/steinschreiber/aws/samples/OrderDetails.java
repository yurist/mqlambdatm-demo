package com.steinschreiber.aws.samples;

import java.math.BigDecimal;

/**
 * Created by mdevhs on 12/2/16.
 */
public class OrderDetails {

    private String productCode;
    private int quantityOrdered;
    private BigDecimal priceEach;
    private int orderLineNumber;

    public String getProductCode() {
        return productCode;
    }

    public int getQuantityOrdered() {
        return quantityOrdered;
    }

    public BigDecimal getPriceEach() {
        return priceEach;
    }

    public int getOrderLineNumber() {
        return orderLineNumber;
    }

    @Override
    public String toString() {
        return "OrderDetails{" +
                "productCode='" + productCode + '\'' +
                ", quantityOrdered=" + quantityOrdered +
                ", priceEach=" + priceEach +
                ", orderLineNumber=" + orderLineNumber +
                '}';
    }
}
