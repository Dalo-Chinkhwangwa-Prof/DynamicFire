package com.dynamicdevz.dynamicfirebase

data class FirePost(
    var time: String,
    var caption: String,
    var imageUrl: String,
    var userId: String,
    var likes: Int
){

    constructor(): this("", "", "", "", 0)

}
