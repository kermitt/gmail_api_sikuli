package fetch;

public class Caller {
	public static void showStack(String msg) {
		StackTraceElement[] ste = new Throwable().getStackTrace();
		String out = msg + "\n";
		for (int i = 1; i < ste.length; i++) {
			/* skip the 0th, i.e., the Caller object itself */
			int j = i - 1;
			String clazz = ste[i].getClassName();
			if (doNotNotPayAttention(clazz)) {
				String line = " ln: " + ste[i].getLineNumber();
				String name = " c: " + ste[i].getClassName();
				String method = " m: " + ste[i].getMethodName();
				out += j + line + name + method + "\n";
			}
		}
		write(out);
	}

	public static void verdict(boolean result, String msg) {
		StackTraceElement[] ste = new Throwable().getStackTrace();
		String line = " ln: " + ste[1].getLineNumber();
		String clazz = " c: " + ste[1].getClassName();
		String method = " m: " + ste[1].getMethodName();
		String passFail = result ? "PASS\t" : "FAIL\t";
		String out = passFail + msg + " |\t" + line + clazz + method;
		if (result == false) {
			System.err.println(out);
		} else {
			write(out);
		}
	}

	public static void simpleLog(String msg) {
		write(msg);
	}

	public static void note(String msg) {
		StackTraceElement[] ste = new Throwable().getStackTrace();

		String line = " ln: " + ste[1].getLineNumber();
		String clazz = " c: " + ste[1].getClassName();
		String method = " m: " + ste[1].getMethodName();

		String out = "****\t" + msg + " |\t" + line + clazz + method;

		write(out);
	}

	public static void log(boolean isDebug, String msg) {
		if (isDebug) {
			StackTraceElement[] ste = new Throwable().getStackTrace();

			String line = " ln: " + ste[1].getLineNumber();
			String clazz = " c: " + ste[1].getClassName();
			String method = " m: " + ste[1].getMethodName();

			String out = msg + " |\t" + line + clazz + method;

			write(out);
		}
	}

	public static void log(String msg) {
		StackTraceElement[] ste = new Throwable().getStackTrace();

		String line = " ln: " + ste[1].getLineNumber();
		String clazz = " c: " + ste[1].getClassName();
		String method = " m: " + ste[1].getMethodName();

		String out = msg + " |\t" + line + clazz + method;

		write(out);
	}

	private static boolean doNotNotPayAttention(String candidate) {
		if (candidate.startsWith("sun") || candidate.startsWith("com.sun") || candidate.startsWith("com.ibm")
				|| candidate.startsWith("java.") || candidate.startsWith("org.junit")
				|| candidate.startsWith("org.eclipse")) {
			return false;
		}
		return true;
	}

	private static void write(String s) {
		System.out.println(s);
	}
}
