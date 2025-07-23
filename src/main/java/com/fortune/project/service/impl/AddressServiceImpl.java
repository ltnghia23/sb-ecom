package com.fortune.project.service.impl;

import com.fortune.project.dto.request.address.AddressCreateRequest;
import com.fortune.project.dto.request.address.AddressResponse;
import com.fortune.project.dto.response.common.ApiResponse;
import com.fortune.project.dto.response.common.PagingResponse;
import com.fortune.project.entity.AddressEntity;
import com.fortune.project.entity.DistrictEntity;
import com.fortune.project.entity.UserEntity;
import com.fortune.project.exception.ResourceNotFoundException;
import com.fortune.project.repository.AddressRepository;
import com.fortune.project.repository.DistrictRepository;
import com.fortune.project.service.AddressService;
import com.fortune.project.util.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressServiceImpl implements AddressService {

    private final ModelMapper modelMapper;
    private final AuthUtil authUtil;
    private final AddressRepository addressRepository;
    private final DistrictRepository districtRepository;


    @Override
    public ApiResponse<?> createAddress(AddressCreateRequest request) {
        UserEntity user = authUtil.loggedInUser();
        DistrictEntity district = districtRepository.findById(request.getDistrictId())
                .orElseThrow(() -> new ResourceNotFoundException("District", "district id", request.getDistrictId()));

        AddressEntity address = modelMapper.map(request, AddressEntity.class);
        address.setId(null);
        address.setDistrict(district);
        address.setUser(user);
        AddressEntity saved = addressRepository.save(address);
        AddressResponse addressResponse = modelMapper.map(saved, AddressResponse.class);
        addressResponse.setDistrict(district.getName());
        return new ApiResponse<>("Address saved", addressResponse, LocalDateTime.now());
    }

    @Override
    public ApiResponse<?> viewAllAddress(Pageable pageable) {
        Page<AddressEntity> addresses = addressRepository.findByIsDeletedFalse(pageable);
        Page<AddressResponse> addressResponses = addresses.map(address -> {
            AddressResponse addressResponse = modelMapper.map(address, AddressResponse.class);
            addressResponse.setDistrict(address.getDistrict().getName());
            return addressResponse;
        });
        var pagingResponse = new PagingResponse<>(addressResponses);
        return new ApiResponse<>("Fetched all address", pagingResponse, LocalDateTime.now());
    }

    @Override
    public ApiResponse<?> getAddressById(Long addressId) {
        AddressEntity address = addressRepository.findByIdAndIsDeletedFalse(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "address id", addressId));

        AddressResponse addressResponse = modelMapper.map(address, AddressResponse.class);
        addressResponse.setDistrict(address.getDistrict().getName());
        return new ApiResponse<>("Fetched address", addressResponse, LocalDateTime.now());
    }

    @Override
    public ApiResponse<?> getUserAddresses(Long userId) {
        List<AddressEntity> addresses = addressRepository.findByUser_id(userId);
        if (addresses.isEmpty()) {
            throw new ResourceNotFoundException("Address", "user id", userId);
        }
        List<AddressResponse> addressResponses = addresses.stream()
                .map(address -> {
                    AddressResponse addressResponse = modelMapper.map(address, AddressResponse.class);
                    addressResponse.setDistrict(address.getDistrict().getName());
                    return addressResponse;
                }).toList();
        return new ApiResponse<>("Fetched addresses", addressResponses, LocalDateTime.now());
    }

    @Override
    public ApiResponse<?> updateAddress(Long addressId, AddressCreateRequest request) {
        AddressEntity address = addressRepository.findByIdAndIsDeletedFalse(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "address id", addressId));

        DistrictEntity district = districtRepository.findById(request.getDistrictId())
                .orElseThrow(() -> new ResourceNotFoundException("District", "district id", request.getDistrictId()));

        modelMapper.map(request, address);
        address.setDistrict(district);
        AddressEntity saved = addressRepository.save(address);

        AddressResponse addressResponse = modelMapper.map(saved, AddressResponse.class);
        addressResponse.setDistrict(saved.getDistrict().getName());
        return new ApiResponse<>("Updated address", addressResponse, LocalDateTime.now());
    }

    @Override
    public ApiResponse<?> deleteAddressById(Long addressId) {
        AddressEntity address = addressRepository.findByIdAndIsDeletedFalse(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "address id", addressId));
        address.setIsDeleted(true);
        addressRepository.save(address);
        return new ApiResponse<>("Deleted address", null, LocalDateTime.now());
    }

    @Override
    public AddressEntity findById(Long shippingAddressId) {
        return addressRepository.findById(shippingAddressId).orElseThrow(() -> new ResourceNotFoundException("Address", "address id", shippingAddressId));
    }
}
