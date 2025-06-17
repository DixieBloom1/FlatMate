package com.example.flatmate.models

import android.os.Parcel
import android.os.Parcelable

data class Ad(
    val title: String = "",
    val name: String = "",
    val surname: String = "",
    val birthYear: String = "",
    val faculty: String = "",
    val location: String = "",
    val description: String = "",
    val imageUrl: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(name)
        parcel.writeString(surname)
        parcel.writeString(birthYear)
        parcel.writeString(faculty)
        parcel.writeString(location)
        parcel.writeString(description)
        parcel.writeString(imageUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Ad> {
        override fun createFromParcel(parcel: Parcel): Ad {
            return Ad(parcel)
        }

        override fun newArray(size: Int): Array<Ad?> {
            return arrayOfNulls(size)
        }
    }
}
