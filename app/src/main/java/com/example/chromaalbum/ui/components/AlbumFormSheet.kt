package com.example.chromaalbum.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.chromaalbum.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumFormSheet(
    mode: AlbumSheetMode,
    onSave: (name: String, description: String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by rememberSaveable(mode) { mutableStateOf(if (mode is AlbumSheetMode.Edit) mode.album.name else "") }
    var description by rememberSaveable(mode) {
        mutableStateOf(if (mode is AlbumSheetMode.Edit) mode.album.description.orEmpty() else "")
    }
    var showNameError by rememberSaveable(mode) { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
                    .imePadding(),
        ) {
            val title =
                if (mode is AlbumSheetMode.Edit) {
                    stringResource(R.string.album_form_title_edit)
                } else {
                    stringResource(R.string.album_form_title_create)
                }
            Text(text = title)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (it.isNotBlank()) showNameError = false
                },
                label = { Text(stringResource(R.string.album_form_label_name)) },
                isError = showNameError,
                supportingText =
                    if (showNameError) {
                        { Text(stringResource(R.string.album_form_error_name_required)) }
                    } else {
                        null
                    },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.album_form_label_description)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (name.isBlank()) {
                        showNameError = true
                    } else {
                        onSave(name, description.takeIf { it.isNotBlank() })
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.album_form_button_save))
            }
        }
    }
}
