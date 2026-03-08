package me.psikuvit.express.event;

/**
 * Domain event fired after an order is accepted by a delivery guy.
 */
public record OrderAcceptedEvent(
        Long orderId,
        String deliveryGuyName,
        String whatsappNumber,
        String message
) {
}

