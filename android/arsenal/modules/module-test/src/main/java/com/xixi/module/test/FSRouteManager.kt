package com.xixi.module.test

import android.util.Log

/**
 * <pre>
 *     author : maokangren
 *     e-mail : maokangren@chexiang.com
 *     desc   :
 * </pre>
 */
object CXRouteManager {
    val TAG: String = javaClass.name

    private val moduleMap: MutableMap<String, String> = hashMapOf(
            "fsj://native//test:" to "com.xixi.module.test.TestRouteManager",
            "fsj://native//other:" to "com.xixi.module.other.OtherRouteManager"
    )

    init {
        moduleMap.entries.forEach {
            try {
                Class.forName(it.value)
            } catch (exception: ClassNotFoundException) {
                Log.e(TAG, "${it.key},${it.value}", exception)
            }
        }
    }
}