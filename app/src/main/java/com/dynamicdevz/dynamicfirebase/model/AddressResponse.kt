package com.dynamicdevz.dynamicfirebase.model

data class AddressResponse(
    val plus_code: PlusCode,
    val results: List<Result>,
    val status: String
)