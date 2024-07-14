package com.xayah.libpickyou;

interface IRemoteRootService {
    ParcelFileDescriptor traverse(String pathString);
    boolean mkdirs(String src);
}
