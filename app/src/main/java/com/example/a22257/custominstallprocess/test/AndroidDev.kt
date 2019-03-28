package com.example.a22257.custominstallprocess.test

class AndroidDev {
    var song = "song"
    var name = 12
    var isSex = false

    fun math() {
        for (i in 0..22) {
            println(i.toString())
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val androidDev = AndroidDev()
            androidDev.math()
            println("songjian")
        }
    }
}
