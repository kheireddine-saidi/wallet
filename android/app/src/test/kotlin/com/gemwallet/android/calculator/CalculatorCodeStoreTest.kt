package com.gemwallet.android.calculator

import android.content.SharedPreferences
import io.mockk.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CalculatorCodeStoreTest {

    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private val data = mutableMapOf<String, Any?>()
    private lateinit var store: CalculatorCodeStore

    @Before
    fun setUp() {
        data.clear()
        prefs = mockk(relaxed = true)
        editor = mockk(relaxed = true)

        every { prefs.edit() } returns editor
        every { editor.putString(any(), any()) } answers {
            data[arg(0)] = arg(1)
            editor
        }
        every { editor.apply() } answers {}
        every { prefs.contains(any()) } answers { data.containsKey(arg(0)) }
        every { prefs.getString(any(), any()) } answers { data[arg(0)] as? String ?: arg(1) }

        store = CalculatorCodeStore(prefs)
    }

    @Test
    fun testValidCodeValidation() {
        assertFalse(store.isValidCode("123"))
        assertFalse(store.isValidCode("12345678901"))
        assertFalse(store.isValidCode("123a"))
        assertTrue(store.isValidCode("1234"))
        assertTrue(store.isValidCode("1234567890"))
    }

    @Test
    fun testSetAndVerifyCode() {
        assertFalse(store.hasCode())
        assertTrue(store.setCode("1337"))
        assertTrue(store.hasCode())

        assertTrue(store.verifyCode("1337"))
        assertFalse(store.verifyCode("1234"))
        assertFalse(store.verifyCode("13370"))

        for ((_, value) in data) {
            val str = value as? String ?: continue
            assertFalse("Plaintext code should not be persisted", str.contains("1337"))
        }
    }

    @Test
    fun testInvalidCodeRejection() {
        assertFalse(store.setCode("123"))
        assertFalse(store.hasCode())
    }
}
