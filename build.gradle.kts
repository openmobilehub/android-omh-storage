plugins {
    id("io.github.gradle-nexus.publish-plugin") version Versions.nexusPublishPlugin
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version
            Versions.secretsGradlePlugin apply false
}

val useMavenLocal by extra(getBooleanFromProperties("useMavenLocal", null))
val useLocalProjects by extra(getBooleanFromProperties("useLocalProjects", null))

if (useLocalProjects) {
    println("OMH Storage project running with useLocalProjects enabled")
}

if (useMavenLocal) {
    println(
        "OMH Storage project running with useMavenLocal enabled${
            if (useLocalProjects) ", but only publishing will be altered since dependencies are overriden by useLocalProjects"
            else ""
        } "
    )
}

subprojects {
    if (useMavenLocal) {
        repositories {
            mavenLocal()
            gradlePluginPortal()
            google()
            maven("https://atlas.microsoft.com/sdk/android")
        }
    } else {
        repositories {
            mavenCentral()
            google()
            maven("https://s01.oss.sonatype.org/content/groups/staging/")
        }
    }
}

tasks.register("publishCoreToMavenLocal") {
    dependsOn(
        ":packages:core:assembleRelease",
        ":packages:core:publishToMavenLocal",
    )
}

tasks.register("publishPluginsToMavenLocal") {
    dependsOn(
        ":packages:plugin-googledrive-gms:assembleRelease",
        ":packages:plugin-googledrive-gms:publishToMavenLocal",
        ":packages:plugin-googledrive-non-gms:assembleRelease",
        ":packages:plugin-googledrive-non-gms:publishToMavenLocal",
        ":packages:plugin-onedrive:assembleRelease",
        ":packages:plugin-onedrive:publishToMavenLocal",
        ":packages:plugin-dropbox:assembleRelease",
        ":packages:plugin-dropbox:publishToMavenLocal",
    )
}

tasks.register("installPrePushHook", Copy::class) {
    from("tools/scripts/pre-push")
    into(".git/hooks")
    fileMode = 0b000_111_111_111
}

tasks.register("installPreCommitHook", Copy::class) {
    from("tools/scripts/pre-commit")
    into(".git/hooks")
    fileMode = 0b000_111_111_111
}

tasks {
    val installPrePushHook by existing
    val installPreCommitHook by existing
    getByName("prepareKotlinBuildScriptModel").dependsOn(installPrePushHook)
    getByName("prepareKotlinBuildScriptModel").dependsOn(installPreCommitHook)
}


val publishToReleaseRepository =
    getValueFromEnvOrProperties("publishingSonatypeRepository")?.toString() == "release"

if (!useMavenLocal) {
    println(
        "OMH Storage project configured to publish to Sonatype "
                + (if (publishToReleaseRepository) "release" else "snapshot")
                + " repository"
    )

    if (!publishToReleaseRepository) {
        subprojects {
            version = "$version-SNAPSHOT" // required for publishing to the snapshot repository
        }
    }

    val ossrhUsername = getValueFromEnvOrProperties("OSSRH_USERNAME")
    val ossrhPassword  = getValueFromEnvOrProperties("OSSRH_PASSWORD")
    val mStagingProfileId = getValueFromEnvOrProperties("SONATYPE_STAGING_PROFILE_ID")
    val signingKeyId by extra(getValueFromEnvOrProperties("SIGNING_KEY_ID"))
    val signingPassword by extra(getValueFromEnvOrProperties("SIGNING_PASSWORD"))
    val signingKey by extra(getValueFromEnvOrProperties("SIGNING_KEY"))

    // Set up Sonatype repository
    afterEvaluate {
        nexusPublishing {
            // fix for nexus publishing plugin not picking up the correct version of the published
            // subproject, since this is happening in the root project;
            // see https://github.com/gradle-nexus/publish-plugin/issues/105
            useStaging.set(provider { publishToReleaseRepository })

            repositories {
                sonatype {
                    stagingProfileId.set(mStagingProfileId.toString())
                    username.set(ossrhUsername.toString())
                    password.set(ossrhPassword.toString())
                    // Add these lines if using new Sonatype infra
                    nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
                    snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
                }
            }
        }
    }
}