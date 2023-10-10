// IShizukuService.aidl
package com.fansan.dex2oat;

// Declare any non-default types here with import statements

interface IShizukuService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void destroy() = 16777114; // Destroy method defined by Shizuku server

    void dex2oat(in List<String> names) = 1;
}