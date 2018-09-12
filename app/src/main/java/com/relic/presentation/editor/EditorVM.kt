package com.relic.presentation.editor

import android.arch.lifecycle.ViewModel

class EditorVM : EditorContract.VM, ViewModel {
    private var isInitialized : Boolean = false;

    public override fun isInitialized(): Boolean = isInitialized

    init {
        isInitialized = true;
    }

    constructor() {

    }

}