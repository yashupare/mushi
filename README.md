# Mushi

A plugin powered music player for Android, iOS, Windows, macOS and Linux, built with Compose
Multiplatform. Music metadata and audio come from plugins; Group Jam lets friends listen to the
same music at the same moment on their own devices.

## Building from source

### 1. Prerequisites

- **JDK 21** to run Gradle (the Gradle daemon JDK and GraalVM are provisioned automatically).
- **Rust** (`rustup` + `cargo`), for the `composeApp` library built
  through [Gobley](https://gobley.dev).
- **Android SDK**, only for Android builds.
- **[ComposeNativeWebview](https://github.com/kdroidFilter/ComposeNativeWebview)**, which isn't on
  Maven Central yet. Clone [KRTirtho/ComposeNativeWebview](https://github.com/KRTirtho/ComposeNativeWebview) at
  commit `b11ac748de1219f9432338c067a2c5dafef3072a` and run `./gradlew publishToMavenLocal` in it.
  Its JNI backends are only built for the host OS.
- **Linux only: runtime libraries, including VLC.** Needed to run the app, including
  `:composeApp:run`:

  | Distro                   | Command                                                                                                                                                          |
  | ------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------- |
  | Debian/Ubuntu            | `sudo apt-get install libvlc5 vlc-plugin-base libwebkit2gtk-4.1-0 libgtk-3-0 libasound2 libgl1 libxtst6 libxi6 libxrender1`                                      |
  | Fedora                   | `sudo dnf install vlc-libs webkit2gtk4.1 gtk3 alsa-lib mesa-libGL libXtst libXi libXrender` (`vlc-libs` needs [RPM Fusion](https://rpmfusion.org/Configuration)) |
  | Arch / CachyOS / Manjaro | `sudo pacman -S vlc webkit2gtk-4.1 gtk3 alsa-lib libglvnd libxtst libxi libxrender`                                                                              |
  | openSUSE                 | `sudo zypper install libvlc5 libwebkit2gtk-4_1-0 libgtk-3-0 libasound2 Mesa-libGL1 libXtst6 libXi6 libXrender1`                                                  |

  On Ubuntu 24.04+/Debian 13, `libgtk-3-0` and `libasound2` are `libgtk-3-0t64` and `libasound2t64`;
  apt resolves this itself.

### 2. Run in development

Desktop (JVM), fastest edit/run loop
```bash
./gradlew :composeApp:run
```

Android debug APK
```bash
./gradlew :composeApp:assembleStableDebug
```
> Use `./gradlew :composeApp:assembleNightlyDebug` for the nightly channel.

Development builds stay fast: the release-only optimizations below are skipped.

### 3. Build the desktop app

A native build takes **about 10 minutes**, uses **every CPU core** and peaks at **~15 GB of RAM**.
It only happens for the tasks below, never for `run`.

**Native toolchain** (Nucleus downloads GraalVM CE on the first build; set `GRAALVM_HOME` to use
your own):

| OS      | Requirements                                                                                                                                                                                                                                                    |
| ------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Linux   | `gcc`, `zlib` headers, `patchelf`; `rpm` for `.rpm`, `fuse` + `zstd` for `.AppImage`. <br/>Debian/Ubuntu: `sudo apt-get install build-essential zlib1g-dev patchelf rpm fuse zstd`. <br/>Fedora: `sudo dnf install gcc zlib-devel patchelf rpm-build fuse zstd` |
| macOS   | Xcode Command Line Tools (`xcode-select --install`)                                                                                                                                                                                                             |
| Windows | Visual Studio Build Tools with "Desktop development with C++" (use an "x64 Native Tools" prompt if `native-image` can't find MSVC)                                                                                                                              |

**Build and run the app folder:**

```bash
./gradlew :composeApp:createGraalvmNativeDistributable
```
> Output folder: composeApp/build/compose/binaries/main/graalvm-app/

Build and launch in one step
```bash
./gradlew :composeApp:runGraalvmNativeDistributable
```

Keep the `mushi` binary next to the libraries it was built with; it loads them from its own
folder.

**Installers** (each OS builds only its own):

| OS               | Command                                                                 |
| ---------------- | ----------------------------------------------------------------------- |
| Linux            | `./gradlew :composeApp:packageGraalvmDeb :composeApp:packageGraalvmRpm` |
| Linux (AppImage) | `./gradlew :composeApp:packageGraalvmAppImage -PappImage=true`          |
| Windows          | `./gradlew :composeApp:packageGraalvmExe` (or `packageGraalvmMsi`)      |
| macOS            | `./gradlew :composeApp:packageGraalvmDmg`                               |
> Output folder: `composeApp/build/compose/binaries/main/graalvm-<format>`

> [!Tip]
> Add `-Pnightly=true` for the nightly channel (separate icons and name).

<details>
<summary>How the Linux AppImage bundle works</summary>

- [`DownloadLinuxGtkBundleTask`](buildSrc/src/main/kotlin/com/mushi/gradle/DownloadLinuxGtkBundleTask.kt)
  downloads the Ubuntu 24.04 packages (audio-only VLC via
  [vlcj-bundler](https://github.com/team-spotube/vlcj-bundler-gradle-plugin), GTK 3, WebKitGTK 4.1,
  glib-networking), keeps only what GTK/WebKit reach and relocates the libraries with `patchelf`.
  Needs `ar`, `tar`, `zstd`, `readelf`, `patchelf`.
- WebKitGTK has its helper-process path compiled in, so
  [`AppImageEnvironment`](composeApp/src/jvmMain/kotlin/com/mushi/app/core/appimage/AppImageEnvironment.kt)
  patches it to a relative path and moves to that directory at startup. It also sets
  `GIO_EXTRA_MODULES` and `WEBKIT_DISABLE_DMABUF_RENDERER=1` (the bundled WebKitGTK 2.44 never
  paints against a newer host Mesa).
- `main()` must call `GraalVmInitializer.initialize()` first: the VLC loader and the environment
  setup read `compose.application.resources.dir`.

</details>

### 4. Native build breaks, JVM works

A native image is a closed world: everything reached through reflection, JNI or JNA (vlcj, Zipline,
NewPipe, ktor, datastore, ...) must be declared at build time. Nucleus generates most of it; the
rest lives in [`composeApp/graalvm/reachability-metadata.json`](composeApp/graalvm/reachability-metadata.json).

Symptoms: `ClassNotFoundException`, `NoSuchMethodException` or `MissingReflectionRegistrationError`
in the native binary, while `:composeApp:run` works.

1. `./gradlew :composeApp:runWithNativeAgent` starts the app on the JVM with GraalVM's tracing agent.
2. Use the broken feature, then **close the window normally**. Ctrl+C discards the recording.
3. Commit the updated JSON in `composeApp/graalvm/` and rebuild.

> [!Tip]
> `./gradlew :composeApp:runGraalvmNative` is a faster debug loop: a quick `-Ob` build that names the
> missing registration.

> [!Warning]
> Keep the hand-written `uk.co.caprica.vlcj.binding.lib.LibVlc` entry, or playback fails to initialize.

### 5. Production vs development builds

Production mode turns on automatically for `package*`, `create*`, `notarize*` and
`nativeImageCompile` tasks. Force it with `-Pproduction=true|false`.

| Area        | Development            | Production                                                                                                               |
| ----------- | ---------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| Rust lib    | fast `debug` profile   | `release` with LTO and stripping (31 MB → 1.8 MB)                                                                        |
| Desktop app | hot-reloadable JVM app | size-optimized (`-Os`) GraalVM native image                                                                              |
| Native libs | all                    | host OS/CPU only; Skia loaded from `lib/`; on Linux ~10 KB `libjvm.so`/`libjava.so` shims instead of the JDK's 28 MB one |

Android picks the Rust profile from its build type.

## Group Jam server

Group Jam uses Mushi's own room server, the `mushi-jam-server` project (a Cloudflare Worker),
deployed at `https://mushi-jam.uddash.workers.dev`. To change it, update `DEFAULT_JAM_SERVER_URL`
in `composeApp/src/commonMain/kotlin/com/mushi/app/modules/settings/SettingsModels.kt`, or
enter another URL in Settings → Group Jam.

## Build settings

Optional Gradle properties (in `gradle.properties` or with `-P`):

| Property              | Effect                                                              |
| --------------------- | ------------------------------------------------------------------- |
| `mushi.repositoryUrl` | Public GitHub repository; turns on the in-app update check.         |
| `mushi.homePageUrl`   | Homepage shown in desktop packages.                                 |
| `mushi.maintainer`    | Maintainer for Linux packages, e.g. `Name <email>`.                 |

## License

Copyright (C) 2026 yashupare and Mushi Contributors.

Mushi is licensed under the [GNU Affero General Public License v3.0 or later](LICENSE). See
[NOTICE](NOTICE) for copyright, attribution and third-party licences.
