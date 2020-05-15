package com.relic.presentation.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.relic.R
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.display_image.*

class DisplayImageFragment : DialogFragment() {

    private val args: DisplayImageFragmentArgs by navArgs()
    private val imageUrl by lazy { args.url }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        // retrieve the reference to the binding
        return inflater.inflate(R.layout.display_image, container, false).apply {
            setOnClickListener { requireActivity().onBackPressed() }
        }
    }

    override fun getTheme(): Int = R.style.FullScreenDialogTheme

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadImage()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Picasso.get().cancelRequest(fullImage)
    }

    private fun loadImage() {
        val callback = object : Callback {
            override fun onSuccess() {
                displayImageProgress.visibility = View.GONE
            }
            override fun onError(e: Exception?) {
                val message = getString(R.string.loading_error, "image")
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }

        Picasso.get().load(imageUrl).fit().centerInside().into(fullImage, callback)
    }
}
