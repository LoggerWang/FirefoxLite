package com.mucc.flownet

class  ApiException  (val code:Int?, private val msg:String):Exception(msg)
