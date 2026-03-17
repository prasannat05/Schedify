package com.example.schedify

import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import java.util.Locale
import kotlin.math.ceil

data class Subject(
    val name: String,
    val weeklyHours: Double,
    val difficulty: Int,
    val priority: String
) {
    val priorityMultiplier: Double = when (priority) {
        "High" -> 1.6
        "Medium" -> 1.3
        else -> 1.0
    }
    val load: Double = weeklyHours * difficulty * priorityMultiplier
}

data class StudySession(
    val subjectName: String,
    val day: String,
    var isCompleted: Boolean = false
)

class MainActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var mainRoot: ViewGroup

    // Screens
    private lateinit var layoutHome: View
    private lateinit var layoutSubjectEntry: View
    private lateinit var layoutDashboard: View
    private lateinit var layoutSettings: View

    // Home Views
    private lateinit var btnGoToSubjects: Button
    private lateinit var btnViewSchedule: Button

    // Subject Entry Views
    private lateinit var etSubjectName: EditText
    private lateinit var etWeeklyHours: EditText
    private lateinit var sbDifficulty: SeekBar
    private lateinit var spnPriority: Spinner
    private lateinit var llSubjectList: LinearLayout

    // Dashboard Views
    private lateinit var llWeeklySchedule: LinearLayout
    private lateinit var tvTotalPlanned: TextView
    private lateinit var tvTotalCompleted: TextView
    private lateinit var tvEfficiency: TextView
    private lateinit var pbEfficiency: ProgressBar

    // Settings Views
    private lateinit var etSettingsSpareTime: TextInputEditText
    private lateinit var etSettingsMaxSubs: TextInputEditText

    // Data
    private var dailySpareTime: Double = 4.0
    private var maxSubjectsPerDay: Int = 3
    private val subjects = mutableListOf<Subject>()
    private val schedule = mutableListOf<StudySession>()

    private val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = DatabaseHelper(this)
        initViews()
        loadDataFromDb()
        setupListeners()
        setupBackNavigation()
        
        showScreen(layoutHome)
    }

    private fun initViews() {
        mainRoot = findViewById(R.id.main)
        layoutHome = findViewById(R.id.layoutHome)
        layoutSubjectEntry = findViewById(R.id.layoutSubjectEntry)
        layoutDashboard = findViewById(R.id.layoutDashboard)
        layoutSettings = findViewById(R.id.layoutSettings)

        btnGoToSubjects = findViewById(R.id.btnGoToSubjects)
        btnViewSchedule = findViewById(R.id.btnViewSchedule)

        etSubjectName = findViewById(R.id.etSubjectName)
        etWeeklyHours = findViewById(R.id.etWeeklyHours)
        sbDifficulty = findViewById(R.id.sbDifficulty)
        spnPriority = findViewById(R.id.spnPriority)
        llSubjectList = findViewById(R.id.llSubjectList)

        llWeeklySchedule = findViewById(R.id.llWeeklySchedule)
        tvTotalPlanned = findViewById(R.id.tvTotalPlanned)
        tvTotalCompleted = findViewById(R.id.tvTotalCompleted)
        tvEfficiency = findViewById(R.id.tvEfficiency)
        pbEfficiency = findViewById(R.id.pbEfficiency)

        etSettingsSpareTime = findViewById(R.id.etSettingsSpareTime)
        etSettingsMaxSubs = findViewById(R.id.etSettingsMaxSubs)
    }

    private fun loadDataFromDb() {
        val config = db.getConfig()
        dailySpareTime = config.first
        maxSubjectsPerDay = config.second
        etSettingsSpareTime.setText(dailySpareTime.toString())
        etSettingsMaxSubs.setText(maxSubjectsPerDay.toString())

        subjects.clear()
        subjects.addAll(db.getSubjects())
        llSubjectList.removeAllViews()
        subjects.forEach { addSubjectToListView(it) }

        schedule.clear()
        schedule.addAll(db.getSchedule())
        if (schedule.isNotEmpty()) {
            displayDashboard()
        }
    }

    private fun setupListeners() {
        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            layoutSettings.isVisible = !layoutSettings.isVisible
        }

        findViewById<Button>(R.id.btnSaveSettings).setOnClickListener {
            val timeStr = etSettingsSpareTime.text.toString()
            val maxStr = etSettingsMaxSubs.text.toString()
            if (timeStr.isNotEmpty() && maxStr.isNotEmpty()) {
                dailySpareTime = timeStr.toDouble()
                maxSubjectsPerDay = maxStr.toInt()
                db.saveConfig(dailySpareTime, maxSubjectsPerDay)
                layoutSettings.isVisible = false
                showToast("Configuration Saved")
            }
        }

        findViewById<Button>(R.id.btnClearData).setOnClickListener {
            db.clearSubjects()
            loadDataFromDb()
            layoutSettings.isVisible = false
            showScreen(layoutHome)
            showToast("All Data Cleared")
        }

        btnGoToSubjects.setOnClickListener { showScreen(layoutSubjectEntry) }
        btnViewSchedule.setOnClickListener {
            if (schedule.isNotEmpty()) showScreen(layoutDashboard)
            else showToast("Generate a schedule first")
        }

        findViewById<Button>(R.id.btnAddSubject).setOnClickListener {
            val name = etSubjectName.text.toString()
            val hoursStr = etWeeklyHours.text.toString()
            val difficulty = sbDifficulty.progress + 1
            val priority = spnPriority.selectedItem.toString()

            if (name.isNotEmpty() && hoursStr.isNotEmpty()) {
                val subject = Subject(name, hoursStr.toDouble(), difficulty, priority)
                subjects.add(subject)
                db.addSubject(subject)
                addSubjectToListView(subject)
                clearSubjectInputs()
            } else {
                showToast("Please enter subject details")
            }
        }

        findViewById<Button>(R.id.btnGeneratePlan).setOnClickListener {
            if (subjects.isNotEmpty()) {
                generateSchedule()
                db.saveSchedule(schedule)
                displayDashboard()
                showScreen(layoutDashboard)
            } else {
                showToast("Add subjects first")
            }
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (layoutSettings.isVisible) {
                    layoutSettings.isVisible = false
                } else if (!layoutHome.isVisible) {
                    showScreen(layoutHome)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun showScreen(screen: View) {
        TransitionManager.beginDelayedTransition(mainRoot, AutoTransition())
        layoutHome.isVisible = false
        layoutSubjectEntry.isVisible = false
        layoutDashboard.isVisible = false
        screen.isVisible = true
    }

    private fun clearSubjectInputs() {
        etSubjectName.text.clear()
        etWeeklyHours.text.clear()
        sbDifficulty.progress = 2
    }

    private fun addSubjectToListView(subject: Subject) {
        val card = MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 8.dp, 0, 8.dp)
            }
            radius = 16.dp.toFloat()
            setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface))
            setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.glow_indigo)))
            strokeWidth = 1.dp
        }
        val tv = TextView(this).apply {
            text = String.format(Locale.getDefault(), "%s \u2022 %.1fh \u2022 Load: %.1f", subject.name, subject.weeklyHours, subject.load)
            setPadding(16.dp, 16.dp, 16.dp, 16.dp)
            setTextColor(ContextCompat.getColor(context, R.color.on_surface))
        }
        card.addView(tv)
        llSubjectList.addView(card, 0)
    }

    private fun generateSchedule() {
        schedule.clear()
        val sortedSubjects = subjects.sortedByDescending { it.load }
        val dayTimeUsage = mutableMapOf<String, Double>()
        val daySubjectsScheduled = mutableMapOf<String, MutableSet<String>>()
        daysOfWeek.forEach { 
            dayTimeUsage[it] = 0.0 
            daySubjectsScheduled[it] = mutableSetOf()
        }

        for (subject in sortedSubjects) {
            var sessionsToSchedule = ceil(subject.weeklyHours).toInt()
            for (day in daysOfWeek) {
                if (sessionsToSchedule <= 0) break
                val currentInDay = daySubjectsScheduled[day]!!
                if (currentInDay.size < maxSubjectsPerDay && (dayTimeUsage[day] ?: 0.0) + 1.0 <= dailySpareTime) {
                    schedule.add(StudySession(subject.name, day))
                    dayTimeUsage[day] = (dayTimeUsage[day] ?: 0.0) + 1.0
                    currentInDay.add(subject.name)
                    sessionsToSchedule--
                }
            }
        }
    }

    private fun displayDashboard() {
        llWeeklySchedule.removeAllViews()
        daysOfWeek.forEach { day ->
            val daySessions = schedule.filter { it.day == day }
            if (daySessions.isNotEmpty()) {
                val dayTitle = TextView(this).apply {
                    text = day
                    setTextColor(ContextCompat.getColor(context, R.color.secondary))
                    setPadding(8.dp, 16.dp, 0, 8.dp)
                    typeface = Typeface.DEFAULT_BOLD
                }
                llWeeklySchedule.addView(dayTitle)
                val card = MaterialCardView(this).apply {
                    radius = 16.dp.toFloat()
                    setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface))
                }
                val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
                daySessions.forEach { session ->
                    val cb = CheckBox(this).apply {
                        text = session.subjectName
                        isChecked = session.isCompleted
                        setTextColor(ContextCompat.getColor(context, R.color.on_surface))
                        setOnCheckedChangeListener { _, checked ->
                            session.isCompleted = checked
                            db.updateSessionStatus(session.subjectName, session.day, checked)
                            updateEfficiency()
                        }
                    }
                    list.addView(cb)
                }
                card.addView(list)
                llWeeklySchedule.addView(card)
            }
        }
        updateEfficiency()
    }

    private fun updateEfficiency() {
        val total = schedule.size
        val completed = schedule.count { it.isCompleted }
        val efficiency = if (total > 0) (completed.toDouble() / total * 100).toInt() else 0
        tvEfficiency.text = String.format(Locale.getDefault(), "Efficiency: %d%%", efficiency)
        tvTotalPlanned.text = String.format(Locale.getDefault(), "Planned: %d", total)
        tvTotalCompleted.text = String.format(Locale.getDefault(), "Done: %d", completed)
        ObjectAnimator.ofInt(pbEfficiency, "progress", efficiency).setDuration(500).start()
    }

    private fun showToast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
}
