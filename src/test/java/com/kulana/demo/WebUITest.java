/*
 * Copyright 2013 Telegraph Hill (http://www.thpii.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kulana.demo;

import java.util.List;
import java.util.regex.Pattern;

import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.kulana.demo.gui.GmailHomePage;
import com.kulana.demo.gui.GmailLoginPage;
import com.kulana.demo.gui.GoogleMapsPage;
import com.kulana.core.foundation.UITest;
import com.kulana.core.foundation.crypto.CryptoTool;
import com.kulana.core.foundation.utils.HTML;
import com.kulana.core.foundation.utils.SpecialKeywords;
import com.kulana.core.foundation.webdriver.decorator.ExtendedWebElement;

public class WebUITest extends UITest
{
	@Test
	@Parameters({"email", "password"})
	public void testGmailLoginWithEncryptedData(String email, String password) throws Exception
	{
		GmailLoginPage gmailLoginPage = new GmailLoginPage(driver);
		gmailLoginPage.open();
		GmailHomePage gmailHomePage = gmailLoginPage.login(email, password);
		String decryptedEmail = (new CryptoTool()).decryptByPattern(email, Pattern.compile(SpecialKeywords.CRYPT));
		Assert.assertEquals(gmailHomePage.getUser(), decryptedEmail);
	}
	
	@Test
	@Parameters({"from", "to"})
	public void testGoogleMaps(String from, String to) throws Exception
	{
		GoogleMapsPage googleMapsPage = new GoogleMapsPage(driver);
		googleMapsPage.open();
		googleMapsPage.search(to);
		click(googleMapsPage.getDirectionsLink);
		Assert.assertEquals(googleMapsPage.pointBTextField.getAttribute(HTML.VALUE), to);
		List<ExtendedWebElement> routes = googleMapsPage.getDirections(from, to);
		Assert.assertTrue(routes.size() > 0, "No routes found!");
	}
}