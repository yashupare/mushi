package com.mushi.app.modules.settings

import kotlin.test.Test
import kotlin.test.assertEquals

class CountryDefaultTest {

    @Test
    fun `takes the device region, falling back to the US`() {
        assertEquals(CountryCode.IN, countryFor("IN"))
        assertEquals(CountryCode.IN, countryFor("in"))
        assertEquals(CountryCode.US, countryFor(""))
        assertEquals(CountryCode.US, countryFor("419"))
    }
}
