package test;


import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.callAction;
import static play.test.Helpers.charset;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.contentType;
import static play.test.Helpers.status;

import org.junit.Test;

import play.mvc.Result;

public class HomeControllerTest {

	@Test
	public void testIndex() {
		Result result = callAction(controllers.routes.ref.HomeController.index());
	    assertThat(status(result)).isEqualTo(OK);
	    assertThat(contentType(result)).isEqualTo("text/html");
	    assertThat(charset(result)).isEqualTo("utf-8");
	    assertThat(contentAsString(result)).contains("home");
	}

}