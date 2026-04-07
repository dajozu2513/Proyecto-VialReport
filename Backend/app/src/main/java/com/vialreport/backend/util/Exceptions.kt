package com.vialreport.backend.util

class NotFoundException(message: String) : Exception(message)
class UnauthorizedException(message: String) : Exception(message)
class BadRequestException(message: String) : Exception(message)
class ConflictException(message: String) : Exception(message)