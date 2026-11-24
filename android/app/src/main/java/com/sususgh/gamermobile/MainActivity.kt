package com.sususgh.gamermobile

import android.app.Activity
import android.os.Bundle
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlin.math.sin

class MainActivity : Activity() {
    private val ink = Color.rgb(48, 82, 111)
    private val muted = Color.rgb(105, 132, 153)
    private val gold = Color.rgb(213, 181, 116)
    private lateinit var mascot: MascotView
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(238, 247, 253)
        window.navigationBarColor = Color.rgb(238, 247, 253)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.rgb(238, 247, 253))
            clipToPadding = false
        }
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(12), dp(18), dp(28))
        }
        scroll.addView(page)

        // Header: menu, centered brand, edit and overflow controls.
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        header.addView(roundIcon("☰", 48) { showToast("导航菜单") })
        header.addView(TextView(this).apply {
            text = "GAMER-MOBILE"
            textSize = 23f
            setTextColor(Color.rgb(177, 193, 205))
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(0, dp(52), 1f))
        header.addView(roundIcon("✎", 48) { showToast("编辑冒险主页") })
        header.addView(roundIcon("⋮", 42) { showToast("更多设置") })
        page.addView(header)

        // Scenic fantasy banner, custom-drawn so the app does not bundle game artwork.
        page.addView(FantasyBanner(this), LinearLayout.LayoutParams(-1, dp(188)).apply {
            topMargin = dp(12)
        })

        // Companion card and speech bubble.
        val welcome = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(14), 0, dp(14))
        }
        val mascotCard = FrameLayout(this).apply {
            background = rounded(Color.WHITE, 26)
            layoutParams = LinearLayout.LayoutParams(dp(126), dp(142))
            clipToOutline = true
        }
        mascot = MascotView(this)
        mascotCard.addView(mascot, FrameLayout.LayoutParams(-1, -1))
        welcome.addView(mascotCard)

        val speech = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            background = rounded(Color.WHITE, 24)
            setPadding(dp(16), dp(14), dp(14), dp(14))
            layoutParams = LinearLayout.LayoutParams(0, dp(120), 1f).apply { leftMargin = dp(12) }
        }
        speech.addView(TextView(this).apply {
            text = "欢迎回来，旅行者！"
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(25, 31, 38))
        })
        speech.addView(TextView(this).apply {
            text = "准备好让 AI 帮你开启新的冒险了吗？"
            textSize = 15f
            setTextColor(muted)
            setLineSpacing(dp(3).toFloat(), 1f)
            setPadding(0, dp(5), 0, 0)
        })
        welcome.addView(speech)
        page.addView(welcome)

        // Adventure task panel, matching the supplied screenshot's blue inset panel.
        val taskPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(Color.rgb(213, 233, 249), 24)
            setPadding(dp(14), dp(14), dp(14), dp(8))
        }
        taskPanel.addView(TextView(this).apply {
            text = "冒险任务"
            textSize = 25f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(ink)
            setPadding(dp(4), 0, 0, dp(8))
        })
        taskPanel.addView(taskCard("▣", "AI 模型管理") {
            showToast("模型管理正在开发中，后续可配置本地模型和远程推理服务。")
        })
        taskPanel.addView(taskCard("⌘", "游戏自动代理") {
            showToast("游戏自动代理尚未接通屏幕识别与 AI 动作引擎。")
        })
        taskPanel.addView(taskCard("▱", "实时运行状态") {
            showToast("连接真实推理服务后，这里将显示运行状态和网络指标。")
        })
        page.addView(taskPanel)

        val start = TextView(this).apply {
            text = "开启冒险"
            textSize = 23f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.rgb(222, 195, 133), gold, Color.rgb(204, 169, 99))
            ).apply { cornerRadius = dp(22).toFloat() }
            elevation = dp(2).toFloat()
            setOnClickListener {
                mascot.setMood(1)
                status.text = "小伙伴：旅行者，AI 冒险引擎还在准备中！"
                showToast("当前为 UI 预览版，尚不会自动操作游戏。")
            }
        }
        page.addView(start, LinearLayout.LayoutParams(-1, dp(68)).apply {
            topMargin = dp(14)
        })

        // Keep the requested live telemetry concept below the screenshot-inspired home.
        val dataTitle = TextView(this).apply {
            text = "冒险通讯终端"
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(ink)
            setPadding(dp(8), dp(18), 0, dp(8))
        }
        page.addView(dataTitle)
        val dataCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(Color.WHITE, 22)
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }
        dataCard.addView(TextView(this).apply {
            text = "实时网络数据流"
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(ink)
        })
        val metrics = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        metrics.addView(metric("延迟", "-- ms"), LinearLayout.LayoutParams(0, dp(54), 1f))
        metrics.addView(metric("上传", "-- MB/s"), LinearLayout.LayoutParams(0, dp(54), 1f))
        metrics.addView(metric("下载", "-- MB/s"), LinearLayout.LayoutParams(0, dp(54), 1f))
        dataCard.addView(metrics)
        dataCard.addView(WaveView(this), LinearLayout.LayoutParams(-1, dp(62)))
        dataCard.addView(TextView(this).apply {
            text = "未连接数据源 · 当前波形仅为界面动效，不代表真实流量"
            textSize = 11f
            setTextColor(Color.GRAY)
        })
        page.addView(dataCard)

        status = TextView(this).apply {
            text = "小伙伴正在待命，等待你的指令。"
            textSize = 13f
            setTextColor(muted)
            setPadding(dp(4), dp(12), dp(4), 0)
        }
        page.addView(status)

        setContentView(scroll)
        mascot.startFloating()
    }

    private fun roundIcon(symbol: String, size: Int, action: () -> Unit): View {
        return TextView(this).apply {
            text = symbol
            textSize = 25f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(22, 28, 34))
            background = rounded(Color.WHITE, 50)
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
            layoutParams = LinearLayout.LayoutParams(dp(size), dp(48)).apply {
                if (symbol != "☰") leftMargin = dp(4)
            }
        }
    }

    private fun taskCard(icon: String, title: String, action: () -> Unit): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = rounded(Color.WHITE, 20)
            setPadding(dp(18), 0, dp(14), 0)
            layoutParams = LinearLayout.LayoutParams(-1, dp(72)).apply {
                bottomMargin = dp(9)
            }
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }
        row.addView(TextView(this).apply {
            text = icon
            textSize = 23f
            setTextColor(Color.rgb(91, 139, 174))
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(dp(42), -1))
        row.addView(TextView(this).apply {
            text = title
            textSize = 19f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(28, 33, 39))
            gravity = Gravity.CENTER_VERTICAL
        })
        return row
    }

    private fun metric(name: String, value: String) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_VERTICAL
        addView(TextView(this@MainActivity).apply {
            text = name
            textSize = 12f
            setTextColor(Color.GRAY)
        })
        addView(TextView(this@MainActivity).apply {
            text = value
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(ink)
        })
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun showToast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    // Original fantasy landscape illustration drawn locally; no copied game screenshots/assets.
    private class FantasyBanner(context: android.content.Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat()
            val h = height.toFloat()
            val save = canvas.save()
            val clip = Path().apply { addRoundRect(0f, 0f, w, h, dp(22f), dp(22f), Path.Direction.CW) }
            canvas.clipPath(clip)
            paint.shader = LinearGradient(0f, 0f, 0f, h, Color.rgb(86, 177, 231), Color.rgb(218, 241, 249), Shader.TileMode.CLAMP)
            canvas.drawRect(0f, 0f, w, h, paint)
            paint.shader = null
            paint.color = Color.WHITE
            paint.alpha = 225
            canvas.drawCircle(w*.20f, h*.19f, h*.12f, paint)
            canvas.drawCircle(w*.29f, h*.16f, h*.16f, paint)
            canvas.drawCircle(w*.40f, h*.20f, h*.11f, paint)
            canvas.drawCircle(w*.72f, h*.12f, h*.08f, paint)
            paint.alpha = 255
            paint.color = Color.rgb(103, 154, 190)
            val peaks = Path().apply {
                moveTo(0f,h*.67f); lineTo(w*.18f,h*.38f); lineTo(w*.29f,h*.55f)
                lineTo(w*.48f,h*.30f); lineTo(w*.64f,h*.58f); lineTo(w*.82f,h*.35f)
                lineTo(w,h*.62f); lineTo(w,h); lineTo(0f,h); close()
            }
            canvas.drawPath(peaks, paint)
            paint.color = Color.rgb(94, 174, 137)
            val hills = Path().apply {
                moveTo(0f,h*.69f); quadTo(w*.22f,h*.53f,w*.44f,h*.72f)
                quadTo(w*.69f,h*.53f,w,h*.68f); lineTo(w,h); lineTo(0f,h); close()
            }
            canvas.drawPath(hills, paint)
            paint.color = Color.rgb(73, 157, 190)
            canvas.drawOval(w*.48f,h*.69f,w*.93f,h*.94f,paint)
            paint.color = Color.rgb(238, 214, 132)
            for (i in 0..34) {
                val x = (i * 37 % 997) / 997f * w
                val y = h * (.70f + (i % 7) * .035f)
                canvas.drawCircle(x, y, dp(1.2f), paint)
            }
            paint.color = Color.WHITE
            paint.textSize = dp(11f)
            paint.setShadowLayer(dp(3f), 0f, dp(1f), Color.BLACK)
            canvas.drawText("A WORLD OF ADVENTURE", dp(14f), h-dp(14f), paint)
            paint.clearShadowLayer()
            canvas.restoreToCount(save)
        }
        private fun dp(v: Float) = v * resources.displayMetrics.density
    }

    // A small original star-fairy mascot with a gentle floating idle animation.
    private class MascotView(context: android.content.Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var phase = 0f
        private var mood = 0
        private val runner = object : Runnable {
            override fun run() {
                phase += .08f
                invalidate()
                postDelayed(this, 45)
            }
        }
        fun startFloating() { post(runner) }
        fun setMood(value: Int) { mood = value; invalidate() }
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val cx = width/2f
            val cy = height*.49f + sin(phase).toFloat()*dp(4f)
            val s = minOf(width, height).toFloat()
            // tiny golden halo
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dp(2f)
            paint.color = Color.rgb(219, 190, 123)
            canvas.drawOval(cx-s*.19f, cy-s*.39f, cx+s*.19f, cy-s*.31f, paint)
            paint.style = Paint.Style.FILL
            // cape / little body
            paint.color = Color.rgb(76, 111, 145)
            val cape = Path().apply {
                moveTo(cx-s*.13f,cy+s*.13f); quadTo(cx-s*.32f,cy+s*.36f,cx-s*.04f,cy+s*.42f)
                quadTo(cx+s*.20f,cy+s*.34f,cx+s*.14f,cy+s*.13f); close()
            }
            canvas.drawPath(cape,paint)
            // hair
            paint.color = Color.rgb(242, 235, 220)
            canvas.drawCircle(cx,cy-s*.10f,s*.22f,paint)
            // face
            paint.color = Color.rgb(255, 224, 190)
            canvas.drawOval(cx-s*.15f,cy-s*.18f,cx+s*.15f,cy+s*.13f,paint)
            // hair cap and side locks
            paint.color = Color.rgb(250, 247, 237)
            canvas.drawCircle(cx,cy-s*.20f,s*.17f,paint)
            canvas.drawOval(cx-s*.25f,cy-s*.17f,cx-s*.13f,cy+s*.15f,paint)
            canvas.drawOval(cx+s*.13f,cy-s*.17f,cx+s*.25f,cy+s*.15f,paint)
            // eyes
            paint.color = Color.rgb(49, 82, 112)
            canvas.drawOval(cx-s*.085f,cy-s*.035f,cx-s*.035f,cy+s*.055f,paint)
            canvas.drawOval(cx+s*.035f,cy-s*.035f,cx+s*.085f,cy+s*.055f,paint)
            // star ornament
            paint.color = Color.rgb(225, 190, 112)
            val star = Path().apply {
                moveTo(cx,cy-s*.34f); lineTo(cx+s*.035f,cy-s*.27f); lineTo(cx+s*.10f,cy-s*.26f)
                lineTo(cx+s*.05f,cy-s*.21f); lineTo(cx+s*.06f,cy-s*.15f)
                lineTo(cx,cy-s*.19f); lineTo(cx-s*.06f,cy-s*.15f)
                lineTo(cx-s*.05f,cy-s*.21f); lineTo(cx-s*.10f,cy-s*.26f)
                lineTo(cx-s*.035f,cy-s*.27f); close()
            }
            canvas.drawPath(star,paint)
            if (mood == 1) {
                paint.color = Color.rgb(221, 166, 115)
                canvas.drawCircle(cx,cy+s*.08f,dp(3f),paint)
            }
        }
        private fun dp(v: Float) = v * resources.displayMetrics.density
    }

    private class WaveView(context: android.content.Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 2.5f }
        private var phase = 0f
        private val runner = object : Runnable {
            override fun run() { phase += .12f; invalidate(); postDelayed(this, 50) }
        }
        override fun onAttachedToWindow() { super.onAttachedToWindow(); post(runner) }
        override fun onDetachedFromWindow() { removeCallbacks(runner); super.onDetachedFromWindow() }
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat()
            val h = height.toFloat()
            for (line in 0..1) {
                paint.color = if (line == 0) Color.rgb(77,174,216) else Color.rgb(159,139,207)
                val path = Path()
                for (x in 0..w.toInt() step 4) {
                    val y = h*.5f + sin(x*.045f + phase + line*1.5f).toFloat() * (if (line == 0) 13f else 8f)
                    if (x == 0) path.moveTo(x.toFloat(), y) else path.lineTo(x.toFloat(), y)
                }
                canvas.drawPath(path, paint)
            }
        }
    }
}
