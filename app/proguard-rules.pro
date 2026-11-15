# Add project specific ProGuard rules here.
# Keep line numbers and source file info for crash diagnostics
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Preserve annotations, generics, and Kotlin metadata for reflection-based libraries
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,Exceptions

# Keep all Firestore / domain models and no-arg constructors for toObject() / toObjects()
-keep class com.example.data.model.** { *; }
-keepclassmembers class com.example.data.model.** {
    <init>(...);
    *;
}
-keep @com.google.firebase.firestore.IgnoreExtraProperties class * { *; }
-keepclassmembers class * {
    @com.google.firebase.firestore.DocumentId *;
    @com.google.firebase.firestore.ServerTimestamp *;
    @com.google.firebase.firestore.PropertyName *;
    @com.google.firebase.firestore.Exclude *;
}

# Keep Room entities & DAOs
-keep class com.example.data.local.** { *; }

# Keep Moshi & Retrofit API models
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}

# Keep Credential Manager / Google Identity classes
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }

# Avoid R8 warnings on optional annotation / JDK references in third-party libraries
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn java.lang.invoke.StringConcatFactory
