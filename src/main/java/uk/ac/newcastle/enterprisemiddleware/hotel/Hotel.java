package uk.ac.newcastle.enterprisemiddleware.hotel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.ac.newcastle.enterprisemiddleware.booking.Booking;
import uk.ac.newcastle.enterprisemiddleware.customer.Customer;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

@Entity
@NamedQueries({
        @NamedQuery(name = Hotel.FIND_ALL, query = "SELECT h FROM Hotel h ORDER BY h.name ASC"),
        @NamedQuery(name = Hotel.FIND_BY_POSTALCODE, query = "SELECT h FROM Hotel h WHERE h.postalCode = :postalCode"),
        @NamedQuery(name = Hotel.FIND_BY_PHONE, query = "SELECT h FROM Hotel h WHERE h.phoneNumber = :phoneNumber")
})
@XmlRootElement
@Table(name = "hotels", uniqueConstraints = @UniqueConstraint(columnNames = "phoneNumber"))
public class Hotel implements Serializable {
    public static final String FIND_ALL = "hotel.findAll";
    public static final String FIND_BY_POSTALCODE = "hotel.findByPostalCode";

    public static final String FIND_BY_PHONE = "hotel.findByPhone";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Hotel name is required")
    @Size(max = 50, message = "Hotel name must be less than or equal to 50 characters")
    private String name;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^0[0-9]{10}$", message = "Invalid phone number format")
    private String phoneNumber;

    @NotBlank(message = "Postal code is required")
    @Size(min = 6, max = 6, message = "Postal code must be 6 characters long")
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "Postal code must be alphanumeric")
    private String postalCode;

    @JsonIgnore
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Booking> bookings;

    // Constructors

    public Hotel() {
        // Default constructor needed by JPA
    }

    public Hotel(String name, String location, String phoneNumber, String postalCode) {
        this.name = name;
        this.location = location;
        this.phoneNumber = phoneNumber;
        this.postalCode = postalCode;
    }

    // Getter and Setter methods

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    @Override
    public String toString() {
        return "Hotel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", postalCode='" + postalCode + '\'' +
                '}';
    }
}
