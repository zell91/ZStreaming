package com.util.system;

public class Response {
	
	public enum Result{
		ERROR, SUCCESS
	}

	private Result result;
	private CharSequence message;
	
	public Response(Result result, CharSequence message) {
		this.result = result;
		this.message = message;
	}

	public CharSequence getMessage() {
		return message;
	}

	public void setMessage(CharSequence message) {
		this.message = message;
	}

	public Result getResult() {
		return result;
	}

	public void setResult(Result result) {
		this.result = result;
	}
}
