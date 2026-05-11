package org.remess.mdeditor

import org.junit.Assert.*
import org.junit.Test

// Тест-кейсы №1, 2, 3 — регистрация и авторизация (валидация полей)
class AuthValidationTest {

    private fun validateRegister(email: String, password: String, confirm: String): String? {
        if (email.isBlank() || password.isBlank()) return "заполните все поля"
        if (password != confirm) return "пароли не совпадают"
        if (password.length < 6) return "пароль минимум 6 символов"
        return null
    }

    private fun validateLogin(email: String, password: String): String? {
        if (email.isBlank() || password.isBlank()) return "введите email и пароль"
        return null
    }

    // TC1 — Регистрация нового пользователя

    @Test
    fun tc1_register_validData_noError() {
        val result = validateRegister("test@mail.ru", "qwerty1", "qwerty1")
        assertNull(result)
    }

    @Test
    fun tc1_register_emptyEmail_returnsError() {
        val result = validateRegister("", "qwerty1", "qwerty1")
        assertNotNull(result)
    }

    @Test
    fun tc1_register_emptyPassword_returnsError() {
        val result = validateRegister("test@mail.ru", "", "")
        assertNotNull(result)
    }

    @Test
    fun tc1_register_passwordMismatch_returnsError() {
        val result = validateRegister("test@mail.ru", "qwerty1", "qwerty2")
        assertEquals("пароли не совпадают", result)
    }

    @Test
    fun tc1_register_shortPassword_returnsError() {
        val result = validateRegister("test@mail.ru", "abc", "abc")
        assertEquals("пароль минимум 6 символов", result)
    }

    // TC2 — Авторизация с корректными данными

    @Test
    fun tc2_login_validCredentials_noError() {
        val result = validateLogin("test@mail.ru", "qwerty1")
        assertNull(result)
    }

    // TC3 — Авторизация с некорректными данными

    @Test
    fun tc3_login_emptyPassword_returnsError() {
        val result = validateLogin("test@mail.ru", "")
        assertNotNull(result)
    }

    @Test
    fun tc3_login_emptyEmail_returnsError() {
        val result = validateLogin("", "qwerty1")
        assertNotNull(result)
    }

    @Test
    fun tc3_login_bothEmpty_returnsError() {
        val result = validateLogin("", "")
        assertEquals("введите email и пароль", result)
    }
}
