# WatchDog
> 一个微办公自动打卡的小 App。(不再维护)

## 使用条件
1. root
2. 屏幕长亮，不锁定
    - 也就是必须一直插着电源，在 `开发者选项` 中打开 `不锁定屏幕`
3. 常驻进程

## 使用方法
打开 App，设定上下班时间，点击 「确认」，然后点击 「HOME」键回到桌面即可。切记 `不能点击返回或清理后台` 来退出 App。

## 实现原理
ROOT 权限是绕不过的坎，这个 App 的目的是实现自动化打卡，而不是作弊打卡，所以没有通过模拟位置来实现（虽然我也确实不会...）。 当然这样的实现原理也是最强大的，任你打卡应用怎么防作弊都没用，毕竟我就是模仿手工操作实现的。

获取最高权限，后台通过 adb 命令模仿人工操作，达到模拟打卡的目的。

## 最后
欢迎大家提 ISSUS 或者提交代码，后续考虑加入 `钉钉` 、 `企业微信` 等支持。
