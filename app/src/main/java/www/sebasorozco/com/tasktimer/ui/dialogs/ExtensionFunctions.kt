package www.sebasorozco.com.tasktimer.ui.dialogs

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

fun FragmentActivity.findFragmentById(id: Int) : Fragment?{
    return supportFragmentManager.findFragmentById(id)
}

/**
 * This function show the Dialog with the different objects that it has inside
 */
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

/**
 * Extensions based on the article by Denish Babuhunky
 * at https://medium.com/thoughts-overflow/how-to-add-a-fragment-in-kotlin-way-73203c5a450b
 */

inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) {
    beginTransaction().func().commit()
}

fun FragmentActivity.replaceFragment(fragment: Fragment, frameId: Int){
    supportFragmentManager.inTransaction { replace(frameId,fragment) }
}

fun FragmentActivity.removeFragment(fragment: Fragment){
    supportFragmentManager.inTransaction { remove(fragment) }
}