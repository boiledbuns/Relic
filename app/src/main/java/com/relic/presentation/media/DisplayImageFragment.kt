package com.relic.presentation.media

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.gfycat.core.GfyCore

import com.relic.R
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.display_image.*
import java.lang.Exception

class DisplayImageFragment : Fragment() {
    private var imageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // retrieve the image url from the bundled args
        imageUrl = arguments!!.getString(IMAGE_KEY)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        // retrieve the reference to the binding
        return inflater.inflate(R.layout.display_image, container, false).apply {
            setOnClickListener { activity!!.onBackPressed() }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadImage()
    }

    private fun loadImage() {
        // TODO add gif and video support

        val callback = object : Callback {
            override fun onSuccess() {
                displayImageProgress.visibility = View.GONE
            }
            override fun onError(e: Exception?) {
                val message = getString(R.string.loading_error, "image")
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }

        if (imageUrl != null) {
            Picasso.get().load(imageUrl).fit().centerInside().into(fullImage, callback)
        }
    }

    companion object {
        private val IMAGE_KEY = "image_url"

        fun create(thumbnailUrl : String) : DisplayImageFragment {
            val bundle = Bundle()
            bundle.putString(IMAGE_KEY, thumbnailUrl)

            return DisplayImageFragment().apply {
                arguments = bundle
            }
        }
    }

}
