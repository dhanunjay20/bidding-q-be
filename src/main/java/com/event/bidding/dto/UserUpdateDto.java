package com.event.bidding.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.event.bidding.entity.Address;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @Pattern(regexp = "^\\+?[1-9]\\d{9,14}$", message = "Mobile number must be valid")
    private String mobile;

    private List<Address> addresses; // allow replacing or setting addresses
    private Address address; // keep single-address convenience for adding

    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
             message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character")
    private String password; // Optional: only processed if not null/empty

    @Size(max = 500, message = "Profile URL must not exceed 500 characters")
    private String profileUrl; // allow updating profileUrl
}
