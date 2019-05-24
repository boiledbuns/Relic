package com.relic.presentation.subinfodialog

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.relic.R
import com.relic.dagger.DaggerVMComponent
import com.relic.dagger.modules.AuthModule
import com.relic.dagger.modules.RepoModule
import com.relic.dagger.modules.UtilModule
import com.relic.data.models.SubredditModel
import com.relic.presentation.subinfodialog.SubInfoDialogContract.Companion.ARG_SUB_NAME
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.display_subinfo_sheetdialog.*

class SubInfoBottomSheetDialog : BottomSheetDialogFragment() {
    private val TAG = "SUB_INFO_DIALOG"

    private val viewModel : SubInfoDialogVM by lazy {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                // inject dependencies into factory and construct viewmodel
                return DaggerVMComponent.builder()
                        .repoModule(RepoModule(context!!))
                        .authModule(AuthModule(context!!))
                        .utilModule(UtilModule(activity!!.application))
                        .build()
                        .getDisplaySubInfoVM().create(subName) as T
            }
        }).get(SubInfoDialogVM::class.java)
    }

    private lateinit var subName : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.getString(ARG_SUB_NAME)?.apply {
            subName = this
            bindVm()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.display_subinfo_sheetdialog, container,  false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subNameView.text = resources.getString(R.string.sub_prefix_name, subName)

        // initialize onclicks
        subInfoSubView.setOnClickListener { }
        subInfoPinView.setOnClickListener { viewModel.pinSubreddit(true) }
    }

    private fun bindVm() {
        viewModel.subredditLiveData.nonNull().observe(this) { setSubredditData(it) }
        viewModel.sideBarLiveData.nonNull().observe(this) {
            Log.d(TAG, "Sidebar $it")
        }
    }

    private fun setSubredditData(subredditModel: SubredditModel) {
        if (subredditModel.isSubscribed) {
//            subscribeButtonView.text = getString(R.string.subscribed)
            subInfoSubView.background?.setTint(resources.getColor(R.color.positive))
        } else {
//            subscribeButtonView.text = getString(R.string.subscribe)
            subInfoSubView.background?.setTint(resources.getColor(R.color.negative))
        }

        subCountView.text = resources.getString(R.string.subscriber_count, subredditModel.subscriberCount)
        subDescriptionView.text = Html.fromHtml(Html.fromHtml(subredditModel.description).toString())
        // TODO create custom movement method class
        subDescriptionView.movementMethod
    }

}