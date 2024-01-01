package com.xayah.libpickyou.parcelables

import android.os.Parcel
import android.os.Parcelable

class DirChildrenParcelable() : Parcelable {
    var files: List<FileParcelable> = listOf()
    var directories: List<FileParcelable> = listOf()

    constructor(parcel: Parcel) : this() {
        files = parcel.createTypedArrayList(FileParcelable)!!
        directories = parcel.createTypedArrayList(FileParcelable)!!
    }

    constructor(files: List<FileParcelable>, directories: List<FileParcelable>) : this() {
        this.files = files
        this.directories = directories
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(files)
        parcel.writeTypedList(directories)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DirChildrenParcelable> {
        override fun createFromParcel(parcel: Parcel): DirChildrenParcelable {
            return DirChildrenParcelable(parcel)
        }

        override fun newArray(size: Int): Array<DirChildrenParcelable?> {
            return arrayOfNulls(size)
        }
    }
}
