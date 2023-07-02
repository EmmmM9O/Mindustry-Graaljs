# Mindustry-Graal.js
[英文](https://gitee.com/novarc/Mindustry-Graaljs/blob/master/README.md)
### 在MDT里运行Graal.js
使用ES2022JS标准 
新增指令 `gjs <代码...>`来运行
新增指令 `gja <行动> [参数...] `来配置
新增类 `JsEnv`作为启动js的环境
> 在构建时 包含了graal.js 和graalvm
### 构建
- 从Action下载
- 或者自行运行 `./gradlew jar`
### 作为`Library`使用
1. 添加代码 `compileOnly 'com.gitee.novarc:Mindustry-Graaljs:v1.1.1'` 进入build.gradle
2. 把这个mod加入mod.hjson的依赖列表