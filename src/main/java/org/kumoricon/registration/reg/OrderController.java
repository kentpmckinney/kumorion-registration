package org.kumoricon.registration.reg;

import org.kumoricon.registration.model.attendee.AttendeeRepository;
import org.kumoricon.registration.model.order.Order;
import org.kumoricon.registration.model.order.OrderRepository;
import org.kumoricon.registration.model.order.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class OrderController {
    private final OrderRepository orderRepository;
    private final AttendeeRepository attendeeRepository;
    private final PaymentRepository paymentRepository;

    @Autowired
    public OrderController(OrderRepository orderRepository, AttendeeRepository attendeeRepository, PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.attendeeRepository = attendeeRepository;
        this.paymentRepository = paymentRepository;
    }

    @RequestMapping(value = "/orders")
    @PreAuthorize("hasAuthority('manage_orders')")
    public String listOrders(Model model, @RequestParam(required = false) Integer page) {
        Integer nextPage, prevPage;
        if (page == null) { page = 0; }
        if (page > 0) {
            prevPage = page-1;
        } else {
            prevPage = null;
        }
        List<Order> orders = orderRepository.findAllBy(page);

        if (orders.size() == 20) {
            nextPage = page + 1;
        } else {
            nextPage = null;
        }

        model.addAttribute("orders", orders);
        model.addAttribute("totalCount", orderRepository.count());
        model.addAttribute("page", page);
        model.addAttribute("nextPage", nextPage);
        model.addAttribute("prevPage", prevPage);
        return "reg/orders";
    }

    @RequestMapping(value = "/orders/{orderId}")
    @PreAuthorize("hasAuthority('manage_orders')")
    public String listOrdersById(Model model, @PathVariable String orderId) {
        Order order = orderRepository.findById(getIdFromParameter(orderId));
        model.addAttribute("order", order);
        model.addAttribute("attendees", attendeeRepository.findAllByOrderId(getIdFromParameter(orderId)));
        model.addAttribute("payments", paymentRepository.findByOrderId(getIdFromParameter(orderId)));
        return "reg/orders-by-id";
    }

    private Integer getIdFromParameter(String parameter) {
        try {
            return Integer.parseInt(parameter);
        } catch (NumberFormatException ex) {
            throw new RuntimeException("Bad parameter: " + parameter + " is not an integer");
        }
    }
}
