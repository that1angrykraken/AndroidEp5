package seamonster.kraken.androidep5

import android.content.Context
import android.net.Uri
import android.util.Log

data class Task(val id: Int, val name: String, val date: String)

fun getTasks(context: Context): List<Task> {
    val list = ArrayList<Task>()

    val uri = Uri.parse("content://seamonster.kraken.androidep4.TaskProvider/tasks")
    val projection = arrayOf("id", "name", "date")
    val cursor = context.contentResolver.query(uri, projection, null, null, null)
    Log.d(MainActivity.TAG, "getTasks: ${cursor?.count}")
    cursor?.let {
        if (cursor.moveToFirst()) {
            do {
                val task = Task(cursor.getInt(0), cursor.getString(1), cursor.getString(2))
                list.add(task)
            } while (cursor.moveToNext())
        }
        cursor.close()
    }
    return list
}

