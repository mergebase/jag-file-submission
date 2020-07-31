package ca.bc.gov.open.jag.efilingapi.submission.mappers;

import ca.bc.gov.open.jag.efilingapi.api.model.FilingPackage;
import ca.bc.gov.open.jag.efilingapi.api.model.GenerateUrlRequest;
import ca.bc.gov.open.jag.efilingapi.submission.models.Submission;
import ca.bc.gov.open.jag.efilingcommons.model.AccountDetails;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface SubmissionMapper {

    @Mapping(source = "submissionId", target = "id")
    @Mapping(source = "transactionId", target = "transactionId")
    @Mapping(source = "generateUrlRequest.navigation", target = "navigation")
    @Mapping(source = "generateUrlRequest.clientApplication", target = "clientApplication")
    @Mapping(source = "filingPackage", target = "filingPackage")
    @Mapping(source = "expiryDate", target = "expiryDate")
    Submission toSubmission(
            UUID submissionId,
            UUID transactionId,
            GenerateUrlRequest generateUrlRequest,
            FilingPackage filingPackage,
            long expiryDate);

}
