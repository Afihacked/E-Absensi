#################################################
## 🔐 FIREBASE
#################################################

# Firestore model
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <methods>;
}

-keepattributes *Annotation*

# Firebase Auth
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

#################################################
## 🧭 ANDROIDX NAVIGATION (SafeArgs)
#################################################

-keep class androidx.navigation.** { *; }
-keepclassmembers class * implements androidx.navigation.NavDirections { *; }

#################################################
## 📦 DATA MODELS (WAJIB utk Firestore parsing)
#################################################

-keep class com.afitech.absensi.data.model.** { *; }

#################################################
## 📍 GOOGLE LOCATION & PLACES
#################################################

-keep class com.google.android.gms.location.** { *; }
-keep class com.google.android.libraries.places.** { *; }
-dontwarn com.google.android.gms.**
-dontwarn com.google.android.libraries.places.**

#################################################
## 🖼 EXIF
#################################################

-keep class androidx.exifinterface.** { *; }

#################################################
## 🔥 VIEWBINDING (BIAR TIDAK ERROR)
#################################################

-keep class **Binding { *; }

#################################################
## 🧱 KOTLIN (IMPORTANT)
#################################################

-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

#################################################
## 🛠 GENERAL ANDROID SAFE RULES
#################################################

-keepclassmembers class * extends android.app.Activity { *; }
-keepclassmembers class * extends androidx.fragment.app.Fragment { *; }
-keepclassmembers class * extends android.view.View { *; }

#################################################
## 🔥 REMOVE LOGS IN RELEASE
#################################################

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
