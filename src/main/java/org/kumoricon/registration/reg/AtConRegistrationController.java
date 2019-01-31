package org.kumoricon.registration.reg;

import org.kumoricon.registration.model.attendee.Attendee;
import org.kumoricon.registration.model.attendee.AttendeeRepository;
import org.kumoricon.registration.model.order.Order;
import org.kumoricon.registration.model.order.OrderRepository;
import org.kumoricon.registration.model.user.User;
import org.kumoricon.registration.model.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;


/**
 * This controller handles the pre-reg checkin flow, verifying attendee information and
 * printing a badge
 */
@Controller
public class AtConRegistrationController {
    private final AttendeeRepository attendeeRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Autowired
    public AtConRegistrationController(AttendeeRepository attendeeRepository, OrderRepository orderRepository, UserRepository userRepository) {
        this.attendeeRepository = attendeeRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @RequestMapping(value = "/reg/atconorder/{orderId}/attendee/{attendeeId}")
    @PreAuthorize("hasAuthority('at_con_registration')")
    public String atConAttendee(Model model,
                         @PathVariable String orderId,
                         @PathVariable String attendeeId,
                         @RequestParam(required = false) String err,
                         @RequestParam(required=false) String msg) {
        Order order = orderRepository.getOne(getIdFromParamter(orderId));
        Attendee attendee = null;
        if (attendeeId.equals("new")) {
            attendee = new Attendee();
            if (order.getAttendees().size() > 0) {
                Attendee prevAttendee = order.getAttendees().get(0);
                attendee.setEmergencyContactFullName(prevAttendee.getEmergencyContactFullName());
                attendee.setEmergencyContactPhone(prevAttendee.getEmergencyContactPhone());
                if (prevAttendee.isMinor()) {
                    attendee.setParentFullName(prevAttendee.getParentFullName());
                    attendee.setParentPhone(prevAttendee.getParentPhone());
                    attendee.setParentIsEmergencyContact(prevAttendee.getParentIsEmergencyContact());
                }
            }
        } else {
            attendee = order.getAttendeeById(getIdFromParamter(attendeeId));
        }

        model.addAttribute("order", order);
        model.addAttribute("attendee", attendee);
        model.addAttribute("msg", msg);
        model.addAttribute("err", err);
        return "reg/atcon-order-attendee";
    }

    @RequestMapping(value = "/reg/atconorder/{orderId}/attendee/{attendeeId}", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('at_con_registration')")
    public String atConAttendee(Model model,
                                @ModelAttribute final Attendee attendee,
                                final BindingResult bindingResult,
                                @PathVariable String orderId,
                                @PathVariable String attendeeId,
                                @RequestParam(required = false) String err,
                                @RequestParam(required=false) String msg) {

        if (bindingResult.hasErrors()) {
            return "reg/atcon-order-attendee";
        }


        Order order = orderRepository.getOne(getIdFromParamter(orderId));
        order.addAttendee(attendee);
        attendee.setOrder(order);
        orderRepository.save(order);

        model.addAttribute("order", order);
        model.addAttribute("attendee", attendee);
        model.addAttribute("msg", msg);
        model.addAttribute("err", err);
        return "redirect:/reg/atconorder/" + order.getId() + "?msg=Added+" + attendee.getFirstName();
    }


    @RequestMapping(value = "/reg/atconorder/{orderId}")
    @PreAuthorize("hasAuthority('at_con_registration')")
    public String atConOrder(Model model,
                                @PathVariable String orderId,
                                @RequestParam(required = false) String err,
                                @RequestParam(required=false) String msg) {

        Order order = orderRepository.getOne(getIdFromParamter(orderId));
        Integer thing = order.getId();
        System.out.println(thing);
        model.addAttribute("order", order);
        model.addAttribute("msg", msg);
        model.addAttribute("err", err);
        return "reg/atcon-order";
    }




    @RequestMapping(value = "/reg/atconorder/new", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('at_con_registration')")
    public String newOrder(Model model, Principal principal) {
        User user = userRepository.findOneByUsernameIgnoreCase(principal.getName());
        Order order = new Order();
        order.setOrderTakenByUser(user);
        order.setOrderId(Order.generateOrderId());
        order = orderRepository.save(order);


        return "redirect:/reg/atconorder/" + order.getId() + "/attendee/new";
    }


    private Integer getIdFromParamter(String parameter) {
        try {
            return Integer.parseInt(parameter);
        } catch (NumberFormatException ex) {
            throw new RuntimeException("Bad parameter: " + parameter + " is not an integer");
        }
    }

}
