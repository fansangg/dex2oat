// IUserService.aidl
package com.fansan.dex2oat;

// Declare any non-default types here with import statements

interface IUserService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

    void destroy() = 16777114;

    void doSomething() = 1;

    List<String> dex2oat(String packageName) = 3;

    void exit() = 2;
}