package com.example.schedify

import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
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

    // Main Container for Transitions
    private lateinit var mainRoot: ViewGroup

    // Screen Layouts
    private lateinit var layoutSetup: View
    private lateinit var layoutSubjectEntry: View
    private lateinit var layoutDashboard: View

    // Setup Screen Views
    private lateinit var etDailySpareTime: TextInputEditText
    private lateinit var etMaxSubjectsPerDay: TextInputEditText

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

    // Data
    private var dailySpareTime: Double = 0.0
    private var maxSubjectsPerDay: Int = 0
    private val subjects = mutableListOf<Subject>()
    private val schedule = mutableListOf<StudySession>()

    private val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupListeners()
        
        // Simple entrance animation for the first screen
        layoutSetup.alpha = 0f
        layoutSetup.animate().alpha(1f).setDuration(800).start()
    }

    private fun initViews() {
        mainRoot = findViewById(R.id.main)
        layoutSetup = findViewById(R.id.layoutSetup)
        layoutSubjectEntry = findViewById(R.id.layoutSubjectEntry)
        layoutDashboard = findViewById(R.id.layoutDashboard)

        etDailySpareTime = findViewById(R.id.etDailySpareTime)
        etMaxSubjectsPerDay = findViewById(R.id.etMaxSubjectsPerDay)

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
    }

    private fun setupListeners() {
        findViewById<Button>(R.id.btnContinueToSubjects).setOnClickListener {
            val spareTimeStr = etDailySpareTime.text.toString()
            val maxSubsStr = etMaxSubjectsPerDay.text.toString()

            if (spareTimeStr.isNotEmpty() && maxSubsStr.isNotEmpty()) {
                dailySpareTime = spareTimeStr.toDouble()
                maxSubjectsPerDay = maxSubsStr.toInt()

                if (dailySpareTime > 0 && maxSubjectsPerDay > 0) {
                    animateTransition(layoutSubjectEntry)
                } else {
                    showToast("Values must be greater than zero")
                }
            } else {
                showToast("Please fill all configuration fields")
            }
        }

        findViewById<Button>(R.id.btnAddSubject).setOnClickListener {
            val name = etSubjectName.text.toString()
            val hoursStr = etWeeklyHours.text.toString()
            val difficulty = sbDifficulty.progress + 1
            val priority = spnPriority.selectedItem.toString()

            if (name.isNotEmpty() && hoursStr.isNotEmpty()) {
                val hours = hoursStr.toDouble()
                if (hours > 0) {
                    val subject = Subject(name, hours, difficulty, priority)
                    subjects.add(subject)
                    addSubjectToListViewAnimated(subject)
                    clearSubjectInputs()
                } else {
                    showToast("Study hours must be positive")
                }
            } else {
                showToast("Please enter subject details")
            }
        }

        findViewById<Button>(R.id.btnGeneratePlan).setOnClickListener {
            if (subjects.isNotEmpty()) {
                generateSchedule()
                animateTransition(layoutDashboard)
                displayDashboardAnimated()
            } else {
                showToast("Add at least one subject to generate a plan")
            }
        }

        findViewById<Button>(R.id.btnReset).setOnClickListener {
            subjects.clear()
            schedule.clear()
            llSubjectList.removeAllViews()
            animateTransition(layoutSetup)
        }
    }

    private fun animateTransition(newLayout: View) {
        val transition = AutoTransition()
        transition.duration = 500
        transition.interpolator = AccelerateDecelerateInterpolator()
        
        TransitionManager.beginDelayedTransition(mainRoot, transition)
        
        layoutSetup.visibility = View.GONE
        layoutSubjectEntry.visibility = View.GONE
        layoutDashboard.visibility = View.GONE
        newLayout.visibility = View.VISIBLE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun clearSubjectInputs() {
        etSubjectName.text.clear()
        etWeeklyHours.text.clear()
        sbDifficulty.progress = 2
        spnPriority.setSelection(0)
        etSubjectName.requestFocus()
    }

    private fun addSubjectToListViewAnimated(subject: Subject) {
        val card = MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 4.dp, 0, 4.dp)
            }
            radius = 16.dp.toFloat()
            cardElevation = 2.dp.toFloat()
            setCardBackgroundColor(Color.WHITE)
            strokeWidth = 0
            alpha = 0f
            scaleX = 0.8f
            scaleY = 0.8f
        }

        val tv = TextView(this).apply {
            val content = "${subject.name} \u2022 ${subject.weeklyHours}h \u2022 Load: ${String.format("%.1f", subject.load)}"
            text = content
            setPadding(16.dp, 16.dp, 16.dp, 16.dp)
            setTextColor(ContextCompat.getColor(context, R.color.on_surface))
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }
        
        card.addView(tv)
        llSubjectList.addView(card, 0)

        card.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(400)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun generateSchedule() {
        schedule.clear()
        val sortedSubjects = subjects.sortedByDescending { it.load }
        
        val dayTimeUsage = mutableMapOf<String, Double>()
        val daySubjectUsage = mutableMapOf<String, Int>()
        val daySubjectsScheduled = mutableMapOf<String, MutableSet<String>>()

        daysOfWeek.forEach { 
            dayTimeUsage[it] = 0.0 
            daySubjectUsage[it] = 0
            daySubjectsScheduled[it] = mutableSetOf()
        }

        for (subject in sortedSubjects) {
            var sessionsToSchedule = ceil(subject.weeklyHours).toInt()
            val startIndex = sortedSubjects.indexOf(subject) % 7
            
            for (i in 0 until 7) {
                if (sessionsToSchedule <= 0) break
                
                val day = daysOfWeek[(startIndex + i) % 7]
                val currentTime = dayTimeUsage[day] ?: 0.0
                val currentSubs = daySubjectUsage[day] ?: 0
                val scheduledInDay = daySubjectsScheduled[day]

                if (currentSubs < maxSubjectsPerDay && 
                    currentTime + 1.0 <= dailySpareTime && 
                    scheduledInDay?.contains(subject.name) == false) {
                    
                    schedule.add(StudySession(subject.name, day))
                    dayTimeUsage[day] = currentTime + 1.0
                    daySubjectUsage[day] = currentSubs + 1
                    scheduledInDay?.add(subject.name)
                    sessionsToSchedule--
                }
            }
        }
    }

    private fun displayDashboardAnimated() {
        llWeeklySchedule.removeAllViews()
        var delay = 100L

        for (day in daysOfWeek) {
            val daySessions = schedule.filter { it.day == day }
            if (daySessions.isNotEmpty()) {
                
                val dayTitle = TextView(this).apply {
                    text = day
                    textSize = 18f
                    typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
                    setTextColor(ContextCompat.getColor(context, R.color.primary))
                    setPadding(8.dp, 24.dp, 0, 12.dp)
                    alpha = 0f
                }
                llWeeklySchedule.addView(dayTitle)
                dayTitle.animate().alpha(1f).setStartDelay(delay).setDuration(300).start()

                val dayCard = MaterialCardView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 0, 0, 12.dp)
                    }
                    radius = 24.dp.toFloat()
                    cardElevation = 6.dp.toFloat()
                    strokeWidth = 0
                    setCardBackgroundColor(Color.WHITE)
                    alpha = 0f
                    translationY = 50f.dp
                }

                val sessionsLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(16.dp, 16.dp, 16.dp, 16.dp)
                }

                for (session in daySessions) {
                    val cb = CheckBox(this).apply {
                        text = session.subjectName
                        textSize = 16f
                        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                        setTextColor(ContextCompat.getColor(context, R.color.on_surface))
                        buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.secondary))
                        isChecked = session.isCompleted
                        setPadding(12.dp, 16.dp, 12.dp, 16.dp)
                        setOnCheckedChangeListener { _, isChecked ->
                            session.isCompleted = isChecked
                            updateEfficiencyAnimated()
                        }
                    }
                    sessionsLayout.addView(cb)
                }
                
                dayCard.addView(sessionsLayout)
                llWeeklySchedule.addView(dayCard)

                dayCard.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(delay + 100L)
                    .setDuration(500)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
                
                delay += 150L
            }
        }
        updateEfficiencyAnimated()
    }

    private fun updateEfficiencyAnimated() {
        val total = schedule.size
        val completed = schedule.count { it.isCompleted }
        val efficiency = if (total > 0) (completed.toDouble() / total * 100).toInt() else 0

        val plannedText = "Planned: $total sessions"
        val doneText = "Done: $completed"
        val effText = "Efficiency: $efficiency%"
        
        tvTotalPlanned.text = plannedText
        tvTotalCompleted.text = doneText
        tvEfficiency.text = effText
        
        ObjectAnimator.ofInt(pbEfficiency, "progress", pbEfficiency.progress, efficiency).apply {
            duration = 600
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
    
    private val Float.dp: Float
        get() = (this * resources.displayMetrics.density)
}
