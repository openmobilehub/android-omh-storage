/*
 * Copyright 2023 Open Mobile Hub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.openmobilehub.android.storage.sample.presentation.util

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.openmobilehub.android.storage.sample.R
import com.openmobilehub.android.storage.sample.databinding.ErrorDialogViewBinding

fun View.displayToast(message: String?, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this.context, message, duration).show()
}

fun View.displayToast(@StringRes message: Int, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this.context, message, duration).show()
}

fun Fragment.displayToast(message: String?, duration: Int = Toast.LENGTH_LONG) {
    view?.displayToast(message, duration)
}

fun Fragment.displayToast(@StringRes message: Int, duration: Int = Toast.LENGTH_LONG) {
    view?.displayToast(message, duration)
}

fun Fragment.navigateTo(@IdRes resId: Int) = NavHostFragment
    .findNavController(this)
    .navigate(resId)

fun Fragment.navigateTo(@IdRes resId: Int, args: Bundle?, navOptions: NavOptions?) = NavHostFragment
    .findNavController(this)
    .navigate(resId, args, navOptions)

fun View.displayErrorDialog(
    @StringRes title: Int,
    message: String,
    layoutInflater: LayoutInflater
) {
    context?.let { context ->
        val errorDialogView = ErrorDialogViewBinding.inflate(layoutInflater)
        errorDialogView.textError.text = String.format(
            context.getString(R.string.error_dialog_message),
            message
        )

        val errorDialogBuilder = AlertDialog.Builder(context)
            .setTitle(context.getString(title))
            .setPositiveButton(context.getString(R.string.error_dialog_ok)) { dialog, _ ->
                dialog.dismiss()
            }

        val errorDialog = errorDialogBuilder.create().apply {
            setCancelable(false)
            setView(errorDialogView.root)
        }

        errorDialog.show()
    }
}

fun Fragment.displayErrorDialog(
    message: String,
    @StringRes title: Int = R.string.error_dialog_title,
) {
    view?.displayErrorDialog(title, message, layoutInflater)
}
