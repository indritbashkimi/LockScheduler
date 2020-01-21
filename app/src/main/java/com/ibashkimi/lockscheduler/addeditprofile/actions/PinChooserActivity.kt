package com.ibashkimi.lockscheduler.addeditprofile.actions

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View.OnClickListener
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.ibashkimi.lockscheduler.R
import com.ibashkimi.lockscheduler.databinding.ActivityPinPassChooserBinding
import com.ibashkimi.lockscheduler.ui.BaseActivity

class PinChooserActivity : BaseActivity() {
    private lateinit var binding: ActivityPinPassChooserBinding
    private var input: String? = null
    private var minLength = 4
    private var inputType: String? = null
    private val firstFabListener = OnClickListener { onContinuePressed() }
    private val finalFabListener = OnClickListener { onDonePressed() }

    private val textChangeListener: TextWatcher = TextChangeListener {
        binding.actionButton.isEnabled = it.length >= minLength
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinPassChooserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.ic_clear)
            setDisplayShowHomeEnabled(true)
            setDisplayShowCustomEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
        binding.cancel.setOnClickListener { onCancel() }
        if (savedInstanceState == null) {
            minLength = intent.getIntExtra("min_length", minLength)
            inputType = intent.getStringExtra("type")
            setInitialState()
        } else {
            minLength = savedInstanceState.getInt("min_length", minLength)
            inputType = savedInstanceState.getString("type")
            input = savedInstanceState.getString("input")
            val currInput = savedInstanceState.getString("curr_input")
            binding.editText.setText(currInput)
            val isConfirmState = savedInstanceState.getBoolean("is_confirm_state")
            if (isConfirmState) setConfirmState() else setInitialState()
        }
        if (inputType == "pin") {
            binding.editText.inputType = InputType.TYPE_CLASS_NUMBER or
                    InputType.TYPE_NUMBER_VARIATION_PASSWORD
        } else {
            binding.editText.inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {
            putInt("min_length", minLength)
            putString("type", inputType)
            putString("input", input)
            putString("curr_input", binding.editText.text.toString())
            putBoolean("is_confirm_state", input != null)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onCancel()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onContinuePressed() {
        input = binding.editText.text.toString()
        setConfirmState()
    }

    private fun onDonePressed() {
        if (binding.editText.text.toString() == input) {
            onSave()
        } else {
            binding.inputLayout.error = getString(R.string.password_mismatch_error)
        }
    }

    private fun setInitialState() {
        binding.apply {
            messageText.text = resources.getQuantityString(
                if (inputType == "pin") R.plurals.pin_digits else R.plurals.password_digits,
                minLength,
                minLength
            )
            actionButton.isEnabled = false
            actionButton.setText(R.string.continue_text)
            actionButton.setOnClickListener(firstFabListener)
            editText.addTextChangedListener(textChangeListener)
            editText.imeOptions = EditorInfo.IME_ACTION_NEXT
            editText.setOnEditorActionListener { _: TextView, actionId: Int, _: KeyEvent ->
                when (actionId) {
                    EditorInfo.IME_ACTION_NEXT -> {
                        if (editText.text!!.length >= minLength) onContinuePressed()
                        return@setOnEditorActionListener true
                    }
                    EditorInfo.IME_ACTION_DONE -> {
                        onDonePressed()
                        return@setOnEditorActionListener true
                    }
                }
                false
            }
        }

    }

    private fun setConfirmState() {
        binding.apply {
            editText.imeOptions = EditorInfo.IME_ACTION_DONE
            editText.removeTextChangedListener(textChangeListener)
            editText.setText("")
            actionButton.setOnClickListener(finalFabListener)
            actionButton.setText(android.R.string.ok)
            messageText.setText(if (inputType == "pin") R.string.confirm_pin else R.string.confirm_password)
        }
    }

    private fun onCancel() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun onSave() {
        val resultIntent = Intent()
        resultIntent.putExtra("input", input)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }


    private class TextChangeListener(var listener: (CharSequence) -> Unit) : TextWatcher {
        override fun afterTextChanged(s: Editable) {
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            listener(s)
        }

    }
}