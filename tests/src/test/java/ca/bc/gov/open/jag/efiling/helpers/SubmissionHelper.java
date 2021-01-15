package ca.bc.gov.open.jag.efiling.helpers;

import io.restassured.RestAssured;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.MultiPartSpecification;
import io.restassured.specification.RequestSpecification;

import java.io.File;
import java.text.MessageFormat;
import java.util.UUID;

public class SubmissionHelper {

    private static final String X_TRANSACTION_ID = "X-Transaction-Id";
    private static final String X_USER_ID = "X-User-Id";

    private SubmissionHelper() {}

    public static Response uploadADocumentRequest(UUID transactionId, String accessToken, String universalId,
                                                  MultiPartSpecification fileSpec) {


        RequestSpecification request = RestAssured.given().auth().preemptive()
                .oauth2(accessToken)
                .header(X_TRANSACTION_ID, transactionId)
                .header(X_USER_ID, universalId)
                .multiPart(fileSpec);

       return request.when().post("http://localhost:8080/submission/documents")
               .then()
               .extract().response();
    }

    public static Response generateUrlRequest(UUID transactionId, String universalId, String accessToken,
                                               String submissionId) {

        RequestSpecification request = RestAssured
                .given()
                .auth()
                .preemptive()
                .oauth2(accessToken)
                .contentType(ContentType.JSON)
                .header(X_TRANSACTION_ID, transactionId)
                .header(X_USER_ID, universalId)
                .body(PayloadHelper.generateUrlPayload("test-document.pdf"));

        return request
                .when()
                .post(MessageFormat.format("http://localhost:8080/submission/{0}/generateUrl", submissionId))
                .then()
                .extract()
                .response();

    }

    public static Response getSubmissionDetailsRequest(String accessToken, UUID transactionId,
                                                         String submissionId, String path) {


        RequestSpecification request = RestAssured
                .given()
                .auth()
                .preemptive()
                .oauth2(accessToken)
                .header(X_TRANSACTION_ID, transactionId);

        return request
                .when()
                .get(MessageFormat.format("http://localhost:8080/submission/{0}/{1}", submissionId, path))
                .then()
                .extract()
                .response();

    }

    public static MultiPartSpecification fileSpecBuilder(File file, String fileName, String mimeType) {

       return new MultiPartSpecBuilder(file).
                fileName(fileName).
                controlName("files").
                mimeType(mimeType).
                build();

    }

}
