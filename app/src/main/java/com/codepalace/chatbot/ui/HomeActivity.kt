package com.codepalace.chatbot.ui

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.codepalace.chatbot.R
import com.codepalace.chatbot.vm.HomeViewModel
import java.io.IOException


class HomeActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            viewModel.audioPermissionReady.value = isGranted
            if (isGranted) {
                viewModel.enableWakeWord(this@HomeActivity, true)
            }
            viewModel.refreshState()
        }

    private val viewModel: HomeViewModel by lazy {
        HomeViewModel(requestPermissionLauncher, application)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshState()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // A surface container using the 'background' color from the theme
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = colorResource(id = R.color.color_black_l),
            ) {
                val context = LocalContext.current
                Box {
                    Column {
                        Toolbar()
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 40.dp, vertical = 45.dp),
                            verticalArrangement = Arrangement.spacedBy(22.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            item {
                                ActionItemView(
                                    bgColor = colorResource(id = R.color.color_pink),
                                    @Composable {
                                        Text(
                                            text = getString(R.string.str_tip_install),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight(400),
                                            color = colorResource(id = R.color.color_white),
                                        )
                                    },
                                    if (viewModel.chatGptInstalled.value) getString(R.string.str_action_install) else getString(
                                        R.string.str_install_chatgpt
                                    ),
                                    Modifier,
                                    { viewModel.checkGPT(context) },
                                    viewModel.chatGptInstalled.value,
                                )
                            }
                            item {
                                ActionItemView(
                                    bgColor = colorResource(id = R.color.color_orange),
                                    @Composable {
                                        Text(
                                            text = getString(R.string.str_tip_settings),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight(400),
                                            color = colorResource(id = R.color.color_white),
                                        )
                                    },
                                    getString(R.string.str_test_chatgpt),
                                    Modifier,
                                    { viewModel.openAssistantActivity(context) },
                                    viewModel.chatGptSettingReady.value,
                                )
                            }
                            item {
                                ActionItemView(
                                    bgColor = colorResource(id = R.color.color_brown),
                                    {
                                        if (viewModel.isSelfWakeWordReady.value) @Composable {
                                            PlayAudioText()
                                        } else @Composable {
                                            Text(
                                                text = getString(R.string.str_tip_wake_word_self_first)
                                                        + getString(R.string.str_tip_wake_word_google)
                                                        + getString(R.string.str_tip_wake_word_jarvis)
                                                        + getString(R.string.str_tip_wake_word_self_end),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight(400),
                                                color = colorResource(id = R.color.color_white),
                                            )
                                        }
                                    },
                                    getString(R.string.str_action_wake_word),
                                    Modifier,
                                    { viewModel.toggleWakeWord(context) },
                                    viewModel.wakeWordReady.value,
                                    isWakeEngine = true,
                                )
                            }

                            item {
                                ActionItemView(
                                    bgColor = colorResource(id = R.color.color_purple),
                                    @Composable {
                                        Text(
                                            text = getString(R.string.str_tip_assistant),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight(400),
                                            color = colorResource(id = R.color.color_white),
                                        )
                                    },
                                    if (viewModel.isDefaultAssistant.value) getString(R.string.str_action_toggle_assistant) else getString(
                                        R.string.str_action_set_assistant
                                    ),
                                    Modifier,
                                    { viewModel.setAppAsVoiceAssistant(context) },
                                    viewModel.isDefaultAssistant.value,
                                )
                            }
                        }
                    }
                    if (viewModel.showWakeWordDialog.value) {
                        Dialog(onDismissRequest = { viewModel.showWakeWordDialog.value = false }) {
                            InputKeyDialog(
                                Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 200.dp)
                                    .background(
                                        colorResource(id = R.color.color_black_l),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp)
                            ) { key ->
                                viewModel.accessKey.value = key
                                viewModel.inputWakeWordKey(context)
                                viewModel.showWakeWordDialog.value = false
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun InputKeyDialog(modifier: Modifier = Modifier, action: (key: String) -> Unit = {}) {
        val context = LocalContext.current
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.str_tip_access_key),
                fontSize = 16.sp,
                fontWeight = FontWeight(400),
                color = colorResource(id = R.color.color_white),
                modifier = Modifier.noRippleClickable {
                    viewModel.getAccessKey(context)
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            var text by remember { mutableStateOf(viewModel.accessKey.value) }

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text(stringResource(R.string.str_tip_holder)) },
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 10.dp),
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 18.sp,
                    color = colorResource(id = R.color.color_white),
                    fontWeight = FontWeight.Medium
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(id = R.color.color_white),
                    unfocusedBorderColor = colorResource(id = R.color.color_grey),
                    cursorColor = colorResource(id = R.color.color_white),
                    focusedPlaceholderColor = colorResource(id = R.color.color_grey),
                    unfocusedPlaceholderColor = colorResource(id = R.color.color_grey),
                    disabledPlaceholderColor = colorResource(id = R.color.color_grey),
                    errorPlaceholderColor = colorResource(id = R.color.color_grey),
                ),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                Modifier
                    .wrapContentHeight()
                    .background(
                        colorResource(id = R.color.color_dark_green),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(vertical = 12.dp, horizontal = 24.dp)
                    .noRippleClickable {
                        if (text.isNotEmpty()) {
                            action.invoke(text)
                        } else {
                            Toast
                                .makeText(
                                    context,
                                    R.string.str_key_valid,
                                    Toast.LENGTH_SHORT
                                )
                                .show()
                        }
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.confirm_key),
                    textAlign = TextAlign.Center,
                    color = colorResource(id = R.color.color_white),
                    fontSize = 16.sp,
                    fontWeight = FontWeight(500),
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }

    @Composable
    fun Toolbar(modifier: Modifier = Modifier) {
        val context = LocalContext.current
        Row(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .defaultMinSize(minHeight = 46.dp)
                .background(color = colorResource(id = R.color.color_black))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //toolbar
            Text(
                text = stringResource(id = R.string.app_name),
                fontSize = 20.sp,
                fontWeight = FontWeight(500),
                color = colorResource(id = R.color.color_white),
                modifier = Modifier
                    .weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                painter = painterResource(id = R.drawable.baseline_share_24),
                tint = colorResource(id = R.color.color_white),
                contentDescription = "down",
                modifier = Modifier
                    .size(24.dp)
                    .noRippleClickable {
                        viewModel.doShare(context)
                    }
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box {
                var expanded by remember { mutableStateOf(false) }
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_more_vert_24),
                    tint = colorResource(id = R.color.color_white),
                    contentDescription = "down",
                    modifier = Modifier
                        .size(28.dp)
                        .noRippleClickable {
                            expanded = true
                        }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    DropdownMenuItem(text = {
                        Text(
                            getString(R.string.privacy_policy),
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }, onClick = {
                        expanded = false
                        viewModel.privacyPolicy(context)
                    })
                    DropdownMenuItem(text = {
                        Text("Tutorial Video", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }, onClick = {
                        expanded = false
                        viewModel.watchVideo(context)
                    })
                    DropdownMenuItem(text = {
                        Text("Feedback", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                    }, onClick = {
                        expanded = false
                        viewModel.emailMe(context)
                    })
                }
            }
        }
    }

    @Composable
    fun ActionItemView(
        bgColor: Color,
        description: @Composable () -> Unit,
        actionText: String,
        modifier: Modifier = Modifier,
        action: (isReady: Boolean) -> Unit = {},
        isReady: Boolean = false,
        isWakeEngine: Boolean = false,
    ) {
        Column(modifier) {
            description.invoke()
            if (isWakeEngine) {
                val context = LocalContext.current
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    stringResource(R.string.wake_word_engine), modifier = Modifier,
                    fontSize = 12.sp,
                    fontWeight = FontWeight(400),
                    color = colorResource(id = R.color.color_white),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier
                            .clickable { viewModel.setWakeWordMicroEngine(context, true) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        RadioButton(
                            selected = viewModel.isSelfWakeWordReady.value,
                            onClick = { viewModel.setWakeWordMicroEngine(context, true) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = colorResource(id = R.color.color_white),
                                unselectedColor = colorResource(id = R.color.color_white),
                            )
                        )
                        Text(
                            stringResource(R.string.self_developed), modifier = Modifier,
                            fontSize = 14.sp,
                            fontWeight = FontWeight(400),
                            color = colorResource(id = R.color.color_white),
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { viewModel.setWakeWordMicroEngine(context, false) }
                    ) {
                        RadioButton(
                            selected = !viewModel.isSelfWakeWordReady.value,
                            onClick = { viewModel.setWakeWordMicroEngine(context, false) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = colorResource(id = R.color.color_white),
                                unselectedColor = colorResource(id = R.color.color_white),
                            )
                        )
                        Text(
                            stringResource(R.string.picovoice), modifier = Modifier,
                            fontSize = 14.sp,
                            fontWeight = FontWeight(400),
                            color = colorResource(id = R.color.color_white),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(bgColor, RoundedCornerShape(12.dp))
                    .padding(16.dp)
                    .noRippleClickable {
                        action.invoke(isReady)
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = actionText,
                    textAlign = TextAlign.Center,
                    color = colorResource(id = R.color.color_white),
                    fontSize = 18.sp,
                    fontWeight = FontWeight(500),
                    modifier = Modifier
                        .weight(1f)
                )
                Icon(
                    painter = painterResource(id = if (isReady) R.drawable.toggle_on else R.drawable.toggle_off),
                    tint = colorResource(id = if (isReady) R.color.color_green else R.color.color_grey),
                    contentDescription = "down",
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }


    @Composable
    fun PlayAudioText() {
        val context = LocalContext.current
        var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

        DisposableEffect(Unit) {
            onDispose {
                mediaPlayer?.release()
            }
        }
        val normalStyle = SpanStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight(400),
            color = colorResource(id = R.color.color_white)
        )
        val audioStyle = SpanStyle(
            fontSize = 18.sp,
            fontWeight = FontWeight(500),
            color = colorResource(id = R.color.color_white),
            textDecoration = TextDecoration.Underline,
        )

        val annotatedString = buildAnnotatedString {

            withStyle(
                style = normalStyle
            ) {
                append(getString(R.string.str_tip_wake_word_self_first))
            }
            pushStringAnnotation(
                tag = getString(R.string.str_tip_wake_word_self_gpt),
                annotation = "hey_gpt.wav"
            )
            withStyle(
                style = audioStyle
            ) {
                append(getString(R.string.str_tip_wake_word_self_gpt))
            }
            pop()
            withStyle(
                style = normalStyle
            ) {
                append(getString(R.string.str_tip_wake_word_self_end))
            }
        }

        ClickableText(
            text = annotatedString,
            modifier = Modifier,
            onClick = { offset ->
                annotatedString.getStringAnnotations(
                    tag = getString(R.string.str_tip_wake_word_self_gpt),
                    start = offset,
                    end = offset
                ).firstOrNull()?.let {
                    playAudio(context, it.item, mediaPlayer) { newMediaPlayer ->
                        mediaPlayer = newMediaPlayer
                    }
                }
            }
        )
    }

    private fun playAudio(
        context: android.content.Context,
        fileName: String,
        mediaPlayer: MediaPlayer?,
        onMediaPlayerChange: (MediaPlayer) -> Unit
    ) {
        mediaPlayer?.release()
        val newMediaPlayer = MediaPlayer()

        try {
            val assetFileDescriptor = context.assets.openFd(fileName)
            newMediaPlayer.apply {
                setDataSource(
                    assetFileDescriptor.fileDescriptor,
                    assetFileDescriptor.startOffset,
                    assetFileDescriptor.length
                )
                prepare()
                start()
            }
            onMediaPlayerChange(newMediaPlayer)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}