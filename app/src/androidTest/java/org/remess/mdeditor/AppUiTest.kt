package org.remess.mdeditor

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.remess.mdeditor.ui.screens.EditorScreen
import org.remess.mdeditor.ui.screens.HomeScreen
import org.remess.mdeditor.ui.screens.LoginScreen
import org.remess.mdeditor.ui.screens.RegisterScreen
import org.remess.mdeditor.ui.theme.MDEditorTheme

// UI тест-кейсы №1–10
@RunWith(AndroidJUnit4::class)
class AppUiTest {

    @get:Rule
    val rule = createComposeRule()

    // TC1 — Регистрация нового пользователя

    @Test
    fun tc1_registerScreen_showsRegisterButton() {
        rule.setContent {
            MDEditorTheme {
                RegisterScreen(onRegisterSuccess = {}, onBack = {})
            }
        }
        rule.onNodeWithText("Зарегистрироваться").assertIsDisplayed()
    }

    @Test
    fun tc1_registerScreen_hasEmailAndPasswordFields() {
        rule.setContent {
            MDEditorTheme {
                RegisterScreen(onRegisterSuccess = {}, onBack = {})
            }
        }
        rule.onNodeWithText("Email").assertIsDisplayed()
        rule.onNodeWithText("Подтвердите пароль").assertIsDisplayed()
    }

    // TC2 — Авторизация с корректными данными

    @Test
    fun tc2_loginScreen_showsLoginButton() {
        rule.setContent {
            MDEditorTheme {
                LoginScreen(onLoginSuccess = {}, onGoRegister = {})
            }
        }
        rule.onNodeWithText("Войти").assertIsDisplayed()
    }

    @Test
    fun tc2_loginScreen_hasEmailAndPasswordFields() {
        rule.setContent {
            MDEditorTheme {
                LoginScreen(onLoginSuccess = {}, onGoRegister = {})
            }
        }
        rule.onNodeWithText("Email").assertIsDisplayed()
        rule.onNodeWithText("Пароль").assertIsDisplayed()
    }

    // TC3 — Авторизация с некорректными данными

    @Test
    fun tc3_loginScreen_clickLoginWithEmptyFields_showsError() {
        rule.setContent {
            MDEditorTheme {
                LoginScreen(onLoginSuccess = {}, onGoRegister = {})
            }
        }
        rule.onNodeWithText("Войти").performClick()
        rule.onNodeWithText("введите email и пароль").assertIsDisplayed()
    }

    // TC4 — Создание нового документа

    @Test
    fun tc4_homeScreen_fabIsDisplayed() {
        rule.setContent {
            MDEditorTheme {
                HomeScreen(onOpenDocument = {}, onOpenRemoteDocs = {}, onLogout = {})
            }
        }
        rule.onNodeWithContentDescription("создать").assertIsDisplayed()
    }

    @Test
    fun tc4_homeScreen_fabClick_showsCreateDialog() {
        rule.setContent {
            MDEditorTheme {
                HomeScreen(onOpenDocument = {}, onOpenRemoteDocs = {}, onLogout = {})
            }
        }
        rule.onNodeWithContentDescription("создать").performClick()
        rule.onNodeWithText("Создать").assertIsDisplayed()
    }

    // TC5 — Удаление существующего документа

    @Test
    fun tc5_homeScreen_localTabIsDisplayed() {
        rule.setContent {
            MDEditorTheme {
                HomeScreen(onOpenDocument = {}, onOpenRemoteDocs = {}, onLogout = {})
            }
        }
        rule.onNodeWithText("Локальные").assertIsDisplayed()
    }

    // TC6 — Сохранение документа на сервере

    @Test
    fun tc6_editorScreen_hasUploadButton() {
        rule.setContent {
            MDEditorTheme {
                EditorScreen(docId = -1, onOpenChartBuilder = {}, onBack = {})
            }
        }
        rule.onNodeWithContentDescription("загрузить на сервер").assertIsDisplayed()
    }

    // TC7 — Загрузка документа с сервера

    @Test
    fun tc7_homeScreen_hasRemoteTab() {
        rule.setContent {
            MDEditorTheme {
                HomeScreen(onOpenDocument = {}, onOpenRemoteDocs = {}, onLogout = {})
            }
        }
        rule.onNodeWithText("На сервере").assertIsDisplayed()
    }

    // TC8 — Добавление фотографии в документ

    @Test
    fun tc8_editorScreen_hasPhotoButton() {
        rule.setContent {
            MDEditorTheme {
                EditorScreen(docId = -1, onOpenChartBuilder = {}, onBack = {})
            }
        }
        rule.onNodeWithText("Фото").assertIsDisplayed()
    }

    // TC9 — Создание формулы в документе

    @Test
    fun tc9_editorScreen_hasFormulaButton() {
        rule.setContent {
            MDEditorTheme {
                EditorScreen(docId = -1, onOpenChartBuilder = {}, onBack = {})
            }
        }
        rule.onNodeWithText("Формула").assertIsDisplayed()
    }

    // TC10 — Создание графика в документе

    @Test
    fun tc10_editorScreen_hasChartButton() {
        rule.setContent {
            MDEditorTheme {
                EditorScreen(docId = -1, onOpenChartBuilder = {}, onBack = {})
            }
        }
        rule.onNodeWithText("График").assertIsDisplayed()
    }
}
