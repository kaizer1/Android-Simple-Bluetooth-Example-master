package com.mcuhq.simplebluetooth

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class CustomDialogGOSCAN : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        return builder.setTitle("Важное сообщение")
            .setMessage(" Сканирование завершено с устройства можно сойти")
            .setNegativeButton("Закрыть", null)
            .create()
    }
}