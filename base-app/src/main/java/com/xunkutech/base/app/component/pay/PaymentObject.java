package com.xunkutech.base.app.component.pay;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentObject {
    @NonNull
    String orderNo;
    @NonNull
    long amount;
    @NonNull
    Channel channel;
    @NonNull
    String subject;
    @NonNull
    String body;
    Long timeExpire;
    String description;
}
