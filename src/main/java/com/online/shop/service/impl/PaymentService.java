package com.online.shop.service.impl;

import com.online.shop.dto.OrderDto;
import com.online.shop.dto.OrderStatusDto;
import com.online.shop.dto.UserDto;
import com.online.shop.entity.Order;
import com.online.shop.entity.OrderStatus;
import com.online.shop.service.abstraction.LoggableService;
import com.online.shop.service.exception.EntityUpdateException;
import com.online.shop.service.util.PayPalInfo;
import com.paypal.api.payments.PayerInfo;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.ShippingAddress;
import com.paypal.api.payments.Transaction;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class PaymentService extends LoggableService {

    private final PayPalService payPalService;
    private final OrderServiceImpl orderService;
    private final UserServiceImpl userService;

    @Autowired
    public PaymentService(PayPalService payPalService, OrderServiceImpl orderService, UserServiceImpl userService) {
        super(LoggerFactory.getLogger(PaymentService.class));
        this.payPalService = payPalService;
        this.orderService = orderService;
        this.userService = userService;
    }

    public Optional<String> getPaymentApprovalLink(OrderDto orderDto, String username) {
        if (orderDto.isPaid() ||
                OrderStatus.getStatusObject(orderDto.getStatus()).getSequenceNumber() >
                        OrderStatus.WAITING_FOR_PAYMENT.getSequenceNumber()) {
            return Optional.empty();
        }
        if(orderDto.getPaymentId() != null && !orderDto.getPaymentId().isEmpty()) {
            return payPalService.getPaymentApprovalLink(orderDto.getPaymentId());
        } else {
            UserDto userDto = userService.getUserDtoByUsername(username);
            try {
                PayPalInfo info = payPalService.authorizePayment(
                        orderDto,
                        userDto
                );
                orderService.addPaymentId(orderDto.getId(), info.paymentId);
                orderService.updateStatus(orderDto.getId(), new OrderStatusDto()
                        .setStatus(OrderStatus.WAITING_FOR_PAYMENT.getStatus())
                        .setDescription(OrderStatus.WAITING_FOR_PAYMENT.getDescription()));
                return Optional.of(info.approvalLink);
            } catch (Exception ex) {
                orderService.cancelOrder(orderDto.getId(), "Payment failure");
                throw ex;
            }
        }
    }

    public OrderDto payFor(Long orderId, String paymentId, String payerId) {
        Order order = orderService.getOrderById(orderId);
        logger.debug("Payment id " + paymentId);
        logger.debug("Payer id " + payerId);
        if(paymentId.equals(order.getPaymentId())) {
            if(order.isPaid()) {
                throw new EntityUpdateException("Order already paid");
            }
            if(order.getStatus() != OrderStatus.WAITING_FOR_PAYMENT) {
                throw new EntityUpdateException("Cannot pay for order within current status (" + order.getStatus().toString() + ")");
            }
            OrderDto orderDto = orderService.updateStatus(orderId, new OrderStatusDto()
                    .setStatus(OrderStatus.PAID.getStatus())
                    .setDescription(OrderStatus.PAID.getDescription()));
            Payment payment = payPalService.executePayment(paymentId, payerId);
            // getting order info from payment
            PayerInfo payerInfo = payment.getPayer().getPayerInfo();
            Transaction transaction = payment.getTransactions().get(0);
            ShippingAddress shippingAddress = transaction.getItemList().getShippingAddress();
            logger.debug("Payment: " + payment.toJSON());
            if(payment.getFailureReason() != null) {
                logger.error("Could not pay for order #" + orderId + ", reason: " + payment.getFailureReason());
                throw new EntityUpdateException("Payment failure: " + payment.getFailureReason());
            }

            logger.debug("Shipping: " + shippingAddress.toJSON());
            logger.debug("Payer: " + payerInfo.toJSON());
            logger.debug("Transaction: " + transaction.toJSON());

            return orderDto;
        } else {
            throw new EntityUpdateException("Different payment ids");
        }
    }

}
