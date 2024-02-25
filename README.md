# OpenAutojs ( Free, open source Auto.js )

[简体中文文档](README_zh-CN.md)

## Brief Introduction

A JavaScript runtime and development environment on the Android platform that supports accessibility services, with development goals similar to JsBox and Workflow.  
This project is based on Autox.js to establish the openautojs community, this project aims to allow users to enjoy free and open source Auto .js  

### About OpenAutojs

* Documentation https://openautojs.github.io
* Project Address https://github.com/openautojs/openautojs
* VS Code Extension: https://github.com/openautojs/openautojs-vscode-extension

### Download address：
[https://github.com/openautojs/openautojs/releases](https://github.com/openautojs/openautojs/releases)  

#### APK release notes：
- universal: General version (don't care about the size of the installation package, too lazy to choose this version, including the following 2 CPU architecture so)
- armeabi-v7a: 32-bit ARM device(Standby machine preferred)
- arm64-v8a: 64-bit ARM devices (mainstream flagships)

### Characteristic

1. Easy-to-use automation functions implemented by accessibility services
2. Floating window recording and running
3. More professional & powerful selector API, providing searching, traversing, getting information, operations, etc. for on-screen controls. Similar to Google's UI testing framework Ui Automator, you can also use it as a mobile version of the UI testing framework
4. Using JavaScript as a scripting language, and supporting code completion, variable renaming, code formatting, find and replace functions, etc., it can be used as a JavaScript IDE
5. Support for writing interfaces using e 4x and can package Java Script as apk files that you can use to develop gadget applications
6. Support the use of root privileges to provide more powerful screen click, swipe, record function and run shell commands. Recording can produce JS files or binary files, and the playback of recorded actions is relatively smooth
7. Provide functions such as taking screenshots, saving screenshots, image color finding, and image finding
8. It can be used as a Tasker plug-in, and can be used in combination with Tasker for daily workflows
9. With interface analysis tools, similar to Android Studio's LayoutInspector, you can analyze the level and scope of the interface and obtain the control information on the interface

## License

Based on Git Annotations, code before 2020.7.24 is protected by the original license, code added in 2020.7.24 ~ 2023.2.17 is protected by GPLv2, and code added after 2023.2.17 is protected by GPLv3

This product is licensed under the [GPL-V3](https://opensource.org/license/gpl-3-0/) license,
And [autojs Project](https://github.com/hyb1996/Auto.js) license:

Based on [Mozilla Public License Version 2.0](https://github.com/hyb1996/NoRootScriptDroid/blob/master/LICENSE.md) with the following terms:
Non-Commercial Use — The source code and binary products of this Project and the Projects derived from it may not be used for any commercial or for-profit purposes

JS scripts developed based on this platform are not subject to the above protocols

## About Development：

#### Compilation related：
Command description: Run the command in the project root directory, if using Windows powerShell < 7.0, use the command containing ";"

##### Install the debug build locally to the device：
```shell
./gradlew inrt:assembleTemplateDebug && ./gradlew inrt:cp2APPDebug && ./gradlew app:assembleV6Debug && ./gradlew app:installV6Debug
#or
./gradlew inrt:assembleTemplateDebug ; ./gradlew inrt:cp2APPDebug ; ./gradlew app:assembleV6Debug ; ./gradlew app:installV6Debug
```
The generated debug version APK file is under app/build/outputs/apk/v6/debug with the default signature

##### Compile the release version locally：
```shell
./gradlew inrt:assembleTemplate && ./gradlew inrt:cp2APP && ./gradlew app:assembleV6
#or
./gradlew inrt:assembleTemplate ; ./gradlew inrt:cp2APP ; ./gradlew app:assembleV6
```
The generated APK file is an unsigned APK file. Under app/build/outputs/apk/v6/release, it needs to be signed before it can be installed.

##### Local Android Studio to run the debug build to the device:
First run the following command:

```shell
./gradlew inrt:assembleTemplate && ./gradlew inrt:cp2APP
#or
./gradlew inrt:assembleTemplate ; ./gradlew inrt:cp2APP
```

Then click the Android Studio Run button

##### Local Android Studio compiles and signs the release APK:
First run the following command:

```shell
./gradlew inrt:assembleTemplate && ./gradlew inrt:cp2APP
#or
./gradlew inrt:assembleTemplate ; ./gradlew inrt:cp2APP
```

Then click Android Studio menu "Build" -> "Generate Signed Bundle APK..." -> check "APK"
-> "Next" -> select or create a new certificate -> "Next" -> select "v6Release" -> "Finish"
Generated APK file, under app/v6/release
