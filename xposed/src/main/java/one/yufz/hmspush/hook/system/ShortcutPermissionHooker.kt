package one.yufz.hmspush.hook.system

import android.app.AndroidAppHelper
import android.content.pm.ShortcutInfo
import android.os.Binder
import one.yufz.hmspush.common.HMS_PACKAGE_NAME
import one.yufz.xposed.HookCallback
import one.yufz.xposed.MethodHookParam
import one.yufz.xposed.findMethodExact
import one.yufz.xposed.hook

object ShortcutPermissionHooker {
    private fun fromHms() = try {
        Binder.getCallingUid() == AndroidAppHelper.currentApplication().packageManager.getPackageUid(HMS_PACKAGE_NAME, 0)
    } catch (e: Throwable) {
        false
    }

    private fun tryHookPermission(packageName: String): Boolean {
        if (fromHms()) {
            Binder.clearCallingIdentity()
            return true
        }
        return false
    }

    private fun hookPermission(targetPackageNameParamIndex: Int, hookExtra: (MethodHookParam.() -> Unit)? = null): HookCallback = {
        doBefore {
            if (tryHookPermission(args[targetPackageNameParamIndex] as String)) {
                hookExtra?.invoke(this)
            }
        }
    }

    fun hook(classShortcutService: Class<*>) {
        //    void pushDynamicShortcut(String packageName, in ShortcutInfo shortcut, int userId);
        classShortcutService.findMethodExact("pushDynamicShortcut", String::class.java, ShortcutInfo::class.java, Int::class.java)
            .hook(hookPermission(0))

        //    int getMaxShortcutCountPerActivity(String packageName, int userId);
        classShortcutService.findMethodExact("getMaxShortcutCountPerActivity", String::class.java, Int::class.java)
            .hook(hookPermission(0))

        //    void verifyCaller(@NonNull String packageName, @UserIdInt int userId)
        classShortcutService.findMethodExact("verifyCaller", String::class.java, Int::class.java)
            .hook(hookPermission(0))
    }
}