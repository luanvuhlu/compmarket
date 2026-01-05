package com.luanvv.ecommerce.dto

import com.luanvv.ecommerce.entity.AddressType

data class AddressDto(
    val id: Long?,
    val addressType: AddressType,
    val street: String,
    val city: String,
    val state: String,
    val postalCode: String,
    val country: String,
    val isDefault: Boolean = false
)

data class CreateAddressRequest(
    val addressType: AddressType,
    val street: String,
    val city: String,
    val state: String,
    val postalCode: String,
    val country: String,
    val isDefault: Boolean = false
)
