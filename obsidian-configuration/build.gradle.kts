plugins {
    java
}

dependencies {
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    implementation(libs.gson)
    implementation(libs.annotations)
//    implementation(libs.yaml)
}