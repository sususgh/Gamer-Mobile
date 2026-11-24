package com.sususgh.gamermobile

import android.app.Activity
import android.os.Bundle
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.content.res.ColorStateList

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var companion: TextView
    private lateinit var wave: WaveView

    private val ink = Color.rgb(47, 78, 105)
    private val blue = Color.rgb(218, 237, 250)
    private val gold = Color.rgb(213, 181, 116)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(238, 247, 253)
        window.navigationBarColor = Color.rgb(238, 247, 253)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        val scroll = ScrollView(this).apply { setBackgroundColor(Color.rgb(238, 247, 253)) }
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(28))
        }
        scroll.addView(page)

        val header = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
        }
        header.addView(label("☰", 27f, ink), LinearLayout.LayoutParams(dp(44), dp(48)))
        header.addView(label("GAMER-MOBILE", 23f, Color.rgb(91, 130, 160), true), LinearLayout.LayoutParams(0, dp(48), 1f))
        header.addView(label("⋮", 28f, ink), LinearLayout.LayoutParams(dp(32), dp(48)))
        page.addView(header)

        val scene = FantasyScene(this).apply { layoutParams = LinearLayout.LayoutParams(-1, dp(205)).also { it.topMargin = dp(12) } }
        page.addView(scene)

        val welcome = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(16), 0, dp(16))
        }
        val mascotCard = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            background = rounded(Color.WHITE, 28)
            layoutParams = LinearLayout.LayoutParams(dp(116), dp(136))
        }
        companion = label("🧚🏻‍♀️", 57f, ink)
        companion.translationY = 0f
        mascotCard.addView(companion)
        welcome.addView(mascotCard)
        val speech = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(Color.WHITE, 24)
            setPadding(dp(15), dp(16), dp(15), dp(16))
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).also { it.leftMargin = dp(12) }
        }
        speech.addView(label("欢迎回来，旅行者！", 20f, Color.rgb(32, 42, 52), true))
        speech.addView(label("准备好让 AI 帮你开启新的冒险了吗？", 15f, Color.rgb(94, 119, 139)).apply { setPadding(0, dp(6), 0, 0) })
        welcome.addView(speech)
        page.addView(welcome)

        page.addView(section("冒险任务"))
        page.addView(menuCard("▣", "AI 模型管理") { showMessage("模型管理：本版本先提供界面预览，模型运行时尚未接入。") })
        page.addView(menuCard("🎮", "游戏自动代理") { showMessage("游戏代理：屏幕识别与 AI 动作引擎仍在开发中。") })
        page.addView(menuCard("⌁", "实时运行状态") { showMessage("实时状态：连接推理服务后才会显示真实数据。") })

        page.addView(section("冒险通讯终端"))
        val network = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(Color.rgb(255,255,255), 22)
            setPadding(dp(16), dp(14), dp(16), dp(14))
        }
        network.addView(label("网络与 AI 数据流", 17f, ink, true))
        val metrics = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0, dp(10), 0, dp(8)) }
        metrics.addView(metric("延迟", "-- ms"), LinearLayout.LayoutParams(0, dp(56), 1f))
        metrics.addView(metric("上传", "-- MB/s"), LinearLayout.LayoutParams(0, dp(56), 1f))
        metrics.addView(metric("下载", "-- MB/s"), LinearLayout.LayoutParams(0, dp(56), 1f))
        network.addView(metrics)
        wave = WaveView(this).apply { layoutParams = LinearLayout.LayoutParams(-1, dp(76)) }
        network.addView(wave)
        network.addView(label("尚未连接 · 波形将在采集到真实数据后反映实际流量", 11f, Color.GRAY))
        page.addView(network)

        status = label("小伙伴正在待命，等待你的指令。", 14f, ink)
        status.setPadding(dp(4), dp(14), dp(4), dp(8))
        page.addView(status)

        val start = TextView(this).apply {
            text = "开启冒险"
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = rounded(gold, 24)
            setPadding(0, dp(17), 0, dp(17))
            setOnClickListener {
                showMessage("这是首个 UI 预览版本。AI 推理、屏幕捕获和自动操作尚未接通，暂不会接管游戏。")
                companion.text = "🧚🏻‍♀️"
            }
        }
        page.addView(start, LinearLayout.LayoutParams(-1, dp(66)).apply { topMargin = dp(16) })
        page.addView(label("首页预览版本 · AI 自动游戏功能持续开发中", 11f, Color.GRAY).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, 0)
        })

        setContentView(scroll)
        animateCompanion()
        wave.start()
    }

    private fun animateCompanion() {
        companion.animate().translationY(dp(-7).toFloat()).setDuration(1050).withEndAction {
            companion.animate().translationY(dp(7).toFloat()).setDuration(1050).withEndAction { animateCompanion() }.start()
        }.start()
    }

    private fun showMessage(message: String) {
        status.text = message
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun section(text: String) = label(text, 22f, ink, true).apply { setPadding(dp(10), dp(14), 0, dp(10)) }

    private fun menuCard(icon: String, title: String, action: () -> Unit): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = rounded(Color.WHITE, 22)
            setPadding(dp(18), 0, dp(16), 0)
            layoutParams = LinearLayout.LayoutParams(-1, dp(72)).apply { bottomMargin = dp(10) }
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }
        row.addView(label(icon, 23f, Color.rgb(91, 139, 174)), LinearLayout.LayoutParams(dp(42), -2))
        row.addView(label(title, 19f, Color.rgb(34, 42, 50), true))
        row.addView(label("›", 28f, Color.LTGRAY).apply { gravity = Gravity.RIGHT }, LinearLayout.LayoutParams(0, -2, 1f))
        return row
    }

    private fun metric(name: String, value: String) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        addView(label(name, 12f, Color.GRAY))
        addView(label(value, 15f, ink, true))
    }

    private fun label(text: String, size: Float, color: Int, bold: Boolean = false) = TextView(this).apply {
        this.text = text
        textSize = size
        setTextColor(color)
        if (bold) typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER_VERTICAL
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private class FantasyScene(context: android.content.Context) : View(context) {
        private val p = Paint(Paint.ANTI_ALIAS_FLAG)
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat(); val h = height.toFloat()
            p.shader = LinearGradient(0f, 0f, 0f, h, Color.rgb(91,177,230), Color.rgb(213,239,247), Shader.TileMode.CLAMP)
            canvas.drawRoundRect(0f,0f,w,h,dp(22f),dp(22f),p); p.shader = null
            p.color = Color.WHITE; p.alpha = 220
            canvas.drawCircle(w*.22f,h*.18f,h*.12f,p); canvas.drawCircle(w*.31f,h*.16f,h*.15f,p); canvas.drawCircle(w*.40f,h*.19f,h*.10f,p)
            p.color = Color.rgb(116,164,190)
            val mountains = Path().apply { moveTo(0f,h*.65f); lineTo(w*.25f,h*.34f); lineTo(w*.43f,h*.62f); lineTo(w*.68f,h*.29f); lineTo(w,h*.67f); lineTo(w,h); lineTo(0f,h); close() }
            canvas.drawPath(mountains,p)
            p.color = Color.rgb(104,174,139)
            val hills = Path().apply { moveTo(0f,h*.72f); quadTo(w*.25f,h*.48f,w*.48f,h*.75f); quadTo(w*.75f,h*.53f,w,h*.72f); lineTo(w,h); lineTo(0f,h); close() }
            canvas.drawPath(hills,p)
            p.color = Color.rgb(76,157,190); canvas.drawOval(w*.48f,h*.70f,w*.94f,h*.96f,p)
            p.color = Color.rgb(255,255,255); p.textSize = dp(13f); canvas.drawText("A NEW ADVENTURE AWAITS", dp(15f), h-dp(16f), p)
        }
        private fun dp(v: Float) = v * resources.displayMetrics.density
    }

    private class WaveView(context: android.content.Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 3f }
        private var phase = 0f
        fun start() { post(object : Runnable { override fun run() { phase += .12f; invalidate(); postDelayed(this, 50) } }) }
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat(); val h = height.toFloat()
            for (line in 0..1) {
                paint.color = if (line == 0) Color.rgb(77,174,216) else Color.rgb(159,139,207)
                paint.alpha = 230
                val path = Path()
                for (x in 0..w.toInt() step 4) {
                    val y = h*.5f + kotlin.math.sin(x*.045f + phase + line*1.5f).toFloat() * (if (line==0) 13f else 8f)
                    if (x == 0) path.moveTo(x.toFloat(),y) else path.lineTo(x.toFloat(),y)
                }
                canvas.drawPath(path,paint)
            }
        }
    }
}
