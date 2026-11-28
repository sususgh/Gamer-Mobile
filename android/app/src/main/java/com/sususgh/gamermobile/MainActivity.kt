package com.sususgh.gamermobile

import android.app.Activity
import android.app.AlertDialog
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.speech.tts.TextToSpeech
import android.os.Build
import java.util.Locale
import android.os.Bundle
import android.graphics.*
import android.graphics.Movie
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
    private lateinit var mascot: PaimonView
    private lateinit var status: TextView
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var ttsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        textToSpeech = TextToSpeech(this) { result ->
            ttsReady = result == TextToSpeech.SUCCESS
            if (ttsReady) textToSpeech?.language = Locale.SIMPLIFIED_CHINESE
        }
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
        header.addView(roundIcon("▦", 48) { startQrScanner() }.apply {
            contentDescription = "扫描电脑端配对二维码"
            isFocusable = true
        })
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
        mascot = PaimonView(this)
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

        // Push-to-talk voice companion: the user explicitly taps the microphone.
        val voicePanel = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = rounded(Color.WHITE, 20)
            setPadding(dp(14), dp(10), dp(14), dp(10))
        }
        val voiceCopy = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        voiceCopy.addView(TextView(this).apply {
            text = "和小伙伴说句话"
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(ink)
        })
        voiceCopy.addView(TextView(this).apply {
            text = "点击麦克风，说出你的游戏任务"
            textSize = 12f
            setTextColor(muted)
        })
        voicePanel.addView(voiceCopy, LinearLayout.LayoutParams(0, -2, 1f))
        voicePanel.addView(TextView(this).apply {
            text = "🎙"
            textSize = 25f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = rounded(Color.rgb(75, 132, 184), 30)
            setPadding(dp(15), dp(9), dp(15), dp(9))
            isClickable = true
            isFocusable = true
            contentDescription = "点击开始语音识别"
            setOnClickListener { startVoiceInput() }
        })
        page.addView(voicePanel, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) })

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
        taskPanel.addView(taskCard("▦", "扫描电脑端配对二维码") {
            startQrScanner()
        })
        taskPanel.addView(taskCard("▣", "AI 模型管理") {
            showModelSetupDialog()
        })
        taskPanel.addView(taskCard("♫", "打开派蒙语音对话") {
            showPaimonVoiceDialog()
        })
        taskPanel.addView(taskCard("⌘", "自动识别当前游戏") {
            showGameAutoDialog()
        })
        taskPanel.addView(taskCard("▱", "实时运行状态") {
            showToast("连接真实推理服务后，这里将显示运行状态和网络指标。")
        })
        page.addView(taskPanel)

        // World rules: survival is the default; creative remains an explicit alternative.
        val worldPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(Color.WHITE, 20)
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }
        worldPanel.addView(TextView(this).apply {
            text = "Minecraft 世界模式"
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(ink)
        })
        worldPanel.addView(TextView(this).apply {
            text = "仅在识别到《我的世界》时生效：生存 / 创造模式"
            textSize = 12f
            setTextColor(muted)
            setPadding(0, dp(4), 0, dp(9))
        })
        val modeChoices = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        val survivalChoice = TextView(this).apply {
            text = "⚔ 生存模式（默认）"
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = rounded(Color.rgb(80, 132, 171), 14)
            setPadding(dp(8), dp(12), dp(8), dp(12))
        }
        val creativeChoice = TextView(this).apply {
            text = "✧ 创造模式"
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(ink)
            background = rounded(Color.rgb(235, 243, 249), 14)
            setPadding(dp(8), dp(12), dp(8), dp(12))
        }
        modeChoices.addView(survivalChoice, LinearLayout.LayoutParams(0, -2, 1f).apply { rightMargin = dp(5) })
        modeChoices.addView(creativeChoice, LinearLayout.LayoutParams(0, -2, 1f).apply { leftMargin = dp(5) })
        survivalChoice.setOnClickListener {
            getSharedPreferences("gamer_mobile", MODE_PRIVATE).edit().putString("world_mode", "survival").apply()
            survivalChoice.setTextColor(Color.WHITE)
            survivalChoice.background = rounded(Color.rgb(80, 132, 171), 14)
            creativeChoice.setTextColor(ink)
            creativeChoice.background = rounded(Color.rgb(235, 243, 249), 14)
            status.text = "世界模式：生存模式。派蒙将以生存、探索和资源管理为优先目标。"
            speak("好耶！生存冒险出发啦！我们先保证安全，再收集资源、制作工具和建造小屋！")
        }
        creativeChoice.setOnClickListener {
            getSharedPreferences("gamer_mobile", MODE_PRIVATE).edit().putString("world_mode", "creative").apply()
            creativeChoice.setTextColor(Color.WHITE)
            creativeChoice.background = rounded(Color.rgb(80, 132, 171), 14)
            survivalChoice.setTextColor(ink)
            survivalChoice.background = rounded(Color.rgb(235, 243, 249), 14)
            status.text = "世界模式：创造模式。自主目标将偏向探索和建筑创作。"
            speak("创造模式开启！这次我们来建造一个超棒的世界吧！")
        }
        val savedMode = getSharedPreferences("gamer_mobile", MODE_PRIVATE).getString("world_mode", "survival")
        if (savedMode == "creative") creativeChoice.performClick()
        else getSharedPreferences("gamer_mobile", MODE_PRIVATE).edit().putString("world_mode", "survival").apply()
        worldPanel.addView(modeChoices)
        page.addView(worldPanel, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) })

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

    private fun startQrScanner() {
        // Request CAMERA explicitly before launching the embedded ZXing capture activity.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 702)
            return
        }

        try {
            val integrator = IntentIntegrator(this)
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            integrator.setPrompt("将电脑端 Gamer 配对二维码放入取景框")
            integrator.setBeepEnabled(false)
            integrator.setBarcodeImageEnabled(false)
            integrator.setOrientationLocked(false)
            integrator.initiateScan()
        } catch (e: android.content.ActivityNotFoundException) {
            status.text = "二维码扫描器启动失败：未找到 ZXing 扫描 Activity。"
            showToast("扫描模块未正确安装，请更新 Gamer Mobile。")
        } catch (e: SecurityException) {
            status.text = "摄像头权限不足，无法启动扫码。"
            showToast("请在系统设置中允许 Gamer Mobile 使用摄像头。")
        } catch (e: Exception) {
            status.text = "二维码扫描器启动失败：${e.localizedMessage ?: "未知错误"}"
            showToast("无法启动二维码扫描器，请检查摄像头权限后重试。")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val scanResult: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (scanResult != null) {
            val contents = scanResult.contents
            if (contents.isNullOrBlank()) {
                status.text = "已取消二维码扫描。"
            } else {
                val pairing = PairingClient.parse(contents)
                if (pairing == null) {
                    status.text = "二维码格式无效。请扫描电脑端 Gamer 配对页生成的二维码。"
                    showToast("无效配对二维码")
                    return
                }
                AlertDialog.Builder(this)
                    .setTitle("请求连接电脑")
                    .setMessage("目标电脑：${pairing.host}:${pairing.port}\\n协议：${pairing.protocol}\\n\\n确认后，手机将向该电脑提交一次性配对请求。请确认这是你自己的电脑和可信局域网。")
                    .setNegativeButton("取消", null)
                    .setPositiveButton("确认配对") { _, _ ->
                        status.text = "正在向电脑提交配对请求……"
                        Thread {
                            val result = try {
                                PairingClient.claim(this, pairing)
                            } catch (error: Exception) {
                                PairingClient.Result(false, error.message ?: "连接失败", null)
                            }
                            runOnUiThread {
                                if (result.success) {
                                    getSharedPreferences("gamer_mobile", MODE_PRIVATE).edit()
                                        .putString("paired_host", pairing.host)
                                        .putInt("paired_port", pairing.port)
                                        .putString("paired_instance_id", pairing.instanceId)
                                        .putString("paired_session_id", result.sessionId)
                                        .putString("paired_session_token", result.sessionToken)
                                        .putLong("paired_at", System.currentTimeMillis())
                                        .remove("last_pairing_qr_payload")
                                        .apply()
                                    status.text = "已与电脑建立配对会话。下一步可启用屏幕共享和游戏控制权限。"
                                    showToast("配对成功！")
                                } else {
                                    status.text = "配对失败：${result.message}"
                                    showToast("配对失败，请在电脑端重新生成二维码。")
                                }
                            }
                        }.start()
                    }
                    .show()
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
    private fun showGameAutoDialog() {
        val prefs = getSharedPreferences("gamer_mobile", MODE_PRIVATE)
        val name = prefs.getString("detected_game_name", null)
        val packageName = prefs.getString("detected_game_package", null)
        val kind = prefs.getString("detected_game_kind", "unknown")
        val mode = prefs.getString("world_mode", "survival") ?: "survival"
        val gameStatus = when {
            name == null -> "尚未检测到游戏。请先开启无障碍服务，并打开一款游戏。"
            kind == "minecraft" -> "已识别《我的世界》：将使用 Minecraft 专属 AI 配置。"
            kind == "genshin" -> "已识别《原神》：将切换到原神 AI 配置。"
            kind == "game" -> "已识别游戏：$name。将交由通用游戏 AI 适配器处理。"
            else -> "当前前台应用：$name（暂未匹配专属游戏配置）。"
        }
        val modePicker = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item,
                arrayOf("生存模式", "创造模式"))
            setSelection(if (mode == "creative") 1 else 0)
            isEnabled = kind == "minecraft"
        }
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
            addView(TextView(this@MainActivity).apply {
                text = gameStatus + (if (!packageName.isNullOrBlank()) "\n包名：$packageName" else "")
                textSize = 14f
                setTextColor(ink)
            })
            addView(TextView(this@MainActivity).apply {
                text = "Minecraft 世界模式"
                textSize = 14f
                setTextColor(muted)
                setPadding(0, dp(12), 0, dp(4))
            })
            addView(modePicker)
            addView(TextView(this@MainActivity).apply {
                text = "说明：自动识别依赖用户主动启用的 Android 无障碍服务。当前版本可识别游戏并切换配置；真正自动游玩仍需接入屏幕视觉模型、游戏动作适配器和 AI 推理服务。"
                textSize = 12f
                setTextColor(muted)
                setPadding(0, dp(10), 0, 0)
            })
        }
        AlertDialog.Builder(this)
            .setTitle("游戏识别与 AI 控制")
            .setView(body)
            .setNegativeButton("关闭", null)
            .setPositiveButton("保存 Minecraft 模式") { _, _ ->
                if (kind == "minecraft") {
                    val selected = if (modePicker.selectedItemPosition == 1) "creative" else "survival"
                    prefs.edit().putString("world_mode", selected).apply()
                    status.text = "Minecraft 模式已设为：${if (selected == "creative") "创造模式" else "生存模式"}"
                    speak(if (selected == "creative") "创造模式切换好啦！" else "生存模式切换好啦！")
                } else {
                    showToast("只有识别到《我的世界》时，才会应用这项模式设置。")
                }
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        if (::status.isInitialized) {
            val prefs = getSharedPreferences("gamer_mobile", MODE_PRIVATE)
            val detected = prefs.getString("detected_game_name", null)
            if (!detected.isNullOrBlank()) status.text = "当前识别游戏：$detected"
        }
    }

    private fun showPaimonVoiceDialog() {
        val input = EditText(this).apply {
            hint = "输入想让派蒙说的话"
            setText("你好呀旅行者！派蒙来陪你聊天啦！")
            minLines = 2
            gravity = Gravity.TOP
        }
        val note = TextView(this).apply {
            text = "手机语音对话已内置：可直接输入文字让手机朗读，也可使用麦克风进行语音输入。注意：当前朗读使用 Android 系统中文语音，不是派蒙原声模型。上游 paimon.pth 是桌面端 Python/VITS 权重，尚无经验证的 Android 可直接加载模型包；不能把 .pth 下载到手机就当作能运行。"
            textSize = 13f
            setTextColor(muted)
            setPadding(0, dp(8), 0, dp(12))
        }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
            addView(note)
            addView(input)
        }
        AlertDialog.Builder(this)
            .setTitle("派蒙 · 手机语音对话")
            .setView(layout)
            .setNeutralButton("🎙 语音输入") { _, _ -> startVoiceInput() }
            .setNegativeButton("关闭", null)
            .setPositiveButton("🔊 让派蒙说话") { _, _ ->
                val message = input.text.toString().trim()
                if (message.isNotEmpty()) {
                    status.text = "派蒙正在说话：$message"
                    speak(message)
                } else {
                    showToast("先输入一句想让派蒙说的话。")
                }
            }
            .show()
    }

    private fun showModelSetupDialog() {
        val prefs = getSharedPreferences("gamer_mobile", MODE_PRIVATE)
        val providers = arrayOf(
            "OpenAI 兼容接口",
            "DeepSeek",
            "本地 / 自建服务器",
            "其他兼容模型服务"
        )
        val current = prefs.getInt("model_provider_index", 0).coerceIn(providers.indices)
        val providerPicker = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, providers)
            setSelection(current)
        }
        val endpoint = EditText(this).apply {
            hint = "服务地址（例如 https://your-host/v1）"
            setSingleLine(true)
            setText(prefs.getString("model_endpoint", ""))
        }
        val model = EditText(this).apply {
            hint = "模型名称（例如模型服务要求的 model id）"
            setSingleLine(true)
            setText(prefs.getString("model_name", ""))
        }
        val note = TextView(this).apply {
            text = "可配置不同模型服务。当前版本仅保存连接配置；尚未发送请求、联网研究或执行模型推理。API 密钥管理与安全存储将在接入网络客户端时实现，请勿在此处填写密钥。"
            textSize = 12f
            setTextColor(muted)
            setPadding(0, dp(8), 0, 0)
        }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
            addView(providerPicker)
            addView(endpoint)
            addView(model)
            addView(note)
        }
        AlertDialog.Builder(this)
            .setTitle("AI 模型与研究服务")
            .setView(layout)
            .setNegativeButton("取消", null)
            .setPositiveButton("保存配置") { _, _ ->
                prefs.edit()
                    .putInt("model_provider_index", providerPicker.selectedItemPosition)
                    .putString("model_endpoint", endpoint.text.toString().trim())
                    .putString("model_name", model.text.toString().trim())
                    .apply()
                status.text = "模型服务配置已保存；当前尚未连接模型。"
                speak("配置保存好啦！等模型连接功能接通，我们就可以请不同的 AI 帮忙研究冒险策略。")
            }
            .show()
    }

    private fun startVoiceInput() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            speak("哎呀，旅行者，这台设备暂时没有可用的语音识别服务。")
            status.text = "设备没有可用的语音识别服务。"
            return
        }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 701)
            return
        }
        try {
            if (speechRecognizer == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
                speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) { status.text = "小伙伴正在听你说话……" }
                    override fun onBeginningOfSpeech() { status.text = "听到了！继续说吧。" }
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() { status.text = "正在理解你的指令……" }
                    override fun onError(error: Int) {
                        status.text = "语音识别未完成，请再试一次。"
                        speak("欸？我没听清楚，旅行者再说一次嘛。")
                    }
                    override fun onResults(results: Bundle?) {
                        val phrase = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                        if (phrase.isNullOrBlank()) {
                            speak("旅行者，你刚才说了什么？我没有听清楚。")
                        } else {
                            handleVoiceCommand(phrase)
                        }
                    }
                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "zh-CN")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "请说出游戏任务，例如：派蒙，去挖矿")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            status.text = "无法启动语音识别：${e.localizedMessage ?: "未知错误"}"
        }
    }

    private fun handleVoiceCommand(raw: String) {
        val command = raw.replace("派蒙", "").replace("小伙伴", "").trim()
        val task = when {
            command.contains("挖矿") || command.contains("矿") -> "采矿任务"
            command.contains("建房") || command.contains("建造") || command.contains("盖房") -> "建造任务"
            command.contains("木头") || command.contains("木材") -> "采集木材任务"
            command.contains("跟着我") || command.contains("跟随") -> "跟随任务"
            command.contains("回家") || command.contains("返回基地") -> "返回基地任务"
            command.contains("停止") || command.contains("停下") || command.contains("取消") -> "停止任务"
            command.contains("汇报") || command.contains("情况") -> "状态查询"
            else -> null
        }
        if (task == null) {
            status.text = "识别到：$raw（暂未匹配到支持的通用指令）"
            speak("旅行者，这个指令我还没学会。你可以让我挖矿、采集木材、建造、跟随、回家或者停止。")
            return
        }
        status.text = "语音识别：$raw\n任务解析：$task\n当前状态：已解析，等待接入游戏 AI 执行引擎。"
        val reply = when (task) {
            "采矿任务" -> "收到！我会把目标记为采矿。等游戏 AI 接入后，就能识别矿石并规划采集路线啦！"
            "建造任务" -> "好耶！建造任务已记下。还需要游戏 AI 识别材料、地形和建造规则，才能真正动工哦！"
            "采集木材任务" -> "木材收集任务记下啦！接入游戏代理后就去寻找树木。"
            "跟随任务" -> "明白啦，我会把目标设为跟随玩家。"
            "返回基地任务" -> "收到，目标设为返回基地。"
            "停止任务" -> "收到！停止指令优先级最高。当前原型没有启动自动操作，所以没有游戏动作需要中止。"
            else -> "旅行者，目前还没有正在执行的游戏任务。"
        }
        speak(reply)
    }

    private fun speak(message: String) {
        if (ttsReady) textToSpeech?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "gamer-mobile-reply")
        else status.text = "$message（设备语音引擎尚未就绪）"
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 701 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startVoiceInput()
        } else if (requestCode == 701) {
            status.text = "未获得麦克风权限，无法使用语音控制。"
        } else if (requestCode == 702 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startQrScanner()
        } else if (requestCode == 702) {
            status.text = "未获得摄像头权限，无法扫描电脑端二维码。请在系统设置中允许摄像头权限。"
            showToast("摄像头权限被拒绝，扫码功能无法启动。")
        }
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        mascot.removeCallbacksForCleanup()
        super.onDestroy()
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
    /**
     * Renders the REAL Paimon animation supplied in app/src/main/assets/paimon/.
     * No hand-drawn substitute is rendered when the licensed/source animation is absent.
     * idle.gif and talk.gif must contain genuine Paimon animation frames.
     */
    private class PaimonView(context: android.content.Context) : View(context) {
        private var talking = false
        private var movie: Movie? = null
        private var phase = 0f
        private var assetAvailable = false
        private val runner = object : Runnable {
            override fun run() {
                phase += .035f
                invalidate()
                postDelayed(this, 33)
            }
        }

        init {
            loadAnimation()
            post(runner)
        }

        fun setMood(value: Int) {
            val nextTalking = value == 1
            if (talking != nextTalking) {
                talking = nextTalking
                loadAnimation()
                invalidate()
            }
        }

        fun startFloating() {
            if (!isAttachedToWindow) post(runner)
        }

        fun removeCallbacksForCleanup() {
            removeCallbacks(runner)
        }

        private fun loadAnimation() {
            val file = if (talking) "paimon/talk.gif" else "paimon/idle.gif"
            movie = try {
                context.assets.open(file).use { Movie.decodeStream(it) }
            } catch (_: Exception) {
                null
            }
            assetAvailable = movie != null
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            post(runner)
        }

        override fun onDetachedFromWindow() {
            removeCallbacks(runner)
            super.onDetachedFromWindow()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val animation = movie ?: return
            val duration = animation.duration().takeIf { it > 0 } ?: 1000
            animation.setTime((System.currentTimeMillis() % duration).toInt())
            val baseScale = minOf(width.toFloat() / animation.width(), height.toFloat() / animation.height())
            val scale = baseScale * .92f
            val drawW = animation.width() * scale
            val drawH = animation.height() * scale
            val bob = sin(phase).toFloat() * resources.displayMetrics.density * 4f
            canvas.save()
            canvas.translate((width - drawW) / 2f, (height - drawH) / 2f + bob)
            canvas.scale(scale, scale)
            animation.draw(canvas, 0f, 0f)
            canvas.restore()
            postInvalidateDelayed(33)
        }
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
