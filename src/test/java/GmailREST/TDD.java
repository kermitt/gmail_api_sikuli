package GmailREST;

import fetch.Caller;
import fetch.GetActivationCode;
import fetch.NonRESTfulMain;

public class TDD {

	static boolean endToEndTest() {
		NonRESTfulMain bot = new NonRESTfulMain();
		String code = bot.getActivationCode();

		boolean isOk = false; 
		try { 
			if ( code.length() == 6) {
				Integer.parseInt(code); 
				isOk = true; 
			}
		} catch (Exception not_a_number) {
			isOk = false;
		}
		
		Caller.verdict(isOk,"FOUND: " + code );
		return isOk; 
	}

	static boolean getTheActivationCode() {
		GetActivationCode gac = new GetActivationCode();
		String body = gac.fetch();
		boolean isOk = body.length() > 0 ? true : false;
		Caller.verdict(isOk, "One activation email was pulled from google");
		return isOk;
	}

	static boolean getCodeFromString() {
		String body = "...Dear SPOUSE: You&#39;re almost finished! Type the activation code into the field on the verification screen, click Activate, and you&#39;re set! Activation code: 931311 This code will remain...";
		String expected = "931311";
		GetActivationCode gac = new GetActivationCode();
		String actual = gac.parse(body);
		boolean isOk = expected.equals(actual);

		Caller.verdict(isOk, "REGEX is OK");
		return isOk; 
	}

	public static void main(String... strings) {
		boolean isOk = true; 
		Caller.note("...Trying (3) gmail centric integration tests...");
		isOk &= getCodeFromString();
		isOk &= getTheActivationCode();
		isOk &= endToEndTest();
		Caller.note("+ ----------- +");
		Caller.verdict(isOk, "The end " );
	}
}