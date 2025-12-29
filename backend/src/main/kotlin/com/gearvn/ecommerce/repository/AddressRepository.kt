package com.gearvn.ecommerce.repository

import com.gearvn.ecommerce.entity.Address
import com.gearvn.ecommerce.entity.AddressType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AddressRepository : JpaRepository<Address, Long> {
    fun findByUserId(userId: Long): List<Address>
    fun findByUserIdAndAddressType(userId: Long, addressType: AddressType): List<Address>
    fun findByUserIdAndIsDefaultTrue(userId: Long): Address?
}
