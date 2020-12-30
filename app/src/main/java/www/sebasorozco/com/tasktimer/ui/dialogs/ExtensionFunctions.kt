package www.sebasorozco.com.tasktimer.ui.dialogs

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import www.sebasorozco.com.tasktimer.R


fun FragmentActivity.showConfirmationDialog(id: Int,
                                            message: String,
                                            positiveCaption: Int,
                                            negativeCaption: Int) {
    val args = Bundle().apply {
        putInt(DIALOG_ID, id)
        putString(DIALOG_MESSAGE, message)
        putInt(DIALOG_POSITIVE_RID, positiveCaption)
        putInt(DIALOG_NEGATIVE_RID, negativeCaption)
    }
    val dialog = AppDialog()
    dialog.arguments = args
    dialog.show(supportFragmentManager, null)
}
