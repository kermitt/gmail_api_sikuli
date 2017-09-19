package fetch;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.*;
import com.google.api.services.gmail.Gmail;

import java.util.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* 
 * https://developers.google.com/gmail/api/quickstart/java
 * NOTE1: This is a MAVEN project. 
 * The google docs are a gradle example...
 * Beware1: Where the TOKEN is stored will be determined by the DATA_STORE_DIR - If you change the SCOPES variable then first manually clear out whatever is in the DATA_STORE_DIR.
 * Beware2: get your client-secret.json from google and store that in your /resources dir
 * NOTE2: Step1 gets _pointers_ to a list of emails that satisfy the criteria 'from:Regence in:inbox' for my test user
 * The 'messages' returned from step1 merely contain {id:'foo',threadId:'bar'}. Use the ID to fetch the actual email in STEP2.
 * From step2 fetch the activation guid and return that. The end. 
 * NOTE3: use the 'used_codes.txt' to prevent activation code re-usage
 */
public class GetActivationCode {
	public Set<String> used = new HashSet<>();

	private final String APPLICATION_NAME = "GetActivationCode";
	private final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"),
			"credentials/" + APPLICATION_NAME);
	private FileDataStoreFactory DATA_STORE_FACTORY;
	private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private HttpTransport HTTP_TRANSPORT;
	private final List<String> SCOPES = Arrays.asList(GmailScopes.GMAIL_LABELS, GmailScopes.GMAIL_READONLY);
	{
		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
	}
	// + ------------------------------------------ +
	public void _reset() { 
		// for use in RESTful integration testing for the python client side - w/o this then this then the application would need to be stopped and restarted after end usage/consumption of an activation code... 
		used = new HashSet<>();
	}
	private List<Message> listMessagesMatchingQuery(Gmail service, String userId, String query) throws IOException {

		ListMessagesResponse response = service.users().messages().list(userId).setQ(query).execute();

		List<Message> messages = new ArrayList<Message>();
		while (response.getMessages() != null) {
			messages.addAll(response.getMessages());
			if (response.getNextPageToken() != null) {
				String pageToken = response.getNextPageToken();
				response = service.users().messages().list(userId).setQ(query).setPageToken(pageToken).execute();
			} else {
				break;
			}
		}
		return messages;
	}

	private String getMessage(Gmail service, String userId, String messageId) throws IOException {

		Message message = service.users().messages().get(userId, messageId).execute();
		return message.getSnippet();
	}

	private Credential authorize() throws IOException {
		InputStream in = GetActivationCode.class.getResourceAsStream("/client_secret.json");
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES).setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline").build();
		Credential credential = null;
		try {
			credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return credential;
	}

	private Gmail getGmailService() throws IOException {
		Credential credential = authorize();
		return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
	}

	public String parse(String body) {
		String regex = "\\s\\d\\d\\d\\d\\d\\d\\s";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(body);
		String actual = "no activation code was found";
		if (m.find()) {
			actual = m.group();
			actual = actual.trim();
		}
		return actual;
	}

	public String fetch() {
		
		List<String> codes = new ArrayList<>();
		Gmail service;
		try {
			service = getGmailService();
			String user = "me";

			// ought to have only 1 result
			List<Message> candidates = listMessagesMatchingQuery(service, user, "from:regence in:inbox");

			// Sadly, the Message candidate objects' 'getInternalDate()' returns
			// a null...
			// So, no date for you
			for (Message candidate : candidates) {
				// Long internalDate = candidate.getInternalDate(); //sad that
				// this does not work
				String raw = getMessage(service, user, candidate.getId());
				String code = parse(raw);
				if (code != null && !used.contains(code)) {
					used.add(code);
					codes.add(code);
				} else {
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		Caller.log("found count is " + codes.size() + " already seen count is "  + used.size());
		if ( codes.size() > 1 ) {
			// Boo - too many were found. Better log into Testy's gmail account and delete some 
			return "TOO_MANY_FOUND";
		} else if ( codes.size() == 1 ) { 
			// Yay
			return codes.get(0);
		} else { 
			// Boo - maybe try again in a few seconds
			return "NONE_FOUND";
		}
	}
}