package com.dynamicdevz.dynamicfirebase.model

data class FirePost(
    var time: String,
    var caption: String,
    var imageUrl: String,
    var userId: String,
    var likes: Int,
    var location: String
){
    constructor(): this("", "", "", "", 0, "")

}
