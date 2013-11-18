package com.kulana.demo.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import net.rcarz.jiraclient.Field;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;

import org.apache.log4j.Logger;
import org.testng.ITestResult;

import com.kulana.core.foundation.jira.IJiraUpdater;
import com.kulana.core.foundation.report.TestResultItem;
import com.kulana.core.foundation.report.email.EmailReportItemCollector;
import com.kulana.core.foundation.utils.Configuration;
import com.kulana.core.foundation.utils.Configuration.Parameter;
import com.kulana.core.foundation.utils.naming.TestNamingUtil;

public class CustomJiraUpdater implements IJiraUpdater
{
	protected static final Logger LOG = Logger.getLogger(CustomJiraUpdater.class);
	
	private static final String SEARCH_CRITERIA = "project='%s' AND summary~'%s'";
	
	@Override
	public void updateAfterTest(JiraClient jira, ITestResult result) 
	{
		if(!isConfigValid()) return;
		try
		{
			String jiraTicket = EmailReportItemCollector.pull(result).getJiraTicket();
			// Check if test description has JIRA id
			if(jiraTicket != null)
			{
				Issue issue = jira.getIssue(jiraTicket);
				addComment(issue, result);
			}
			else 
			{
				Issue.SearchResult sr = jira.searchIssues(String.format(SEARCH_CRITERIA, Configuration.get(Parameter.JIRA_PROJECT), TestNamingUtil.getCanonicalTestName(result)));
				
				if(sr.issues.isEmpty() && Configuration.getBoolean(Parameter.JIRA_CREATE_NEW_TICKET))
				{
					if(result.getStatus() != 1)
					{
						Issue newIssue = jira.createIssue(Configuration.get(Parameter.JIRA_PROJECT_SHORT), "Bug")
								.field(Field.SUMMARY, TestNamingUtil.getCanonicalTestName(result))
								.execute();
						EmailReportItemCollector.pull(result).setJiraTicket(newIssue.getKey());
						addComment(newIssue, result);
					}
				}
				else if(!sr.issues.isEmpty())
				{
					EmailReportItemCollector.pull(result).setJiraTicket(sr.issues.get(0).getKey());
					addComment(sr.issues.get(0), result);
				}
			}
		}
		catch(Exception e)
		{
			LOG.error("Jira status not updated: " + e.getMessage());
		}
	}
	
	private void addComment(Issue issue, ITestResult result) throws JiraException
	{
		if("Done".equals(issue.getStatus().getName()))
		{
			// Reopen issue
			issue.transition().execute("Reopen and start progress");
		}
		String date = new SimpleDateFormat(Configuration.get(Parameter.DATE_FORMAT)).format(new Date());
		// PASS
		if(result.getStatus() == 1)
		{
			// If test passed - press done, and add comment
			issue.transition().execute("Done");
			issue.addComment("PASSED at " + date);
		}
		// FAIL
		else
		{
			// If test failed - populated description with details and add comment
			issue.update().field(Field.DESCRIPTION, result.getThrowable().getMessage()).execute();
			issue.addComment("FAILED at " + date);
		}
	}
	
	private boolean isConfigValid()
	{
		return !Configuration.isNull(Parameter.JIRA_URL) && !Configuration.isNull(Parameter.JIRA_USER) && !Configuration.isNull(Parameter.JIRA_PASSWORD);
	}

	@Override
	public void updateAfterSuite(JiraClient jira, List<TestResultItem> results) throws Exception
	{
		// Do nothing after test suite
	}
}
