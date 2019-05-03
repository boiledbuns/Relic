package com.relic.presentation.media

import android.arch.lifecycle.MutableLiveData
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gfycat.core.GfyCore
import com.gfycat.core.gfycatapi.pojo.Gfycat
import com.relic.R
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.display_gif.*
import kotlinx.coroutines.*

class DisplayGfycatFragment : Fragment() {
    private val TAG = "DISPLAY_GFYCAT_FRAGMENT"

    private var gfyUrl: String? = null
    private val gfycatLiveData : MutableLiveData<Gfycat> = MutableLiveData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // retrieve the image url from the bundled args
        gfyUrl = arguments!!.getString(GFY_KEY)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        // retrieve the reference to the binding
        return inflater.inflate(R.layout.display_gif, container, false).apply {
            setOnClickListener { activity!!.onBackPressed() }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gfycatLiveData.observe(viewLifecycleOwner) { gfycat ->
            displayGfycat.setupGfycat(gfycat)
        }

        loadGfyCat()
    }

    override fun onDestroyView() {
        displayGfycat.release()
        super.onDestroyView()
    }

    private fun loadGfyCat() {
        gfyUrl?.let {
            // parse the gfyid from the url
            val urlTokens = it.split("/")
            val gfyId = urlTokens.last()
            Log.d(TAG, "gfy id $gfyId")

            GlobalScope.launch {
                GfyCore.getFeedManager().getGfycat(gfyId).subscribe { gfycat, error ->
                    if (error != null) {
                        Log.d(TAG, "Error retrieving gfycat ${error}")
                    } else {
                        gfycatLiveData.postValue(gfycat)
                    }
                }
            }

        }

        // remember to release the resources for player when done
    }

    companion object {
        private val GFY_KEY = "gfy_url"

        fun create(gfyUrl : String) : DisplayGfycatFragment {
            val bundle = Bundle()
            bundle.putString(GFY_KEY, gfyUrl)

            return DisplayGfycatFragment().apply {
                arguments = bundle
            }
        }
    }

}
