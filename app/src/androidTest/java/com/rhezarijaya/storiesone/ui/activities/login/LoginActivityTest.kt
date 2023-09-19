package com.rhezarijaya.storiesone.ui.activities.login

import androidx.paging.ExperimentalPagingApi
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.rhezarijaya.storiesone.R
import com.rhezarijaya.storiesone.data.network.APIConfig
import com.rhezarijaya.storiesone.ui.activities.main.MainActivity
import com.rhezarijaya.storiesone.util.EspressoIdlingResource
import com.rhezarijaya.storiesone.util.JsonConverter
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalPagingApi
@LargeTest
@RunWith(AndroidJUnit4::class)
class LoginActivityTest {
    private val mockWebServer = MockWebServer()

    @get:Rule
    val activity = ActivityScenarioRule(LoginActivity::class.java)

    @Before
    fun setup() {
        val port = 8000

        mockWebServer.start(port)
        APIConfig.TESTING_BASE_URL = "http://127.0.0.1:$port"

        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun loginAndLogoutSuccess() {
        // untuk mendapatkan context, saya mendapat referensi dari materi
        // Latihan UI Test Menggunakan Espresso kelas BFAA
        val context = InstrumentationRegistry.getInstrumentation().context

        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(JsonConverter.readStringFromAssets("login_success.json"))
        mockWebServer.enqueue(mockResponse)

        // cek apakah semua edit text dan button login tampil di layar
        onView(withId(R.id.ed_login_email)).check(matches(isDisplayed()))
        onView(withId(R.id.ed_login_password)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_login)).check(matches(isDisplayed()))

        // lakukan pengetikan email dan password
        onView(withId(R.id.ed_login_email)).perform(
            typeText("seseorang@example.com"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.ed_login_password)).perform(typeText("12345678"), closeSoftKeyboard())

        Intents.init()

        // lakukan klik pada button login
        onView(withId(R.id.btn_login)).perform(click())

        // cek apakah ada intent ke main activity
        intended(hasComponent(MainActivity::class.java.name))

        // cek apakah recycler view di main activity tampil
        onView(withId(R.id.rv_stories)).check(matches(isDisplayed()))

        // membuka action bar dan klik ke menu logout
        openActionBarOverflowOrOptionsMenu(context)
        onView(withText("Logout")).perform(click())

        // lakukan klik pada tombol yes di alert dialog
        onView(withText("Yes")).perform(click())

        // cek apakah ada intent ke login activity
        intended(hasComponent(LoginActivity::class.java.name))

        // cek apakah semua edit text dan button login tampil di layar
        onView(withId(R.id.ed_login_email)).check(matches(isDisplayed()))
        onView(withId(R.id.ed_login_password)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_login)).check(matches(isDisplayed()))
    }
}