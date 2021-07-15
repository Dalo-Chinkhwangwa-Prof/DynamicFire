package com.dynamicdevz.dynamicfirebase.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.dynamicdevz.dynamicfirebase.databinding.SignupFragmentLayoutBinding
import com.dynamicdevz.dynamicfirebase.model.NewUser

class SignupFragment : Fragment() {


    companion object{

        private lateinit var fragment: SignupFragment
        fun getInstance(): SignupFragment{
            if(!this::fragment.isInitialized){
                fragment = SignupFragment()
            }
            return fragment
        }
    }

    private lateinit var binding: SignupFragmentLayoutBinding

    private lateinit var  signupDelegate: SignupDelegate

    interface SignupDelegate {
        fun registerUser(newUser: NewUser)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        signupDelegate = context as LoginActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SignupFragmentLayoutBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.registerButton.setOnClickListener {

            if(verifyInput()){
                val email = binding.emailEdittext.text.toString().trim()
                val password = binding.passwordEdittext.text.toString().trim()
                clearFields()
                signupDelegate.registerUser(NewUser(email, password))
                requireActivity().supportFragmentManager.popBackStack()
            }

        }
    }

    private fun clearFields() {
        binding.emailEdittext.text.clear()
        binding.verifyEmailEdittext.text.clear()
        binding.passwordEdittext.text.clear()
        binding.vertifyPasswordEdittext.text?.clear()
    }

    private fun verifyInput(): Boolean {
        if(binding.emailEdittext.text.isEmpty()){
            return showMessage("Email cannot be empty")
        } else if(binding.verifyEmailEdittext.text.isEmpty()){
            return showMessage("Please verify email")
        } else if(binding.verifyEmailEdittext.text.toString().trim() != binding.emailEdittext.text.toString().trim()){
            return showMessage("Emails do not match")
        }else if(binding.passwordEdittext.text.isEmpty()){
            return showMessage("Password should not be empty")
        } else if(binding.vertifyPasswordEdittext.text.toString().isEmpty()){
            return showMessage("Please verify password")
        } else if(binding.vertifyPasswordEdittext.text.toString().trim() != binding.passwordEdittext.text.toString().trim()){
            return showMessage("Passwords do not match!")
        }
        return true
    }

    private fun showMessage(s: String): Boolean {
        Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show()
        return false
    }


}