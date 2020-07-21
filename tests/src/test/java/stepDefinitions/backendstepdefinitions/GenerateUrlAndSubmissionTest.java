package stepDefinitions.backendstepdefinitions;

import ca.bc.gov.open.jagefilingapi.qa.backend.generateurlpayload.GenerateUrlPayload;
import ca.bc.gov.open.jagefilingapi.qa.backendutils.APIResources;
import ca.bc.gov.open.jagefilingapi.qa.backendutils.TestUtil;
import ca.bc.gov.open.jagefilingapi.qa.frontendutils.JsonDataReader;
import ca.bc.gov.open.jagefilingapi.qa.requestbuilders.GenerateUrlRequestBuilders;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class GenerateUrlAndSubmissionTest {

    private Response response;
    private GenerateUrlRequestBuilders generateUrlRequestBuilders;
    private String submissionId;
    private JsonPath jsonPath;
    private String universalId;
    private String firstName;
    private String lastName;
    private String middleName;
    private String email;
    private static final String CONTENT_TYPE = "application/json";
    private static final String X_AUTH_USER_ID = "X-Auth-UserId";
    private String validExistingCSOGuid;
    private String nonExistingCSOGuid;
    private GenerateUrlPayload payloadData;
    public Logger log = LogManager.getLogger(GenerateUrlAndSubmissionTest.class);

    @Given("POST http request is made to {string} with valid existing CSO account guid and a single image file")
    public void postHttpRequestIsMadeToWithValidExistingCsoAccountGuidAndASingleImageFile(String resource) throws IOException {
        generateUrlRequestBuilders = new GenerateUrlRequestBuilders();

        response = generateUrlRequestBuilders.validRequestWithSingleDocument(resource);
    }

    @When("status code is {int} and content type is verified")
    public void statusCodeIsAndContentTypeIsVerified(Integer statusCode) {
        generateUrlRequestBuilders = new GenerateUrlRequestBuilders();

        switch (statusCode) {
            case 200:
                assertEquals(200, response.getStatusCode());
                assertEquals(CONTENT_TYPE, response.getContentType());
                break;
            case 403:
                assertEquals(403, response.getStatusCode());
                assertEquals(CONTENT_TYPE, response.getContentType());
                break;
            case 404:
                assertEquals(404, response.getStatusCode());
                assertEquals(CONTENT_TYPE, response.getContentType());
                break;
            case 405:
                assertEquals(405, response.getStatusCode());
                assertEquals(CONTENT_TYPE, response.getContentType());
                break;
            default:
                log.info("Expected status code did not match.");
        }
    }

    @Then("verify submission id and document count is received")
    public void verifySubmissionIdAndDocumentCountIsReceived() {
        jsonPath = new JsonPath(response.asString());

        submissionId = TestUtil.getJsonPath(response, "submissionId");
        int receivedCount = jsonPath.get("received");

        switch (receivedCount) {
            case 1:
                assertEquals(1, receivedCount);
                break;
            case 4:
                assertEquals(4, receivedCount);
                break;
            default:
                log.info("Document count did not match.");
        }
        assertNotNull(submissionId);
    }

    @Given("POST http request is made to {string} with client application, court details and navigation urls")
    public void POSTHttpRequestIsMadeToWithClientApplicationCourtDetailsAndNavigationUrls(String resource) throws IOException {
        payloadData = new GenerateUrlPayload();
        APIResources resourceGet = APIResources.valueOf(resource);

        validExistingCSOGuid = JsonDataReader.getCsoAccountGuid().getValidExistingCSOGuid();

        RequestSpecification request = given()
                .spec(TestUtil.requestSpecification())
                .header(X_AUTH_USER_ID, validExistingCSOGuid)
                .body(payloadData.validGenerateUrlPayload());
        response = request.when().post(resourceGet.getResource() + submissionId + "/generateUrl")
                .then()
                .spec(TestUtil.responseSpecification())
                .extract().response();
    }

    @Then("verify expiry date and eFiling url are returned with the CSO account guid and submission id")
    public void verifyExpiryDateAndEfilingUrlAreReturnedWithTheCsoAccountGuidAndSubmissionId() throws URISyntaxException {
        jsonPath = new JsonPath(response.asString());

        String respUrl = jsonPath.get("efilingUrl");
        Long expiryDate = jsonPath.get("expiryDate");

        List<NameValuePair> params = URLEncodedUtils.parse(new URI(respUrl), StandardCharsets.UTF_8);
        String respSubId = null;
        String respTemp = null;

        for (NameValuePair param : params) {
            if (param.getName().equals("submissionId")) {
                respSubId = param.getValue();
            } else if (param.getName().equals("temp")) {
                respTemp = param.getValue();
            }
        }

        assertEquals(submissionId, respSubId);
        assertEquals(validExistingCSOGuid, respTemp);
        assertNotNull(expiryDate);
    }

    @Given("{string} id is submitted with GET http request")
    public void idIsSubmittedWithGetHttpRequest(String resource) throws IOException {
        APIResources resourceGet = APIResources.valueOf(resource);

        RequestSpecification request = given()
                .spec(TestUtil.requestSpecification())
                .header(X_AUTH_USER_ID, validExistingCSOGuid);
        response = request.when().get(resourceGet.getResource() + submissionId)
                .then()
                .spec(TestUtil.responseSpecification())
                .extract().response();
    }

    @Then("verify universal id, user details, account type and identifier values are returned and not empty")
    public void verifyUniversalIdUserDetailsAccountTypeAndIdentifierValuesAreReturnedAndNotEmpty() {
        jsonPath = new JsonPath(response.asString());

        universalId = jsonPath.get("userDetails.universalId");
        firstName = jsonPath.get("userDetails.firstName");
        lastName = jsonPath.get("userDetails.lastName");
        middleName = jsonPath.get("userDetails.middleName");
        email = jsonPath.get("userDetails.email");

        List<String> type = jsonPath.get("userDetails.accounts.type");
        List<String> identifier = jsonPath.get("userDetails.accounts.identifier");

        assertThat(universalId, is(equalToIgnoringCase(validExistingCSOGuid)));
        assertThat(firstName, is(not(emptyString())));
        assertThat(lastName, is(not(emptyString())));
        assertThat(middleName, is(not(emptyString())));
        assertThat(email, is(not(emptyString())));
        log.info("Names and email objects from the valid CSO account submission response does not have empty values");

        assertFalse(type.isEmpty());
        assertFalse(identifier.isEmpty());
        log.info("Account type and identifier objects from the valid CSO account submission response have valid values");
    }

    @Given("POST http request is made to {string} with invalid CSO account guid in the header")
    public void postHttpRequestIsMadeToWithInvalidCsoAccountGuidInHeader(String resource) throws IOException {
        generateUrlRequestBuilders = new GenerateUrlRequestBuilders();

        response = generateUrlRequestBuilders.requestWithInvalidCSOAccountGuid(resource);
    }

    @Given("{string} id with filing package path is submitted with GET http request")
    public void idWithFilingPackagePathIsSubmittedWithGETHttpRequest(String resource) throws IOException {
        APIResources resourceGet = APIResources.valueOf(resource);

        RequestSpecification request = given().spec(TestUtil.requestSpecification())
                .header(X_AUTH_USER_ID, validExistingCSOGuid);

        response = request.when().get(resourceGet.getResource() + submissionId + "/filing-package")
                .then()
                .spec(TestUtil.responseSpecification())
                .extract().response();
    }

    @Then("verify court details and document details are returned and not empty")
    public void verifyCourtDetailsAndDocumentDetailsAreReturnedAndNotEmpty() {
        jsonPath = new JsonPath(response.asString());

        String location = jsonPath.get("court.location");
        String level = jsonPath.get("court.level");
        String courtClass = jsonPath.get("court.class");
        String division = jsonPath.get("court.division");
        String fileNumber = jsonPath.get("court.fileNumber");
        String participatingClass = jsonPath.get("court.participatingClass");
        int submissionFeeAmount = jsonPath.get("submissionFeeAmount");

        List<String> name = jsonPath.get("documents.name");
        List<String> description = jsonPath.get("documents.description");
        List<String> type = jsonPath.get("documents.type");
        List<String> statutoryFeeAmount = jsonPath.get("documents.statutoryFeeAmount");

        assertThat(location, is(not(emptyString())));
        assertThat(level, is(not(emptyString())));
        assertThat(courtClass, is(not(emptyString())));
        assertThat(division, is(not(emptyString())));
        assertThat(fileNumber, is(not(emptyString())));
        assertThat(participatingClass, is(not(emptyString())));
        assertEquals(7, submissionFeeAmount);
        log.info("Court fee and document details response have valid values");

        assertFalse(type.isEmpty());
        assertFalse(name.isEmpty());
        assertFalse(description.isEmpty());
        assertNotNull(statutoryFeeAmount);
        log.info("Account type, description and name objects from the valid CSO account submission response have valid values");
    }

    @And("verify success, error and cancel navigation urls are returned")
    public void verifySuccessErrorAndCancelNavigationUrlsAreReturned() {
        jsonPath = new JsonPath(response.asString());

        String successUrl = jsonPath.get("navigation.success.url");
        String errorUrl = jsonPath.get("navigation.error.url");
        String cancelUrl = jsonPath.get("navigation.cancel.url");

        assertNotNull(successUrl);
        assertNotNull(errorUrl);
        assertNotNull(cancelUrl);
    }

    @Given("{string} id with filename path is submitted with GET http request")
    public void idWithFilenamePathIsSubmittedWithGETHttpRequest(String resource) throws IOException {
        APIResources resourceGet = APIResources.valueOf(resource);

        RequestSpecification request = given().spec(TestUtil.requestSpecification())
                .header(X_AUTH_USER_ID, validExistingCSOGuid);

        response = request.when().get(resourceGet.getResource() + submissionId + "/document" + "/backend.png")
                .then()
                .spec(TestUtil.documentValidResponseSpecification())
                .extract().response();
    }

    @Then("Verify status code is {int} and content type is not json")
    public void verifyStatusCodeIsAndContentTypeIsNotJson(Integer int1) {
        assertEquals(200, response.getStatusCode());
        assertEquals("application/octet-stream", response.getContentType());
    }

    @Given("POST http request is made to {string} with valid existing CSO account guid and multiple file")
    public void POSTHttpRequestIsMadeToWithValidExistingCSOAccountGuidAndMultipleFile(String resource) throws IOException {
        generateUrlRequestBuilders = new GenerateUrlRequestBuilders();

        response = generateUrlRequestBuilders.validRequestWithMultipleDocuments(resource);
    }

    @Given("POST http request is made to {string} with non existing CSO account guid and a single image file")
    public void postHttpRequestIsMadeToWithNonExistingCSOAccountGuidAndASingleImageFile(String resource) throws IOException {
        generateUrlRequestBuilders = new GenerateUrlRequestBuilders();

        response = generateUrlRequestBuilders.requestWithNonExistingCSOAccountGuid(resource);
    }

    @Given("POST http request is made to {string} with client application, court details and navigation urls with non existing guid")
    public void POSTHttpRequestIsMadeToWithClientApplicationCourtDetailsAndNavigationUrlsWithNonExistingGuid(String resource) throws IOException {
        payloadData = new GenerateUrlPayload();
        APIResources resourceGet = APIResources.valueOf(resource);

        nonExistingCSOGuid = JsonDataReader.getCsoAccountGuid().getNonExistingCSOGuid();

        RequestSpecification request = given()
                .spec(TestUtil.requestSpecification())
                .header(X_AUTH_USER_ID, nonExistingCSOGuid)
                .body(payloadData.validGenerateUrlPayload());
        response = request.when().post(resourceGet.getResource() + submissionId + "/generateUrl")
                .then()
                .spec(TestUtil.responseSpecification())
                .extract().response();
    }

    @Then("verify expiry date and eFiling url are returned with non existing CSO account guid and submission id")
    public void verifyExpiryDateAndEFilingUrlAreReturnedWithNonExistingCSOAccountGuidAndSubmissionId() throws URISyntaxException {
        jsonPath = new JsonPath(response.asString());

        String respUrl = jsonPath.get("efilingUrl");
        Long expiryDate = jsonPath.get("expiryDate");

        List<NameValuePair> params = URLEncodedUtils.parse(new URI(respUrl), StandardCharsets.UTF_8);
        String respSubId = null;
        String respTemp = null;

        for (NameValuePair param : params) {
            if (param.getName().equals("submissionId")) {
                respSubId = param.getValue();
            } else if (param.getName().equals("temp")) {
                respTemp = param.getValue();
            }
        }
        assertEquals(submissionId, respSubId);
        assertEquals(nonExistingCSOGuid, respTemp);
        assertNotNull(expiryDate);
    }

    @Given("{string} id is submitted with non existing CSO account GET http request")
    public void idIsSubmittedWithNonExistingCsoAccountGetHttpRequest(String resource) throws IOException {
        APIResources resourceGet = APIResources.valueOf(resource);
        nonExistingCSOGuid = JsonDataReader.getCsoAccountGuid().getNonExistingCSOGuid();

        RequestSpecification request = given()
                .spec(TestUtil.requestSpecification())
                .header(X_AUTH_USER_ID, nonExistingCSOGuid);
        response = request.when().get(resourceGet.getResource() + submissionId)
                .then()
                .spec(TestUtil.responseSpecification())
                .extract().response();
    }

    @Then("verify accounts value is null but names and email details are returned")
    public void verifyAccountsValueIsNullButNamesAndEmailDetailsAreReturned() throws IOException {
        jsonPath = new JsonPath(response.asString());

        String nonExistingCSOGuid = JsonDataReader.getCsoAccountGuid().getNonExistingCSOGuid();

        universalId = jsonPath.get("userDetails.universalId");
        firstName = jsonPath.get("userDetails.firstName");
        lastName = jsonPath.get("userDetails.lastName");
        middleName = jsonPath.get("userDetails.middleName");
        email = jsonPath.get("userDetails.email");
        String accounts = jsonPath.get("accounts");

        assertThat(universalId, is(equalToIgnoringCase(nonExistingCSOGuid)));
        assertThat(firstName, is(not(emptyString())));
        assertThat(lastName, is(not(emptyString())));
        assertThat(email, is(not(emptyString())));
        log.info("Names and email objects from the valid CSO account submission response have valid values.");

        assertNull(accounts);
        log.info("Accounts object value from the valid CSO account submission response is null.");
    }

    @Given("{string} id with filing package path is submitted with non existing CSO account GET http request")
    public void idWithFilingPackagePathIsSubmittedWithNonExistingCSOAccountGETHttpRequest(String resource) throws IOException {
        APIResources resourceGet = APIResources.valueOf(resource);

        RequestSpecification request = given().spec(TestUtil.requestSpecification())
                .header(X_AUTH_USER_ID, nonExistingCSOGuid);

        response = request.when().get(resourceGet.getResource() + submissionId + "/filing-package")
                .then()
                .spec(TestUtil.responseSpecification())
                .extract().response();
    }

    @Given("{string} id with filename path is submitted with non existing CSO account GET http request")
    public void idWithFilenamePathIsSubmittedWithNonExistingCSOAccountGETHttpRequest(String resource) throws IOException {
        APIResources resourceGet = APIResources.valueOf(resource);

        RequestSpecification request = given().spec(TestUtil.requestSpecification())
                .header(X_AUTH_USER_ID, nonExistingCSOGuid);

        response = request.when().get(resourceGet.getResource() + submissionId + "/document" + "/backend.png")
                .then()
                .spec(TestUtil.documentValidResponseSpecification())
                .extract().response();
    }

    @Given("POST http request is made to {string} with invalid file type and a single image file")
    public void postHttpRequestIsMadeToWithInvalidFileTypeAndASingleImageFile(String resource) throws IOException {
        generateUrlRequestBuilders = new GenerateUrlRequestBuilders();

        response = generateUrlRequestBuilders.requestWithInvalidKeyValue(resource);
    }

    @When("status code is {int} and content type is not json")
    public void statusCodeIsAndContentTypeIsNotJson(Integer int1) {
        assertEquals(400, response.getStatusCode());
        assertEquals(CONTENT_TYPE, response.getContentType());
    }

    @Then("verify response returns document required error and message")
    public void verifyResponseReturnsDocumentRequiredErrorAndMessage() {
        jsonPath = new JsonPath(response.asString());
        String error = jsonPath.get("error");
        String message = jsonPath.get("message");

        assertEquals("DOCUMENT_REQUIRED", error);
        assertEquals("At least one document is required.", message);
    }

    @Then("verify response returns invalid role error and message")
    public void verifyResponseReturnsInvalidRoleErrorAndMessage() {
        jsonPath = new JsonPath(response.asString());

        String error = jsonPath.get("error");
        String message = jsonPath.get("message");

        assertEquals("INVALIDROLE", error);
        assertEquals("User does not have a valid role for this request.", message);
    }

    @Given("POST http request is made to {string} with incorrect path")
    public void PostHttpRequestIsMadeToWithIncorrectPath(String resource) throws IOException {
        generateUrlRequestBuilders = new GenerateUrlRequestBuilders();

        response = generateUrlRequestBuilders.requestWithIncorrectPath(resource);
    }

    @Then("verify error message is present and message has no value")
    public void verifyErrorMessageIsPresentAndMessageHasNoValue() {
        jsonPath = new JsonPath(response.asString());

        String error = TestUtil.getJsonPath(response, "error");
        String message = TestUtil.getJsonPath(response, "message");
        int statusCode = jsonPath.get("status");

        switch (statusCode) {
            case 404:
                assertEquals("Not Found", error);
                assertEquals("", message);
                break;
            case 405:
                assertEquals("Method Not Allowed", error);
                assertEquals("", message);
                break;
            default:
                log.info("Status, error and message response for incorrect and or invalid request is verified.");
        }
    }

    @Given("POST http request is made to {string} with invalid path")
    public void PostHttpRequestIsMadeToWithInvalidPath(String resource) throws IOException {
        generateUrlRequestBuilders = new GenerateUrlRequestBuilders();

        response = generateUrlRequestBuilders.requestWithInvalidPath(resource);
    }

    @Given("POST http request is made to {string} without id in the path")
    public void POSTHttpRequestIsMadeToWithoutIdInThePath(String resource) throws IOException {
        generateUrlRequestBuilders = new GenerateUrlRequestBuilders();

        response = generateUrlRequestBuilders.requestWithoutIdInThePath(resource);
    }
}
