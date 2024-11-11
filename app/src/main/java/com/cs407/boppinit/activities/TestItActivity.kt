package com.cs407.boppinit.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cs407.boppinit.databinding.FragmentTestItBinding

class TestItActivityView(private val onComplete: () -> Unit) : Fragment(), BopItActivityView {
    private var _binding: FragmentTestItBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestItBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        startActivity()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun initializeView() {
        binding.btnComplete.setOnClickListener {
            onComplete()
        }
    }

    override fun startActivity() {
        // Nothing needed - just waiting for button click
    }

    override fun stopActivity() {
        // Nothing needed to clean up
    }
}