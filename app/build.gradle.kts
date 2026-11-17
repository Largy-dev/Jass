import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Signature release : fichier keystore.properties HORS du dépôt.
// Chemin : variable d'env JASS_SIGNING_PROPERTIES, sinon ~/.jass-signing/keystore.properties.
// Absent → le build release est produit non signé (le debug reste utilisable).
val signingProps: Properties? = (System.getenv("JASS_SIGNING_PROPERTIES")
    ?: "${System.getProperty("user.home")}/.jass-signing/keystore.properties")
    .let(::file)
    .takeIf { it.isFile }
    ?.let { f -> Properties().apply { f.inputStream().use(::load) } }

// versionCode croissant à chaque build CI (numéro de run GitHub), sinon valeur locale.
val ciVersionCode: Int? = System.getenv("VERSION_CODE")?.toIntOrNull()

android {
    namespace = "com.largy.jass"
    compileSdk = 36

    defaultConfig {
        // Nom de package imposé par la fiche Play Console (le namespace du code reste com.largy.jass)
        applicationId = "com.jass.largy"
        minSdk = 26
        targetSdk = 36
        versionCode = ciVersionCode ?: 2
        versionName = "2.0.${ciVersionCode ?: 0}"
    }

    signingConfigs {
        if (signingProps != null) {
            create("release") {
                storeFile = file(signingProps.getProperty("storeFile"))
                storePassword = signingProps.getProperty("storePassword")
                keyAlias = signingProps.getProperty("keyAlias")
                keyPassword = signingProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)
    testImplementation(libs.junit)
}
