package com.fortune.project.dto.request.address;

import com.fortune.project.entity.AddressEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressResponse {
    private String fullName;
    private String phone;
    private String buildingName;
    private String street;
    private String ward;
    private String district;
    private String city;

    public static AddressResponse from(AddressEntity entity) {
        return new AddressResponse(
                entity.getFullName(),
                entity.getPhoneNumber(),
                entity.getBuildingName(),
                entity.getStreet(),
                entity.getWard(),
                entity.getDistrict().getName(),
                entity.getCity()
        );
    }
}
