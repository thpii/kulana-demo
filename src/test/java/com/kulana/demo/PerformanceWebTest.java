package com.kulana.demo;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;

import com.kulana.core.foundation.PerformanceTest;
import com.kulana.core.foundation.crypto.CryptoTool;
import com.kulana.core.foundation.performance.PerformanceTestResult.Status;
import com.kulana.core.foundation.utils.SpecialKeywords;
import com.kulana.demo.gui.GmailHomePage;
import com.kulana.demo.gui.GmailLoginPage;

public class PerformanceWebTest extends PerformanceTest
{
	public PerformanceWebTest(WebDriver driver, CountDownLatch latch, Map<String, String> testParams)
	{
		super(driver, latch, testParams);
	}

	@Override
	public void executeTask(Map<String, String> testParams) throws Exception
	{
		/*start*/performanceTestResult.startSubTest("OpenGmail");
		GmailLoginPage gmailLoginPage = new GmailLoginPage(driver);
		gmailLoginPage.open();
		/*stop*/performanceTestResult.finishSubTest("OpenGmail", Status.PASS, null);
		
		/*start*/performanceTestResult.startSubTest("LoginGmail");
		GmailHomePage gmailHomePage = gmailLoginPage.login(testParams.get("email"), testParams.get("password"));
		String decryptedEmail = (new CryptoTool()).decryptByPattern(testParams.get("email"), Pattern.compile(SpecialKeywords.CRYPT));
		Assert.assertEquals(gmailHomePage.getUser(), decryptedEmail);
		/*stop*/performanceTestResult.finishSubTest("LoginGmail", Status.PASS, null);
	}
}
