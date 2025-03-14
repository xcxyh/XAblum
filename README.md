# XAlbum - Android 相册选择器

XAlbum 是一个基于 Jetpack Compose 实现的 Android 相册选择器，在 Cursor IDE & Claude AI 的辅助下开发完成。项目采用 MVI 架构模式，支持图片和视频的选择，具有流畅的动画效果和友好的用户界面。

![](C:\Users\Administrator\Desktop\Screenshot_20250222_180310.png)

## 功能特点

- 支持图片和视频选择
- 支持多选操作
- 文件夹分类浏览
- 渐进式加载媒体文件
- 流畅的动画效果
- Material Design 3 风格界面
- 适配 Android 10+ 存储权限

## 技术栈

- Kotlin
- Jetpack Compose
- Coroutines & Flow
- Koin (依赖注入)
- Coil (图片加载)
- MVI 架构模式
- Material Design 3

## 主要特性实现

### 渐进式加载

使用 Flow 实现媒体文件的批量加载，提升用户体验：
- 首次加载 200 个文件
- 后续批次大小翻倍（最大 800）
- 实时更新界面

### 动画效果

- 文件夹下拉菜单：展开/收起动画
- 选择状态：淡入淡出过渡
- 列表滚动：平滑过渡

### 权限适配

适配 Android 10+ 的存储权限：
- Android 10+: READ_MEDIA_IMAGES, READ_MEDIA_VIDEO
- Android 10 以下: READ_EXTERNAL_STORAGE

## 开发过程
本项目在 Cursor IDE & Claude AI 的辅助下完成开发，主要包括：
- 架构设计建议
- 代码实现指导
- 问题排查和优化
- 最佳实践建议

## 待优化项

- [ ] 支持自定义主题
- [ ] 添加预览功能
- [ ] 支持图片裁剪
- [ ] 添加单元测试
- [ ] 性能优化

## License

MIT License

Copyright (c) 2024 XCC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.