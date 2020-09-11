Feature: User can upload additional document and delete the documents

 # @backend
  Scenario: Verify additional document can be uploaded, retrieved and deleted
     ## Call to upload the document ##
    Given initial document is posted to "DOCUMENT_SUBMISSION" with valid existing CSO account guid and a single pdf file
    When validated status code is 200 and content type
    Then verify submission id and document count is returned
    ## Call to generate url ##
    Given POST request is made to "GENERATE_URL_API" with client application, court details and navigation urls
    When validated status code is 200 and content type
    Then verify expiry date and eFiling url are returned with the submission id
    ## Call to get user details ##
    Given "SUBMISSION" id is submitted with GET request
    When validated status code is 200 and content type
    Then verify universal id, user details and account type values are returned and not empty
    And verify navigation urls are returned
    ## Call to update document ##
    Given second document is posted to "DOCUMENT_SUBMISSION"
    When validated status code is 200 and content type
    Then verify submission id and document count is returned
    ## Call to get updated document wth filename ##
    Given "SUBMISSION" id with filename is submitted with GET http request
 #   Then validated status code is 200 and content type is not json
    ## Call to update document properties ##
    Given "SUBMISSION" id with payload is submitted to upload the document properties
    When validated status code is 200 and content type
   # Then verify document properties are updated
    ## Call to delete document ##
    Given "SUBMISSION" id is submitted with DELETE http request
    When validated status code is 200
    ## Call to get updated document wth filename ##
 #   Given "SUBMISSION" id with filename is submitted with GET http request
#   Then validate status code is 404 and content type is json