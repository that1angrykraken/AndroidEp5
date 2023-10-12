package seamonster.kraken.androidep5

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.BigTextStyle
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.lang.StringBuilder
import java.util.Calendar

class NotificationWorker(private val context: Context, params: WorkerParameters) :
    Worker(context, params) {
    companion object {
        private const val CHANNEL_ID = "NOTIFY_DAILY_TASKS"
        private const val NOTIFICATION_ID = 1
    }

    override fun doWork(): Result {
        showNotification()
        return Result.success()
    }

    private fun showNotification() {
        val channel = NotificationChannelCompat
            .Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_HIGH)
            .setName("Daily tasks")
            .build()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Today tasks.")
            .setContentText(getContentText())
            .setStyle(BigTextStyle().bigText(getContentText())) // expandable big text
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).run {
            createNotificationChannel(channel)
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(NOTIFICATION_ID, notification)
            }
        }
    }

    private fun getContentText() : String {
        val list = getTodayTasks()
        return if (list.isEmpty()) {
            "You don't have any tasks today!"
        } else {
            val stringBuilder = StringBuilder()
            list.forEach { task ->
                stringBuilder.append(task.name)
                if (list.lastIndex != list.indexOf(task)) {
                    stringBuilder.append("\n")
                }
            }
            stringBuilder.toString()
        }
    }

    private fun getCalendar(task: Task): Calendar {
        val date = task.date.split("/")
        val day = date[0].toInt()
        val month = date[1].toInt() - 1
        val year = date[2].toInt()
        return Calendar.getInstance().apply {
            set(year, month, day)
        }
    }

    private fun getTodayTasks(): List<Task> {
        val list = ArrayList<Task>()
        val today = getToday()
        val tomorrow = getTomorrow()
        getTasks(context).forEach {
            val taskDate = getCalendar(it)
            if (taskDate.after(today) && taskDate.before(tomorrow)) {
                list.add(it)
            }
        }
        return list
    }

    private fun getToday(): Calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }

    private fun getTomorrow(): Calendar = Calendar.getInstance().apply {
        set(Calendar.DATE, get(Calendar.DATE) + 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }
}