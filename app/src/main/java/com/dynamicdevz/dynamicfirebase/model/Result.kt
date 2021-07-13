package com.dynamicdevz.dynamicfirebase.model

data class Result(
    val address_components: List<AddressComponent>,
    val formatted_address: String,
    val geometry: Geometry,
    val place_id: String,
    val plus_code: PlusCodeX,
    val postcode_localities: List<String>,
    val types: List<String>
)