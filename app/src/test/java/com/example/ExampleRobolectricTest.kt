package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.RoutineTask
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("RoutineMate", appName)
  }

  @Test
  fun `routine task default values`() {
    val task = RoutineTask(
        title = "Team Meeting",
        date = "2026-10-12",
        time = "14:00"
    )
    assertEquals("Team Meeting", task.title)
    assertEquals("2026-10-12", task.date)
    assertEquals("14:00", task.time)
    assertEquals(30, task.durationMinutes)
    assertFalse(task.isCompleted)
    assertFalse(task.isTriggered)
    assertEquals(0, task.snoozeCount)
  }
}
