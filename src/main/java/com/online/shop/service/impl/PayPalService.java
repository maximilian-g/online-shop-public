package com.online.shop.service.impl;

import com.online.shop.config.PayPalConfig;
import com.online.shop.dto.ItemQuantityDto;
import com.online.shop.dto.OrderDto;
import com.online.shop.dto.UserDto;
import com.online.shop.service.exception.PaymentException;
import com.online.shop.service.util.PayPalInfo;
import com.paypal.api.payments.Address;
import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Details;
import com.paypal.api.payments.Item;
import com.paypal.api.payments.ItemList;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.PayerInfo;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PayPalService {

    public static final String APPROVAL_LINK_ATTR = "approvalLink";

    private final PayPalConfig config;
    private final Logger logger;

    @Autowired
    public PayPalService(PayPalConfig config) {
        this.config = config;
        this.logger = LoggerFactory.getLogger(PayPalService.class);
    }

    public PayPalInfo authorizePayment(OrderDto orderDto, UserDto userDto) {
        return authorizePayment(orderDto, userDto, () -> {});
    }

    // authorizing payment using OrderDto, UserDto objects, returns approval link
    public PayPalInfo authorizePayment(OrderDto orderDto, UserDto userDto, Runnable onFailure) {

        // getting information about Payer
        Payer payer = getPayerInformation(userDto, orderDto);
        // getting redirect URL's(cancel, review pages)
        RedirectUrls redirectUrls = getRedirectURLs(orderDto);
        // getting list of transactions
        List<Transaction> listTransaction = getTransactionInformation(orderDto);

        // creating payment, setting [listTransaction], [redirectUrls], [payer] in order to authorize
        Payment requestPayment = new Payment();
        requestPayment.setTransactions(listTransaction);
        requestPayment.setRedirectUrls(redirectUrls);
        requestPayment.setPayer(payer);
        requestPayment.setIntent("authorize");

        // getting APIContext using CLIENT_ID, CLIENT_SECRET, MODE(can be sandbox(to test payment) and live(actual production mode))
        APIContext apiContext = new APIContext(config.getClientId(), config.getClientSecret(), config.getMode());

        try {
            // creating approvedPayment using our [requestPayment] with [apiContext]
            Payment approvedPayment = requestPayment.create(apiContext);
            logger.debug("Payment id = " + approvedPayment.getId());

            // getting and returning approvalLink from [approvedPayment]
            return new PayPalInfo(getApprovalLink(approvedPayment), approvedPayment.getId());
        } catch (PayPalRESTException ex) {
            logger.error("Cannot create payment for order #" + orderDto.getId() +
                    ", exception: " + ex.getMessage());
            onFailure.run();
            throw new PaymentException("Could not create payment for order number " + orderDto.getId());
        }
    }

    // creating Payer info using Payer class
    private Payer getPayerInformation(UserDto userDto, OrderDto orderDto) {
        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        PayerInfo payerInfo = new PayerInfo();
        Address billingAddress = new Address();
        billingAddress.setLine1(orderDto.getAddress().getAddress());
        payerInfo.setEmail(userDto.getEmail());

        payer.setPayerInfo(payerInfo);

        return payer;
    }

    // creating redirect URL's(cancel page and review page)
    private RedirectUrls getRedirectURLs(OrderDto orderDto) {
        RedirectUrls redirectUrls = new RedirectUrls();

        redirectUrls.setCancelUrl(config.getBaseAppUrl() + "/orders/" + orderDto.getId() + "/cancel");
        redirectUrls.setReturnUrl(config.getBaseAppUrl() + "/orders/" + orderDto.getId() +"/proceed_payment");

        return redirectUrls;
    }

    // creating list of transactions
    private List<Transaction> getTransactionInformation(OrderDto orderDto) {

        String total = formatBigDecimalToPayPalString(orderDto.getTotal());

        // filling default(PayPal api) Details class with our orderDetail information
        Details details = new Details();
        details.setShipping("0");
        details.setSubtotal(total);
        details.setTax("0");

        // creating new amount using PayPal api, setting currency, total amount and Details
        Amount amount = new Amount();
        amount.setCurrency("USD");
        amount.setTotal(total);
        amount.setDetails(details);

        // creating transaction, setting amount, setting description(product name)
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription("Ordering items from online shop");

        // creating ItemList(PayPal api) and list of items, that customer have ordered
        ItemList itemList = new ItemList();
        List<Item> items = new ArrayList<>();

        for(ItemQuantityDto itemQuantityDto : orderDto.getOrderItems()) {

            // creating single item, setting currency, name, tax, etc.
            Item item = new Item();
            item.setCurrency("USD");
            item.setName(itemQuantityDto.getItem().getName());
            item.setPrice(formatBigDecimalToPayPalString(itemQuantityDto.getItem().getPrice().getPrice()));
            item.setTax("0");
            item.setQuantity(Long.toString(itemQuantityDto.getQuantity()));

            // adding created item to list of items, adding [List<Item> items] to [ItemList itemList] object
            // and [ItemList itemList] to [Transaction transaction] object
            items.add(item);
        }

        itemList.setItems(items);
        transaction.setItemList(itemList);

        // creating list of transactions, adding our current [Transaction transaction] object to list
        List<Transaction> listTransaction = new ArrayList<>();
        listTransaction.add(transaction);

        // returning list of transactions
        return listTransaction;
    }

    // getting payment by paymentId using APIContext and Payment.get() method
    public Payment getPaymentDetails(String paymentId) {
        APIContext apiContext = new APIContext(config.getClientId(), config.getClientSecret(), config.getMode());
        try {
            return Payment.get(apiContext, paymentId);
        } catch (PayPalRESTException ex) {
            logger.error("Cannot get payment details for payment #" + paymentId +
                    ", exception: " + ex.getMessage());
            throw new PaymentException("Could not get payment info");
        }
    }

    public Optional<String> getPaymentApprovalLink(String paymentId) {
        Payment payment = getPaymentDetails(paymentId);
        return Optional.ofNullable(getApprovalLink(payment));
    }

    // executes payment with concrete paymentId and payerId
    public Payment executePayment(String paymentId, String payerId) {
        // creating instance of PaymentExecution
        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);

        // creating payment with [paymentId] id
        Payment payment = new Payment().setId(paymentId);

        // creating APIContext from CLIENT_ID, CLIENT_SECRET, MODE
        APIContext apiContext = new APIContext(config.getClientId(), config.getClientSecret(), config.getMode());

        try {
            // Executing payment and returning executed payment
            return payment.execute(apiContext, paymentExecution);
        } catch (PayPalRESTException ex) {
            logger.error("Cannot execute payment #" + paymentId +
                    ", exception: " + ex.getMessage());
            throw new PaymentException("Could execute payment");
        }
    }

    // getting approval url from approvedPayment
    private static String getApprovalLink(Payment approvedPayment) {
        List<Links> links = approvedPayment.getLinks();
        for (Links link : links) {
            if (link.getRel().equalsIgnoreCase("approval_url")) {
                return link.getHref();
            }
        }
        return null;
    }

    private static String formatBigDecimalToPayPalString(BigDecimal value) {
        value = value.setScale(2, RoundingMode.HALF_UP);
        return value.toString();
    }

}
