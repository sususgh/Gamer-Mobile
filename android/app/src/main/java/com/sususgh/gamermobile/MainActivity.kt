package com.sususgh.gamermobile

import android.app.Activity
import android.os.Bundle
import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 32, 24, 24)
        }
        val scroll = ScrollView(this).apply { addView(content) }

        status = TextView(this).apply {
            text = "等待选择运行模式"
            textSize = 15f
            setPadding(0, 8, 0, 12)
        }

        content.addView(TextView(this).apply {
            text = "Gamer Mobile"
            textSize = 30f
            typeface = Typeface.DEFAULT_BOLD
        })
        content.addView(TextView(this).apply {
            text = "Hybrid AI Game Agent · Prototype 0.1.0"
            textSize = 14f
            setPadding(0, 6, 0, 24)
        })

        content.addView(section("运行模式"))
        content.addView(actionButton("手机本地 AI 模式") {
            status.text = "已选择本地模式。尚未集成模型运行时或模型权重。"
        })
        content.addView(actionButton("电脑 / 服务器远程 AI 模式") {
            status.text = "已选择远程模式。下一步需要配置经过身份验证的推理服务地址。"
        })

        content.addView(section("运行状态"))
        content.addView(status)

        content.addView(section("下一步配置"))
        content.addView(info(
            "1. 屏幕捕获：后续通过 MediaProjection 请求系统授权。\n" +
            "2. AI 推理：接入本地模型适配器或远程推理 API。\n" +
            "3. 动作协议：校验模型输出，再交给用户明确启用的交互层。\n" +
            "4. 安全控制：始终提供可见的停止入口，不绕过系统权限。"
        ))
        content.addView(actionButton("查看无障碍服务设置") {
            status.text = "请在 Android 设置 → 无障碍中，由你自行决定是否启用 Gamer Mobile 服务。"
            startActivity(android.content.Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS))
        })
        content.addView(info(
            "注意：当前版本只是 Android 客户端骨架，不会读取游戏画面、调用 AI 或自动点击。"
        ))

        setContentView(scroll)
    }

    private fun section(value: String) = TextView(this).apply {
        text = value
        textSize = 18f
        typeface = Typeface.DEFAULT_BOLD
        setPadding(0, 22, 0, 10)
    }

    private fun info(value: String) = TextView(this).apply {
        text = value
        textSize = 15f
        setPadding(0, 8, 0, 12)
    }

    private fun actionButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        setOnClickListener { action() }
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = 8 }
    }
}
