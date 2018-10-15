package com.relic.presentation.subinfodialog

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.relic.R
import com.relic.dagger.DaggerVMComponent
import com.relic.data.RepoModule
import com.relic.presentation.subinfodialog.SubInfoDialogContract.Companion.ARG_SUB_NAME
import kotlinx.android.synthetic.main.display_subinfo_sheetdialog.*

class SubInfoBottomSheetDialog : BottomSheetDialogFragment() {

    private val viewModel : SubInfoDialogVM by lazy {
        ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                // inject dependencies into factory and construct viewmodel
                return DaggerVMComponent.builder()
                        .repoModule(RepoModule(context!!))
                        .build()
                        .getDisplaySubInfoVM().create(subName!!) as T
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

        subNameTextView.text = subName

        // initialize onclicks
        subscribeButtonView.setOnClickListener { viewModel.pinSubreddit(true) }
        pinButtonView.setOnClickListener {  }
    }

    private fun bindVm() {
        //
    }
}