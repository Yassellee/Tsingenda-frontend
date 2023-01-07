# Tsingenda-Frontend

`Tsingenda`是一款由AI驱动的日程管理助手，致力于**深挖日程相关情景，融合人工智能技术**来提供日程的识别、创建和管理的自动化，带给用户全新的日程管理体验。其主要功能包括：

1. 从多种情境中提取信息：截屏、粘贴板、短信……
2. 从上述信息中准确地识别出日程并做出相应的处理
3. 根据用户反馈不断提高识别的准确性

本仓库收纳了`Tsingenda`项目的前端代码。

# Framework Introduction

## 1. 用户系统

为了向不同用户提供更精准的个性化推荐功能，`Tsingenda`实现了用户管理系统，用于集成用户对账号的登录、注册、修改密码等功能。

该部分代码主要位于`com.example.calendarfrontend`下的`LoginActivity`、`RegisterActivity`、`ChangepwdActivity`中。

## 2. 界面设计

作为一款日程管理助手，`Tsingenda`支持系统原生日历包含的绝大多数功能。其主体的日历视图基于`com.haibin:calendarview`实现，具备极强的可拓展性，可以轻松实现多种视图的设计与切换；同时，基于`com.github.li-xiaojun:XPopup`，`Tsingenda`实现了多种弹窗用于封装日程管理、日期跳转等功能。

日历视图部分对应的代码主要位于 package `com.example.calendarfrontend`下`CalendarMonthView`、`CalendarWeekView`中；其余的日程管理、日期跳转、退出账号等功能则位于`MainActicity`中。

## 3. 日程管理

`Tsingenda`实现了`Scheme`这一`Bean`类用于存储日程的时间、地点、标题、起止时间等信息，并使用`sqlite3`作为数据库管理日程信息，支持对日程的增删改查等操作。

该部分代码主要位于`com.example.calendarfrontend`包下的`Scheme`、`SchemeAdapter`、`DbHandler`中。

## 4. 情境监听

`Tsingenda`目前支持对系统截屏、剪贴板内容、短信通知的监听服务，用于从这类情境中提取出潜在的日程信息。其中，截屏与剪贴板内容的获取部分对应的代码位于`com.example.calendarfrontend`包下的`MainActicity`中，短信监听的代码位于`SmsReceiver`中。

此外，我们还尝试了使用无障碍服务监听微信的聊天记录，但由于微信自身的混淆技术最终未能实现；接着我们又尝试了监听QQ或企业微信的聊天记录，虽能获取到界面内容，但在适配不同机型时出现未知错误，该部分代码位于`QQListenerService`中。

## 5. 弹窗反馈

为了根据用户反馈不断提高识别的准确性，`Tsingenda`在检测到日程信息后可能会给出弹窗让用户对后端提取出的日程信息进行编辑与确认，此外用户也可以自行对已有的日程进行增删改查操作。为此我们基于`XPopup`设计并实现了多种弹窗用于反馈。

该部分代码主要位于`com.example.calendarfrontend`包下的`AddSchemePopup`、`ModifySchemePopup`、`PopupWindow`中。

## 6. 前后端通信

基于`okhttp3`实现了前后端通信功能，同时为了维护前后端通信的会话，我们引入了cookie管理机制。

该部分代码主要位于`com.example.calendarfrontend`包下的`MainActivity`、`CookieJarManager`中。

## 7. 代码调试

在`Tsingenda`的内测环节，由于一些bug存在极大的随机性，且可能与机型和和用户操作强相关，为此我们引入了`bugly`进行远程管理。

该部分代码主要位于`com.example.calendarfrontend`包下的`MainActivity`中。

# Effect Preview

下面是`Tsingenda`部分功能的效果展示，测试机型为realme GT Neo2，系统型号为android 12。

![login_and_clip](./images/login_and_clip.gif)		![message](./images/message.gif)		![screenshot](./images/screenshot.gif)	

# Instructions for use

为保证软件的正常运行，需要用户为`Tsingenda`提供**悬浮窗、访问存储空间、访问短信内容**等权限。

进入软件后，与正常日历界面相似，点击右上角的设置与搜索按钮可以进行"跳转到指定日期"、"查询指定日程"、"退出账号"等基本功能。日历下方会展示当天的日程，点击可进行编辑，长按可以删除日程。

当`Tsingenda`从截屏、短信、剪贴板等来源中捕获到日程信息时，当该日程的置信度足够高会直接加入日程表，否则会给出弹窗供用户进行编辑与判断，基本效果如上方视频所示。

