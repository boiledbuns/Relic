package com.relic.presentation

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.relic.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.display_image.view.*

class DisplayImageFragment : Fragment() {
    private val IMAGE_KEY = "image_url"

    private lateinit var rootView : View
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
        rootView = inflater.inflate(R.layout.display_image, container, false).apply {
            setOnClickListener { activity!!.onBackPressed() }
        }

        loadImage(container!!.width)
        return rootView
    }

    private fun loadImage(parentWidth: Int) {
        if (imageUrl != null) {
            Picasso.get().load(imageUrl).resize(parentWidth, 0)
                    .into(rootView.findViewById<ImageView>(R.id.fullImage))
        }
    }

    companion object {
        fun create(thumbnailUrl : String) : DisplayImageFragment{
            val bundle = Bundle()
            bundle.putString("image_url", thumbnailUrl)

            return DisplayImageFragment().apply {
                arguments = bundle
            }
        }
    }

}
