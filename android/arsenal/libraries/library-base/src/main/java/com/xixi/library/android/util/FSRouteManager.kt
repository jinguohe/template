package com.xixi.library.android.util

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.taobao.atlas.bundleInfo.AtlasBundleInfoManager
import android.taobao.atlas.framework.Atlas
import android.taobao.atlas.runtime.ActivityTaskMgr
import android.taobao.atlas.runtime.BundleUtil
import android.taobao.atlas.runtime.RuntimeVariables
import android.text.TextUtils
import com.xixi.library.android.base.FSActivity
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Suppress("unused")
object FSRouteManager {
    private val KEY_ID_CALLBACK = "id_callback"

    private val callbackMap: MutableMap<String, ((bundle: Bundle?) -> Unit?)?> = ConcurrentHashMap()

    private val interceptors: MutableSet<(activity: Activity?, uri: Uri?, bundle: Bundle?) -> Boolean> = Collections.synchronizedSet(HashSet())

    fun addInterceptor(interceptor: (activity: Activity?, uri: Uri?, bundle: Bundle?) -> Boolean) {
        interceptors.add(interceptor)
    }

    @JvmOverloads
    @Synchronized
    fun goToWeb(activity: Activity?, uri: Uri?, callback: ((bundle: Bundle?) -> Unit?)? = null) {
        if (activity == null || activity.isFinishing || uri == null) {
            FSLogUtil.e("goToActivity failed, activity or activityName is invalid !")
            if (callback != null)
                callback(null)
            return
        }
        //val paramBundle = Bundle()
        //uri?.query?.split("&")?.map { it.split("=") }?.filter { it.size == 2 }?.forEach { paramBundle.putString(it[0], it[1]) }
        //return interceptors.any { it.invoke(activity, uri, paramBundle) }
    }

    @JvmOverloads
    @Synchronized
    fun goToActivity(activity: Activity?, activityName: String?, bundle: Bundle? = null, callback: ((bundle: Bundle?) -> Unit?)? = null) {
        if (activity == null || activity.isFinishing || TextUtils.isEmpty(activityName)) {
            FSLogUtil.e("goToActivity failed, activity or activityName is invalid !")
            if (callback != null)
                callback(null)
            return
        }
        val intent = Intent().setClassName(activity, activityName).putExtras(bundle ?: Bundle())
        if (callback != null) {
            val id = activityName + ":" + System.currentTimeMillis()
            callbackMap.put(id, callback)
            intent.putExtra(KEY_ID_CALLBACK, id)
        }
        FSLogUtil.v("callback size:" + callbackMap.size + " : " + callbackMap.keys)
        activity.startActivity(intent)
    }

    @JvmOverloads
    @Synchronized
    fun goToFragment(activity: Activity?, fragmentName: String?, bundle: Bundle? = null, callback: ((bundle: Bundle?) -> Unit?)? = null) {
        if (activity == null || activity.isFinishing || fragmentName == null || TextUtils.isEmpty(fragmentName)) {
            FSLogUtil.e("goToFragment failed, activity or fragmentName is invalid !")
            if (callback != null)
                callback(null)
            return
        }

        //该类在atlas组件中
        if (AtlasBundleInfoManager.instance().bundleInfo.bundles.keys.any { fragmentName.startsWith(it) }) {
            val filterList = AtlasBundleInfoManager.instance().getUninstallBundles().filter { fragmentName.startsWith(it) }
            if (filterList.isNotEmpty()) {
                //该组件尚未被安装
                val location = filterList[0]
                //val activity = ActivityTaskMgr.getInstance().peekTopActivity()
                val dialog = RuntimeVariables.alertDialogUntilBundleProcessed(activity, location) ?: throw RuntimeException("alertDialogUntilBundleProcessed can not return null")

                val activityActivitySize = ActivityTaskMgr.getInstance().sizeOfActivityStack()
                val successTask = BundleUtil.CancelableTask(Runnable {
                    FSToastUtil.show(filterList.toString() + " 安装成功")
                    if (activity === ActivityTaskMgr.getInstance().peekTopActivity() || activityActivitySize == ActivityTaskMgr.getInstance().sizeOfActivityStack() + 1) {
                        goToFragmentInternal(activity, fragmentName, bundle, callback)
                    }
                    if (!activity.isFinishing && dialog.isShowing) {
                        dialog.dismiss()
                    }
                })
                val failedTask = BundleUtil.CancelableTask(Runnable {
                    FSToastUtil.show(filterList.toString() + " 安装失败")
                    if (!activity.isFinishing && dialog.isShowing) {
                        dialog.dismiss()
                    }
                })
                dialog.setOnDismissListener {
                    successTask.cancel()
                    failedTask.cancel()
                }
                if (Atlas.getInstance().getBundle(location) == null || Build.VERSION.SDK_INT < 22) {
                    if (!activity.isFinishing && dialog.isShowing) {
                        dialog.show()
                    }
                }
                BundleUtil.checkBundleStateAsync(location, successTask, failedTask)
            } else if (Atlas.getInstance().bundles.any { fragmentName.startsWith(it.location) }) {
                //该组件已经被安装
                goToFragmentInternal(activity, fragmentName, bundle, callback)
            }
        } else {
            goToFragmentInternal(activity, fragmentName, bundle, callback)
        }
    }

