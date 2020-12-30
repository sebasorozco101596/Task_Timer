package www.sebasorozco.com.tasktimer.data.database

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@SuppressLint("ParcelCreator")
data class Task(val name: String, val description: String?, val sortOrder: Int,var id: Long= 0) :
    Parcelable
