# ProGuard / R8 规则：release 以 proguard-android-optimize.txt 为基线，开 minify + shrinkResources。
#
# 不用 -dontshrink（官方明示的反模式，直接关闭 R8 压缩）。R8 默认删未引用代码换体积，
# 反射/序列化/SDK 回调等被误删时，用**精确** -keep 规则保留（不要整包 -keep，也不要禁用 shrink）。
#
# AGP 9 严格模式（android.r8.strictFullModeForKeepRules 默认 true）：
# `-keep class A` 不再隐含保留默认构造器。若 A 靠无参构造被反射实例化（Gson/Jackson/某些 SDK），
# 必须显式写 `-keep class A { <init>(); }`，否则运行期 NoSuchMethodException/实例化失败。
#
# 常用精确模板（按需取消注释并改包名）：
#
# 数据模型不被混淆（Gson/Jackson 反射读字段名时必需；kotlinx-serialization 编译期生成，通常不需要）
# 反射无参构造场景补 { <init>(); }（见上方 AGP 9 严格模式说明）
# -keep class io.github.mymx2.android.data.model.** { *; }
#
# 所有 Parcelable
# -keep class * implements android.os.Parcelable { *; }
#
# 所有 Serializable
# -keepclassmembers class * implements java.io.Serializable {
#   static final long serialVersionUID;
#   private static final java.io.ObjectStreamField[] serialPersistentFields;
#   private void writeObject(java.io.ObjectOutputStream);
#   private void readObject(java.io.ObjectInputStream);
#   java.lang.Object writeReplace();
#   java.lang.Object readResolve();
# }
#
# 枚举不被混淆（反射 valueOf 时必需）
# -keepclassmembers enum * {
#   public static **[] values();
#   public static ** valueOf(java.lang.String);
# }
#
# 第三方 SDK 回调接口（按实际包名改）
# -keep interface com.some.sdk.** { *; }