    private fun goToFragmentInternal(activity: Activity?, fragmentName: String?, bundle: Bundle? = null, callback: ((bundle: Bundle?) -> Unit?)? = null) {
        val _bundle = bundle ?: Bundle()
        if (callback != null) {
            val id = fragmentName + ":" + System.currentTimeMillis()
            _bundle.putString(KEY_ID_CALLBACK, id)
            callbackMap.put(id, callback)
        }
        FSLogUtil.v("callback size:" + callbackMap.size + " : " + callbackMap.keys)
        FSActivity.start(activity, fragmentName, _bundle)
    }

    fun AtlasBundleInfoManager.getUninstallBundles(): List<String> {
        val installedBundles: List<String> = Atlas.getInstance().bundles.flatMap { listOf(it.location) }
        val allBundles: List<String> = AtlasBundleInfoManager.instance().bundleInfo.bundles.keys.toList()
        FSLogUtil.v("installedBundles:" + installedBundles)
        FSLogUtil.v("allBundles:" + allBundles)
        val uninstallBundles = allBundles.minus(installedBundles)
        FSLogUtil.v("uninstallBundles:" + uninstallBundles)
        FSLogUtil.v("installedBundles:" + installedBundles)
        FSLogUtil.v("allBundles:" + allBundles)
        return uninstallBundles
    }

    @Synchronized
    fun getCallback(fragment: Fragment?): ((bundle: Bundle?) -> Unit?)? = callbackMap[fragment?.arguments?.getString(KEY_ID_CALLBACK)]

    @Synchronized
    fun getCallback(activity: Activity?): ((bundle: Bundle?) -> Unit?)? = callbackMap[activity?.intent?.getStringExtra(KEY_ID_CALLBACK)]

    @Synchronized
    fun removeCallback(fragment: Fragment?) {
        val key = fragment?.arguments?.getString(KEY_ID_CALLBACK)
        if (!TextUtils.isEmpty(key))
            callbackMap.remove(key)
        FSLogUtil.v("callback size:" + callbackMap.size + " : " + callbackMap.keys)
    }

    @Synchronized
    fun removeCallback(activity: Activity?) {
        val key = activity?.intent?.getStringExtra(KEY_ID_CALLBACK)
        if (!TextUtils.isEmpty(key))
            callbackMap.remove(key)
        FSLogUtil.v("callback size:" + callbackMap.size + " : " + callbackMap.keys)
    }

    fun testGoToFragment(activity: Activity, fragmentName: String, bundle: Bundle? = null, callback: ((bundle: Bundle?) -> Unit?)? = null) {

        goToFragment(activity, fragmentName, bundle)
        goToFragment(activity, fragmentName, bundle, callback)
        goToFragment(activity, fragmentName, bundle) { _: Bundle? ->

        }
    }

    fun testGoToActivity(activity: Activity, activityName: String, bundle: Bundle? = null, callback: ((bundle: Bundle?) -> Unit?)? = null) {
        goToActivity(activity, activityName, bundle)
        goToActivity(activity, activityName, bundle, callback)
        goToActivity(activity, activityName, bundle) { _: Bundle? ->

        }
    }
}