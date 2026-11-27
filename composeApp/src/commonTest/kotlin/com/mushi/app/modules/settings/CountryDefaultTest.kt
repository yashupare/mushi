package com.mushi.app.modules.settings

import kotlin.test.Test
import kotlin.test.assertEquals

class CountryDefaultTest {

    @Test
    fun `a new install is set to India`() {
        assertEquals(CountryCode.IN, UserSettings().country)
    }

    @Test
    fun `the old built-in Bangladesh becomes India once`() {
        val old = UserSettings(country = CountryCode.BD)

        assertEquals(CountryCode.IN, old.withIndiaDefault().country)
    }

    @Test
    fun `a country chosen since then stays`() {
        val chosen = UserSettings(country = CountryCode.BD, indiaDefaultApplied = true)

        assertEquals(CountryCode.BD, chosen.withIndiaDefault().country)
        assertEquals(CountryCode.US, UserSettings(country = CountryCode.US).withIndiaDefault().country)
    }
}
