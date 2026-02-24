package me.psikuvit.express.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import me.psikuvit.express.dto.CheckedOrderItem;
import me.psikuvit.express.model.DeliveryGuy;
import me.psikuvit.express.model.Location;
import me.psikuvit.express.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * Service for sending WhatsApp messages to delivery guys using Twilio API.
 * Notifies delivery personnel about new order assignments with complete order details.
 */
@Service
public class WhatsAppService {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppService.class);

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.whatsapp.from}")
    private String fromWhatsAppNumber;

    @PostConstruct
    public void init() {
        try {
            Twilio.init(accountSid, authToken);
            logger.info("Twilio WhatsApp service initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize Twilio WhatsApp service: {}", e.getMessage());
        }
    }

    /**
     * Send order details to delivery guy via WhatsApp
     *
     * @param deliveryGuy The delivery guy to send the message to
     * @param order The order details
     * @param items The order items
     * @param deliveryLocation The delivery location
     */
    public void sendOrderToDeliveryGuy(DeliveryGuy deliveryGuy, Order order,
                                       List<CheckedOrderItem> items, Location deliveryLocation) {
        try {
            String messageBody = buildOrderMessage(deliveryGuy, order, items, deliveryLocation);

            Message message = Message.creator(
                    new PhoneNumber("whatsapp:" + deliveryGuy.getWhatsappNumber()),
                    new PhoneNumber(fromWhatsAppNumber),
                    messageBody
            ).create();

            logger.info("WhatsApp message sent successfully to {} - SID: {}",
                       deliveryGuy.getName(), message.getSid());
        } catch (Exception e) {
            logger.error("Failed to send WhatsApp message to {}: {}",
                        deliveryGuy.getName(), e.getMessage());
            // Don't throw exception - we don't want to fail the order if WhatsApp fails
        }
    }

    /**
     * Build formatted order message for WhatsApp
     */
    private String buildOrderMessage(DeliveryGuy deliveryGuy, Order order,
                                     List<CheckedOrderItem> items, Location deliveryLocation) {
        StringBuilder message = new StringBuilder();

        message.append("🚗 *NEW DELIVERY ORDER* 🚗\n\n");
        message.append("Hi ").append(deliveryGuy.getName()).append("! 👋\n\n");
        message.append("📦 *Order #").append(order.getId()).append("*\n");
        message.append("━━━━━━━━━━━━━━━━━━━━\n\n");

        message.append("📋 *ORDER ITEMS:*\n");
        for (CheckedOrderItem item : items) {
            message.append("• ").append(item.getProductName())
                   .append(" (").append(item.getSize()).append(")")
                   .append(" x").append(item.getQuantity())
                   .append(" - $").append(String.format("%.2f", item.getBasePrice() * item.getQuantity()))
                   .append("\n");
        }

        message.append("\n💰 *Total Price:* $").append(String.format("%.2f", order.getTotalPrice())).append("\n");
        message.append("📏 *Distance:* ").append(String.format("%.2f", order.getDistance())).append(" km\n\n");

        message.append("📍 *DELIVERY LOCATION:*\n");
        message.append("Address: ").append(deliveryLocation.getAddress()).append("\n");
        message.append("Coordinates: ").append(deliveryLocation.getLatitude())
               .append(", ").append(deliveryLocation.getLongitude()).append("\n");
        message.append("🗺️ Google Maps: https://maps.google.com/?q=")
               .append(deliveryLocation.getLatitude()).append(",")
               .append(deliveryLocation.getLongitude()).append("\n\n");

        message.append("━━━━━━━━━━━━━━━━━━━━\n");
        message.append("⏰ Please confirm pickup and start delivery ASAP!\n");
        message.append("Good luck! 🎉");

        return message.toString();
    }

    /**
     * Send a simple notification message
     *
     * @param phoneNumber The recipient's WhatsApp number (with country code)
     * @param messageText The message to send
     */
    public void sendMessage(String phoneNumber, String messageText) {
        try {
            Message message = Message.creator(
                    new PhoneNumber("whatsapp:" + phoneNumber),
                    new PhoneNumber(fromWhatsAppNumber),
                    messageText
            ).create();

            logger.info("WhatsApp message sent successfully - SID: {}", message.getSid());
        } catch (Exception e) {
            logger.error("Failed to send WhatsApp message to {}: {}", phoneNumber, e.getMessage());
        }
    }
}

