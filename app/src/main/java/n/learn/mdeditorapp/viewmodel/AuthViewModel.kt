package n.learn.mdeditorapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import n.learn.mdeditorapp.data.remote.LoginRequest
import n.learn.mdeditorapp.data.remote.RegisterRequest
import n.learn.mdeditorapp.data.remote.RetrofitClient
import n.learn.mdeditorapp.util.SessionManager

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val session = SessionManager(application)
    private val api = RetrofitClient.api

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = AuthState.Error("введите email и пароль")
            return
        }
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                val resp = api.login(LoginRequest(email, password))
                if (resp.isSuccessful) {
                    resp.body()?.token?.let { session.saveToken(it) }
                    _state.value = AuthState.Success
                } else {
                    val msg = when (resp.code()) {
                        401 -> "неверный email или пароль"
                        429 -> "слишком много попыток, подождите 5 минут"
                        else -> "ошибка: ${resp.code()}"
                    }
                    _state.value = AuthState.Error(msg)
                }
            } catch (e: Exception) {
                _state.value = AuthState.Error("нет соединения с сервером")
            }
        }
    }

    fun register(email: String, password: String, confirmPassword: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = AuthState.Error("заполните все поля")
            return
        }
        if (password != confirmPassword) {
            _state.value = AuthState.Error("пароли не совпадают")
            return
        }
        if (password.length < 6) {
            _state.value = AuthState.Error("пароль минимум 6 символов")
            return
        }
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                val resp = api.register(RegisterRequest(email, password))
                if (resp.isSuccessful) {
                    _state.value = AuthState.Success
                } else {
                    val msg = when (resp.code()) {
                        409 -> "пользователь с таким email уже существует"
                        else -> "ошибка регистрации"
                    }
                    _state.value = AuthState.Error(msg)
                }
            } catch (e: Exception) {
                _state.value = AuthState.Error("нет соединения с сервером")
            }
        }
    }

    fun resetState() {
        _state.value = AuthState.Idle
    }
}
