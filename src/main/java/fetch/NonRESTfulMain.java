package fetch;

public class NonRESTfulMain {
	public String getActivationCode() {
		GetActivationCode gac = new GetActivationCode();
		String code = gac.fetch();
		return code; 
	}

	public static void main(String... strings) {
		NonRESTfulMain bot = new NonRESTfulMain();
		String code = bot.getActivationCode();
		Caller.log("The end! Found " + code );
	}
}