package uk.ac.newcastle.enterprisemiddleware.guestBooking;

import uk.ac.newcastle.enterprisemiddleware.booking.Booking;
import uk.ac.newcastle.enterprisemiddleware.customer.Customer;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class GuestBooking {

    @Valid
    @NotNull(message = "Customer cannot be null")
    private Customer customer;

    @Valid
    @NotNull(message = "Booking cannot be null")
    private Booking booking;

    // Constructors, getters, and setters

    public GuestBooking() {
    }

    public GuestBooking(@Valid @NotNull(message = "Customer cannot be null") Customer customer,
                        @Valid @NotNull(message = "Booking cannot be null") Booking booking) {
        this.customer = customer;
        this.booking = booking;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }
}
