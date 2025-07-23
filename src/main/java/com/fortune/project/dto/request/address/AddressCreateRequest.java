package com.fortune.project.dto.request.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class AddressCreateRequest {

    @NotNull
    private Long districtId;

    @NotBlank
    @Size(max = 100)
    private String fullName;

    @NotBlank
    @Pattern(regexp = "^(\\+84|0)[3-9][0-9]{8}$", message = "Invalid phone number")
    private String phoneNumber;

    @NotBlank
    @Size(min = 5, message = "Street name must be at least 5 characters")
    private String street;

    private String buildingName;

    private String ward;

    private String city;

    private String country;

    private Boolean isDefault = false;
}
