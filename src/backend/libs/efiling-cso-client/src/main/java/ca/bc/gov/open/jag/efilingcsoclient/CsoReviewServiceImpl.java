package ca.bc.gov.open.jag.efilingcsoclient;

import ca.bc.gov.ag.csows.filing.DocumentStatuses;
import ca.bc.gov.ag.csows.filing.FilingFacadeBean;
import ca.bc.gov.ag.csows.filing.status.FilingStatus;
import ca.bc.gov.ag.csows.filing.status.FilingStatusFacadeBean;
import ca.bc.gov.ag.csows.filing.status.NestedEjbException_Exception;
import ca.bc.gov.ag.csows.reports.Report;
import ca.bc.gov.ag.csows.reports.ReportService;
import ca.bc.gov.open.jag.efilingcommons.exceptions.EfilingReviewServiceException;
import ca.bc.gov.open.jag.efilingcommons.exceptions.EfilingStatusServiceException;
import ca.bc.gov.open.jag.efilingcommons.submission.EfilingReviewService;
import ca.bc.gov.open.jag.efilingcommons.submission.models.DeleteSubmissionDocumentRequest;
import ca.bc.gov.open.jag.efilingcommons.submission.models.FilingPackageRequest;
import ca.bc.gov.open.jag.efilingcommons.submission.models.review.ReviewFilingPackage;
import ca.bc.gov.open.jag.efilingcommons.utils.DateUtils;
import ca.bc.gov.open.jag.efilingcsoclient.mappers.FilePackageMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CsoReviewServiceImpl implements EfilingReviewService {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private final FilingStatusFacadeBean filingStatusFacadeBean;

    private final ReportService reportService;

    private final FilingFacadeBean filingFacadeBean;

    private final FilePackageMapper filePackageMapper;

    public CsoReviewServiceImpl(FilingStatusFacadeBean filingStatusFacadeBean, ReportService reportService, FilingFacadeBean filingFacadeBean, FilePackageMapper filePackageMapper) {

        this.filingStatusFacadeBean = filingStatusFacadeBean;
        this.reportService = reportService;
        this.filingFacadeBean = filingFacadeBean;
        this.filePackageMapper = filePackageMapper;

    }

    @Override
    public Optional<ReviewFilingPackage> findStatusByPackage(FilingPackageRequest filingPackageRequest) {

        try {

            logger.info("Calling soap findStatusBySearchCriteria by client id and package service ");

            FilingStatus filingStatus = filingStatusFacadeBean
                    .findStatusBySearchCriteria(null, null, null, null, null, null, filingPackageRequest.getPackageNo(), filingPackageRequest.getClientId(), null, null, null, null, BigDecimal.ONE, null);

            if (filingStatus.getFilePackages() == null || filingStatus.getFilePackages().isEmpty()) return Optional.empty();

            return Optional.of(filePackageMapper.toFilingPackage(filingStatus.getFilePackages().get(0)));

        } catch (NestedEjbException_Exception e) {

            throw new EfilingStatusServiceException("Exception while finding status", e.getCause());

        }

    }

    @Override
    public List<ReviewFilingPackage> findStatusByClient(FilingPackageRequest filingPackageRequest) {
        try {

            logger.info("Calling soap findStatusBySearchCriteria by client id service ");

            FilingStatus filingStatus = filingStatusFacadeBean
                    .findStatusBySearchCriteria(null, null, null, null, null, null, null, filingPackageRequest.getClientId(), null, null, null, null, BigDecimal.valueOf(100), null);

            if (filingStatus.getFilePackages().isEmpty()) return new ArrayList<>();

            return filingStatus.getFilePackages().stream().map(filePackageMapper::toFilingPackage).collect(Collectors.toList());

        } catch (NestedEjbException_Exception e) {

            throw new EfilingStatusServiceException("Exception while finding status list", e.getCause());

        }
    }

    @Override
    public Optional<byte[]> getSubmissionSheet(BigDecimal packageNumber) {

        logger.info("Calling soap to retrieve submission report ");

        Report report = new Report();
        report.setName(Keys.REPORT_NAME);
        report.getParameters().addAll(Arrays.asList(Keys.REPORT_PARAMETER, packageNumber.toPlainString()));

        byte[] result = reportService.runReport(report);

        if (result == null || result.length == 0) return Optional.empty();

        return Optional.of(result);
    }

    @Override
    public Optional<byte[]> getSubmittedDocument(BigDecimal packageNumber, String documentIdentifier) {
        // TODO: implement Object Repo Logic
        return Optional.empty();
    }

    @Override
    public void deleteSubmittedDocument(DeleteSubmissionDocumentRequest deleteSubmissionDocumentRequest) {
        try {

            logger.debug("Calling soap [updateDocumentStatus] ");

            DocumentStatuses documentStatuses = new DocumentStatuses();
            documentStatuses.setDocumentId(new BigDecimal(deleteSubmissionDocumentRequest.getDocumentId()));
            documentStatuses.setEntDtm(DateUtils.getCurrentXmlDate());
            documentStatuses.setDocumentStatusTypeCd(ca.bc.gov.open.jag.efilingcommons.Keys.WITHDRAWN_STATUS_CD);
            documentStatuses.setCreatedByPartId(deleteSubmissionDocumentRequest.getClientId());
            documentStatuses.setEntUserId(deleteSubmissionDocumentRequest.getClientId().toEngineeringString());
            documentStatuses.setUpdUserId(deleteSubmissionDocumentRequest.getClientId().toEngineeringString());
            documentStatuses.setStatusDtm(DateUtils.getCurrentXmlDate());

            filingFacadeBean.updateDocumentStatus(documentStatuses);

            logger.info(" [updateDocumentStatus] Successful  ");

        } catch (ca.bc.gov.ag.csows.filing.NestedEjbException_Exception e) {

            logger.error("Error in [updateDocumentStatus] call");

            throw  new EfilingReviewServiceException("Failed to updateDocumentStatus", e.getCause());

        }

        try {

            logger.debug("Calling soap [inactivateReferrals] ");

            filingFacadeBean.inactivateReferrals(deleteSubmissionDocumentRequest.getClientId(), DateUtils.getCurrentXmlDate(), deleteSubmissionDocumentRequest.getDocumentId());

            logger.info(" [updateDocumentStatus] Successful ");

        } catch (ca.bc.gov.ag.csows.filing.NestedEjbException_Exception e) {

            logger.error("Error in [inactivateReferrals] call");

            throw  new EfilingReviewServiceException("Failed in inactivateReferrals", e.getCause());

        }

        try {

            logger.debug("Calling soap [removePackageParties] ");

            filingFacadeBean.removePackageParties(deleteSubmissionDocumentRequest.getPackageNo());

            logger.info(" [updateDocumentStatus]  Successful ");

        } catch (ca.bc.gov.ag.csows.filing.NestedEjbException_Exception e) {

            logger.error("Error in [removePackageParties] call");

            throw  new EfilingReviewServiceException("Failed in removePackageParties", e.getCause());

        }

    }
}
