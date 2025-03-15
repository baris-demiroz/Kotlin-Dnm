package com.example.yemekkitabi.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Tarif (

    @ColumnInfo(name = "isim")//isimler farklı da olabilir
    var isim : String,

    @ColumnInfo(name = "tarif") //isimler farklı da olabilir
    var tarif : String,

    @ColumnInfo(name = "gorsel")//isimler farklı da olabilir
    var gorsel : ByteArray

){
    @PrimaryKey(autoGenerate = true)
    var id =0
}