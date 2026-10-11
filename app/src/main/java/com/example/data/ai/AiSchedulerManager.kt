package com.example.data.ai

import com.example.data.api.GeminiApiClient
import com.example.data.model.RoutineTask
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ProposedRoutineAction(
    val action: String = "NONE", // "CREATE", "UPDATE", "NONE"
    val title: String = "",
    val date: String = "",       // "yyyy-MM-dd"
    val time: String = "",       // "HH:mm"
    val durationMinutes: Int = 30,
    val category: String = "general",
    val priority: String = "medium",
    val explanation: String = ""
)

data class AiScheduleResult(
    val conversationalReply: String,
    val proposedAction: ProposedRoutineAction? = null
)

class AiSchedulerManager(
    private val apiClient: GeminiApiClient = GeminiApiClient()
) {

    suspend fun processUserMessage(
        apiKey: String,
        userMessage: String,
        existingTasks: List<RoutineTask>,
        chatHistory: List<Pair<String, String>> = emptyList()
    ): Result<AiScheduleResult> {
        val now = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dayOfWeekFormat = SimpleDateFormat("EEEE", Locale.US)
        val timeFormat = SimpleDateFormat("HH:mm", Locale.US)

        val currentDateStr = dateFormat.format(now)
        val currentTimeStr = timeFormat.format(now)
        val currentDayStr = dayOfWeekFormat.format(now)

        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrowDateStr = dateFormat.format(cal.time)
        val tomorrowDayStr = dayOfWeekFormat.format(cal.time)

        // Build summary of existing routines
        val scheduleSummary = StringBuilder()
        if (existingTasks.isEmpty()) {
            scheduleSummary.append("No existing routines currently scheduled (all days and times are completely free).\n")
        } else {
            val groupedByDate = existingTasks.groupBy { it.date }
            groupedByDate.forEach { (date, tasks) ->
                scheduleSummary.append("Date: $date\n")
                tasks.sortedBy { it.time }.forEach { t ->
                    scheduleSummary.append("  - ${t.time} (${t.durationMinutes} min): ${t.title} [${t.category}]\n")
                }
            }
        }

        val systemPrompt = """
You are "RoutineMate AI", a smart, friendly, and highly efficient routine scheduler and productivity assistant.
All instructions, explanations, and responses MUST be in clear, elegant, polished English.
(You understand user queries in both English and Bengali effortlessly, but always respond in English).

CURRENT DATE & TIME CONTEXT:
- Today: $currentDateStr ($currentDayStr)
- Current Time: $currentTimeStr
- Tomorrow: $tomorrowDateStr ($tomorrowDayStr)

EXISTING USER ROUTINE SCHEDULE (Check this for occupied vs open free slots):
$scheduleSummary

KEY SCHEDULING RULES:
1. When user requests scheduling a task with general timeframes (e.g. "I have a meeting tomorrow around noon", "আগামীকাল দুপুরের সময় আমার মিটিং আছে", "reading tonight at 9", "gym tomorrow morning"):
   - Interpret time windows:
     * Morning: 08:00 - 11:30
     * Noon / Lunch: 12:00 - 14:30 (e.g. 13:00 or 14:00)
     * Afternoon: 15:00 - 18:00
     * Evening / Night: 19:00 - 22:30
   - Check the EXISTING USER ROUTINE SCHEDULE for conflicts!
   - Find an OPEN, FREE time slot that does not collide with existing routines.
   - In your response, clearly inform the user why and which exact time you picked (e.g. "I found an open slot at 2:00 PM tomorrow, so I've scheduled your 'Team Meeting' for 2:00 PM. You can confirm or let me know if you prefer a different time.").
2. When user asks to change or adjust a time (e.g. "No, make it 3 PM", "Move it to 5:00", "না ৩টায় দাও"):
   - Respect their requested time ("15:00" or "17:00") on the target date.
   - Acknowledge the change smoothly.
3. At the end of every message where a routine/task is identified, created, or changed, you MUST include a JSON block enclosed within ```routine_json and ```:
```routine_json
{
  "action": "CREATE", 
  "title": "Meeting",
  "date": "YYYY-MM-DD",
  "time": "HH:mm",
  "durationMinutes": 30,
  "category": "meeting",
  "priority": "medium",
  "explanation": "Scheduled at 2:00 PM in your open time slot"
}
```
Available action values: "CREATE", "UPDATE", "NONE".
Categories: "meeting", "work", "study", "health", "personal", "general".
Priorities: "low", "medium", "high".
If the message is just a general greeting or casual conversation, set "action": "NONE".
Keep your response concise, minimalist, and conversational.
        """.trimIndent()

        val apiResult = apiClient.generateContent(
            apiKey = apiKey,
            prompt = userMessage,
            systemInstruction = systemPrompt,
            chatHistory = chatHistory
        )

        return apiResult.map { rawResponse ->
            parseAiResponse(rawResponse)
        }
    }

    private fun parseAiResponse(rawResponse: String): AiScheduleResult {
        val jsonPattern = Regex("```routine_json\\s*([\\s\\S]*?)\\s*```")
        val match = jsonPattern.find(rawResponse)

        var conversationalText = rawResponse
        var proposedAction: ProposedRoutineAction? = null

        if (match != null) {
            val jsonStr = match.groupValues[1].trim()
            conversationalText = rawResponse.replace(match.value, "").trim()
            try {
                val json = JSONObject(jsonStr)
                val action = json.optString("action", "NONE")
                if (action.equals("CREATE", ignoreCase = true) || action.equals("UPDATE", ignoreCase = true)) {
                    proposedAction = ProposedRoutineAction(
                        action = action.uppercase(),
                        title = json.optString("title", "Routine Task"),
                        date = json.optString("date", ""),
                        time = json.optString("time", "12:00"),
                        durationMinutes = json.optInt("durationMinutes", 30),
                        category = json.optString("category", "general"),
                        priority = json.optString("priority", "medium"),
                        explanation = json.optString("explanation", "")
                    )
                }
            } catch (_: Exception) {
                // If json parsing fails, fallback gracefully
            }
        }

        return AiScheduleResult(
            conversationalReply = conversationalText,
            proposedAction = proposedAction
        )
    }
}
