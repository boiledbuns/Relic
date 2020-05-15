package com.relic.presentation.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.navArgs
import com.gfycat.core.GfyCore
import com.gfycat.core.gfycatapi.pojo.Gfycat
import com.relic.R
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.display_gif.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

class DisplayGfycatFragment : DialogFragment() {

    private val args: DisplayGfycatFragmentArgs by navArgs()
    private val url by lazy { args.url }

    private val gfycatLiveData: MutableLiveData<Gfycat> = MutableLiveData()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        // retrieve the reference to the binding
        return inflater.inflate(R.layout.display_gif, container, false).apply {
            setOnClickListener { requireActivity().onBackPressed() }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gfycatLiveData.observe(viewLifecycleOwner) { gfycat ->
            displayGfycat.setupGfycat(gfycat)
        }

        loadGfyCat()
    }

    override fun getTheme(): Int = R.style.FullScreenDialogTheme

    override fun onDestroyView() {
        displayGfycat.release()
        super.onDestroyView()
    }

    private fun loadGfyCat() {
        // parse the gfyid from the url
        val urlTokens = url.split("/")
        val gfyId = urlTokens.last()
        Timber.d("gfy id $gfyId")

        GlobalScope.launch {
            GfyCore.getFeedManager().getGfycat(gfyId).subscribe { gfycat, error ->
                if (error != null) {
                    Timber.d("Error retrieving gfycat $error")
                } else {
                    gfycatLiveData.postValue(gfycat)
                }
            }
        }
        // TODO release the resources for player when done
    }
}
