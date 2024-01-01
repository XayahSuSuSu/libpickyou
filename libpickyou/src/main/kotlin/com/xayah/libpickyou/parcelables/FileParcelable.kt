package com.xayah.libpickyou.parcelables

import android.os.Parcel
import android.os.Parcelable

class FileParcelable() : Parcelable {
    var name: String = ""
    var creationTime: Long = 0
    var link: String? = null

    constructor(parcel: Parcel) : this() {
        name = parcel.readString()!!
        creationTime = parcel.readLong()
        link = parcel.readString()
    }

    constructor(path: String, creationTime: Long, link: String? = null) : this() {
        this.name = path
        this.creationTime = creationTime
        this.link = link
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeLong(creationTime)
        parcel.writeString(link)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FileParcelable> {
        override fun createFromParcel(parcel: Parcel): FileParcelable {
            return FileParcelable(parcel)
        }

        override fun newArray(size: Int): Array<FileParcelable?> {
            return arrayOfNulls(size)
        }
    }
}
