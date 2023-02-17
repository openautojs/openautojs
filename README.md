# OpenAutojs（ 免费、开源的 Auto.js ）

[English Document](README_en.md)

## 简介

一个支持无障碍服务的Android平台上的JavaScript 运行环境 和 开发环境，其发展目标是类似JsBox和Workflow。

本项目基于 Autox.js 成立 openautojs 社区，此项目旨在让用户享受到免费、开源的 Auto.js

### OpenAutojs 相关：

* 文档： https://openautojs.github.io
* 项目地址： https://github.com/openautojs/openautojs
* VS Code 插件: https://github.com/openautojs/openautojs-vscode-extension

### 下载地址：
[https://github.com/openautojs/openautojs/releases](https://github.com/openautojs/openautojs/releases)  
如果下载过慢可以右键复制 Release Assets 中APK文件的链接地址，粘贴到 [http://toolwa.com/github/](http://toolwa.com/github/) 等github加速网站下载

#### APK版本说明：
- universal: 通用版（不在乎安装包大小/懒得选就用这个版本，包含以下2种CPU架构so）
- armeabi-v7a: 32位ARM设备（备用机首选）
- arm64-v8a: 64位ARM设备（主流旗舰机）

### 特性

1. 由无障碍服务实现的简单易用的自动操作函数
2. 悬浮窗录制和运行
3. 更专业&强大的选择器API，提供对屏幕上的控件的寻找、遍历、获取信息、操作等。类似于Google的UI测试框架UiAutomator，您也可以把他当做移动版UI测试框架使用
4. 采用JavaScript为脚本语言，并支持代码补全、变量重命名、代码格式化、查找替换等功能，可以作为一个JavaScript IDE使用
5. 支持使用e4x编写界面，并可以将JavaScript打包为apk文件，您可以用它来开发小工具应用
6. 支持使用Root权限以提供更强大的屏幕点击、滑动、录制功能和运行shell命令。录制录制可产生js文件或二进制文件，录制动作的回放比较流畅
7. 提供截取屏幕、保存截图、图片找色、找图等函数
8. 可作为Tasker插件使用，结合Tasker可胜任日常工作流
9. 带有界面分析工具，类似Android Studio的LayoutInspector，可以分析界面层次和范围、获取界面上的控件信息的

## 关于License

以 Git Annotations 为准  
2020.7.24之前的代码受原许可证保护，2020.7.24 ~ 2023.2.17 新增的代码受 GPLv2 保护，2023.2.17后新增的代码受 GPLv3 保护

本产品采用 [GPL-V3](https://opensource.org/licenses/GPL-2.0) 许可证，
以及 [autojs项目](https://github.com/hyb1996/Auto.js) 的协议：

基于[Mozilla Public License Version 2.0](https://github.com/hyb1996/NoRootScriptDroid/blob/master/LICENSE.md)并附加以下条款：

* **非商业性使用** — 不得将此项目及其衍生的项目的源代码和二进制产品用于任何商业和盈利用途

基于此平台开发的js脚本不受以上协议限制

## 关于开发：
#### 编译相关：
命令说明：在项目根目录下运行命令，如果使用 Windows powerShell < 7.0，请使用包含 ";" 的命令

##### 本地安装调试版本到设备：
```shell
./gradlew inrt:assembleTemplateDebug && ./gradlew inrt:cp2APPDebug && ./gradlew app:assembleV6Debug && ./gradlew app:installV6Debug
#或
./gradlew inrt:assembleTemplateDebug ; ./gradlew inrt:cp2APPDebug ; ./gradlew app:assembleV6Debug ; ./gradlew app:installV6Debug
```
生成的调试版本APK文件在 app/build/outputs/apk/v6/debug 下，使用默认签名

##### 本地编译发布版本：
```shell
./gradlew inrt:assembleTemplate && ./gradlew inrt:cp2APP && ./gradlew app:assembleV6
#或
./gradlew inrt:assembleTemplate ; ./gradlew inrt:cp2APP ; ./gradlew app:assembleV6
```
生成的是未签名的APK文件，在 app/build/outputs/apk/v6/release 下，需要签名后才能安装

##### 本地 Android Studio 运行调试版本到设备：
先运行以下命令：

```shell
./gradlew inrt:assembleTemplate && ./gradlew inrt:cp2APP
#或
./gradlew inrt:assembleTemplate ; ./gradlew inrt:cp2APP
```

再点击 Android Studio 运行按钮

##### 本地 Android Studio 编译发布版本并签名：
先运行以下命令：

```shell
./gradlew inrt:assembleTemplate && ./gradlew inrt:cp2APP
#或
./gradlew inrt:assembleTemplate ; ./gradlew inrt:cp2APP
```

再点击 Android Studio 菜单 "Build" -> "Generate Signed Bundle /APK..." -> 勾选"APK" -> "Next" -> 选择或新建证书 -> "Next" -> 选择"v6Release" -> "Finish"
生成的APK文件，在 app/v6/release 下
