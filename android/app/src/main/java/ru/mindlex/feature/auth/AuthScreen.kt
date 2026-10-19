package ru.mindlex.feature.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.mindlex.ui.components.MindLexBackground
import ru.mindlex.ui.components.MindLexCard
import ru.mindlex.ui.components.MindLexTextField
import ru.mindlex.ui.components.PrimaryButton
import ru.mindlex.ui.theme.MechanicGradients

/**
 * Экран входа и регистрации.
 *
 * <p>Форма одна для двух режимов: переключатель меняет только подписи и набор полей.
 * При успешной аутентификации вызывается [onAuthenticated], а корневой граф навигации
 * сам реагирует на появление токена.</p>
 *
 * @param onAuthenticated вызывается после успешного входа или регистрации
 */
@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val error = state.error
    val submit: () -> Unit = { viewModel.submit(onAuthenticated) }

    MindLexBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))

            AuthLogo()

            Spacer(Modifier.height(18.dp))
            Text(
                text = "MindLex",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Тренажёр иностранной лексики",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(28.dp))

            MindLexCard {
                Text(
                    text = if (state.isRegisterMode) "Регистрация" else "Вход",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (state.isRegisterMode) {
                        "Создайте аккаунт, чтобы начать тренировки"
                    } else {
                        "Войдите, чтобы продолжить обучение"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(18.dp))
                MindLexTextField(
                    value = state.email,
                    onValueChange = viewModel::onEmailChange,
                    label = "E-mail",
                    keyboardType = KeyboardType.Email,
                )

                Spacer(Modifier.height(12.dp))
                MindLexTextField(
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = "Пароль",
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                    imeAction = if (state.isRegisterMode) ImeAction.Next else ImeAction.Done,
                    onImeAction = if (state.isRegisterMode) null else submit,
                )

                if (state.isRegisterMode) {
                    Spacer(Modifier.height(12.dp))
                    MindLexTextField(
                        value = state.displayName,
                        onValueChange = viewModel::onDisplayNameChange,
                        label = "Имя",
                        imeAction = ImeAction.Done,
                        onImeAction = submit,
                    )
                }

                if (error != null) {
                    Spacer(Modifier.height(16.dp))
                    AuthErrorBlock(message = error, onDismiss = viewModel::dismissError)
                }

                Spacer(Modifier.height(20.dp))
                PrimaryButton(
                    text = if (state.isRegisterMode) "Создать аккаунт" else "Войти",
                    onClick = submit,
                    loading = state.isLoading,
                )

                Spacer(Modifier.height(4.dp))
                TextButton(
                    onClick = viewModel::toggleMode,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = if (state.isRegisterMode) {
                            "Уже есть аккаунт? Войти"
                        } else {
                            "Нет аккаунта? Зарегистрироваться"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(
                text = "Демо-доступ: demo@mindlex.local / demo1234",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Логотип-монограмма: градиентный квадрат с буквой «M». */
@Composable
private fun AuthLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(92.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(MechanicGradients.ActiveRecall)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "M",
            style = MaterialTheme.typography.displaySmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** Заметный блок ошибки: подложка и рамка цветом [MaterialTheme.colorScheme.error]. */
@Composable
private fun AuthErrorBlock(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val errorColor = MaterialTheme.colorScheme.error
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = errorColor.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, errorColor.copy(alpha = 0.5f)),
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, top = 8.dp, end = 4.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.ErrorOutline,
                contentDescription = null,
                tint = errorColor,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = errorColor,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Скрыть сообщение",
                    tint = errorColor,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
