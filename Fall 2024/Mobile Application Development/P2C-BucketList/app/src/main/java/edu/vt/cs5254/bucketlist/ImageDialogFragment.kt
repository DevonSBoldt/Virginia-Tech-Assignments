package edu.vt.cs5254.bucketlist

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.core.view.doOnLayout
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import edu.vt.cs5254.bucketlist.databinding.FragmentImageDialogBinding
import java.io.File

class ImageDialogFragment : DialogFragment() {

    private val args : ImageDialogFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val binding = FragmentImageDialogBinding.inflate(layoutInflater)
        val photoFile = File(
            requireContext().applicationContext.filesDir,
            args.goalImageFilename
        )

        if (photoFile.exists()){
            binding.root.doOnLayout { dialog ->
                val bitmap = getScaledBitmap(
                    photoFile.path,
                    dialog.width,
                    dialog.height
                )
                binding.imageDetail.setImageBitmap(bitmap)
            }
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .show()
    }
}