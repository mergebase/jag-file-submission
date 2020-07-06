/* eslint-disable react/require-default-props */
import React, { useState, useEffect } from "react";
import ConfirmationPopup, { TermsOfUse, Button } from "shared-components";
import { getContent } from "../../../modules/csoAccountAgreementContent";
import { propTypes } from "../../../types/propTypes";

const continueButton = {
  label: "Create CSO Account",
  styling: "normal-blue btn",
  onClick: () => {}
};

const content = getContent();

export default function CSOAccount({ confirmationPopup }) {
  const [termsAccepted, acceptTerms] = useState(false);
  const [continueBtnEnabled, setContinueBtnEnabled] = useState(false);

  useEffect(() => {
    if (termsAccepted) {
      setContinueBtnEnabled(true);
    } else {
      setContinueBtnEnabled(false);
    }
  }, [termsAccepted]);

  return (
    <>
      <div className="non-printable">
        <p>
          E-File Submission is a service to help you securely and electronically
          file documents with the Government of British Columbia Court Services
          Online (CSO).&nbsp;
          <a
            href="https://justice.gov.bc.ca/cso/about/index.do"
            target="_blank"
            rel="noopener noreferrer"
          >
            Learn more about CSO
          </a>
          .
        </p>
        <p>You need a CSO account to use E-File Submission.</p>

        <br />

        <h2>Create a Court Services Online (CSO) Account</h2>
        <p>
          The following information will be used to create your CSO account.
        </p>

        <br />
      </div>

      <TermsOfUse
        acceptTerms={() => acceptTerms(!termsAccepted)}
        content={content}
        heading="Electronic Service Agreement"
        confirmText="I accept the Service Agreement"
      />

      <section className="non-printable buttons pt-4">
        <ConfirmationPopup
          modal={confirmationPopup.modal}
          mainButton={confirmationPopup.mainButton}
          confirmButton={confirmationPopup.confirmButton}
          cancelButton={confirmationPopup.cancelButton}
        />
        <Button
          label={continueButton.label}
          onClick={continueButton.onClick}
          styling={continueButton.styling}
          disabled={!continueBtnEnabled}
        />
      </section>
    </>
  );
}

CSOAccount.propTypes = {
  confirmationPopup: propTypes.confirmationPopup
};