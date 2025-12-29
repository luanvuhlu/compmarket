package com.gearvn.ecommerce.exception

class ResourceNotFoundException(message: String) : RuntimeException(message)

class BadRequestException(message: String) : RuntimeException(message)

class UnauthorizedException(message: String) : RuntimeException(message)

class ForbiddenException(message: String) : RuntimeException(message)

class DuplicateResourceException(message: String) : RuntimeException(message)

class InsufficientStockException(message: String) : RuntimeException(message)

class PaymentException(message: String) : RuntimeException(message)
