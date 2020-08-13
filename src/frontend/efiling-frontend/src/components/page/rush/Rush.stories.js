import React from "react";
import { getDocumentsData } from "../../../modules/test-data/documentTestData";
import { getTestData } from "../../../modules/test-data/confirmationPopupTestData";
import { getCourtData } from "../../../modules/test-data/courtTestData";
import Rush from "./Rush";

export default {
  title: "Rush",
  component: Rush,
};

const confirmationPopup = getTestData();
const submissionId = "abc123";
const courtData = getCourtData();
const files = getDocumentsData();
const submissionFee = 25.5;

const payment = {
  confirmationPopup,
  submissionId,
  courtData,
  files,
  submissionFee,
};

export const Default = () => <Rush payment={payment} />;

export const Mobile = () => <Rush payment={payment} />;

Mobile.parameters = {
  viewport: {
    defaultViewport: "mobile2",
  },
};
