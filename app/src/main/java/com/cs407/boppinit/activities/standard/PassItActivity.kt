package com.cs407.boppinit.activities.standard

import androidx.fragment.app.Fragment

// Empty view for passing
class PassItActivityView(private val onComplete: () -> Unit) : Fragment(), BopItActivityView {
    override fun initializeView() {}
    override fun startActivity() {}
    override fun stopActivity() {}
}