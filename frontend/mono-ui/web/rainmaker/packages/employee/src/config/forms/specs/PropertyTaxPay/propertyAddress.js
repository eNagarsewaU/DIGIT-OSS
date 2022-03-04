import { pincode, mohalla, street, colony, houseNumber, dummy } from "egov-ui-kit/config/forms/specs/PropertyTaxPay/utils/reusableFields";
import { fetchLocalizationLabel } from "egov-ui-kit/redux/app/actions";
import { CITY } from "egov-ui-kit/utils/endPoints";
import { fetchGeneralMDMSData, prepareFormData } from "egov-ui-kit/redux/common/actions";
import { setFieldProperty, handleFieldChange } from "egov-ui-kit/redux/form/actions";
import { fetchDropdownData, generalMDMSDataRequestObj, getGeneralMDMSDataDropdownName, getTranslatedLabel } from "egov-ui-kit/utils/commons";
import { getLocale, getTenantId } from "egov-ui-kit/utils/localStorageUtils";
import filter from "lodash/filter";
import get from "lodash/get";
import sortBy from "lodash/sortBy";
import set from "lodash/set";
import commonConfig from "../../../common";
import { initLocalizationLabels } from "egov-ui-kit/redux/app/utils";

const formConfig = {
  name: "propertyAddress",
  fields: {
    city: {
      id: "city",
      jsonPath: "PropertiesTemp[0].address.city",
      required: true,
      localePrefix: { moduleName: "tenant", masterName: "tenants" },
      type: "singleValueList",
      floatingLabelText: "CORE_COMMON_CITY",
      className: "pt-emp-property-address-city",
      disabled: true,
      errorStyle: { position: "absolute", bottom: -8, zIndex: 5 },
      fullWidth: true,
      hintText: "PT_COMMONS_SELECT_PLACEHOLDER",
      numcols: 6,
      dataFetchConfig: {
        // url: CITY.GET.URL,
        // action: CITY.GET.ACTION,
        // queryParams: [],
        // requestBody: {
        //   MdmsCriteria: {
        //     tenantId: commonConfig.tenantId,
        //     moduleDetails: [
        //       {
        //         moduleName: "tenant",
        //         masterDetails: [
        //           {
        //             name: "tenants",
        //           },
        //         ],
        //       },
        //     ],
        //   },
        // },
        // dataPath: ["MdmsRes.tenant.tenants"],
        dependants: [
          {
            fieldKey: "mohalla",
          },
        ],
      },
      updateDependentFields: ({ formKey, field, dispatch, state }) => {
        dispatch(prepareFormData("Properties[0].tenantId", field.value));
        // dispatch(
        //   prepareFormData(
        //     "Properties[0].address.city",
        //     filter(get(state, "common.cities"), (city) => {
        //       return city.code === field.value;
        //     })[0].name
        //   )
        // );
        // // dispatch(setFieldProperty("propertyAddress", "mohalla", "value", ""));
        // const moduleValue = field.value;
        // dispatch(fetchLocalizationLabel(getLocale(), moduleValue, moduleValue));
        // let requestBody = generalMDMSDataRequestObj(field.value);

        // dispatch(fetchGeneralMDMSData(requestBody, "PropertyTax", getGeneralMDMSDataDropdownName()));
        let requestBody = {
          MdmsCriteria: {
            tenantId: field.value,
            moduleDetails: [
              {
                moduleName: "PropertyTax",
                masterDetails: [
                  {
                    name: "Floor",
                  },
                  {
                    name: "OccupancyType",
                  },
                  {
                    name: "OwnerShipCategory",
                  },
                  {
                    name: "OwnerType",
                  },
                  {
                    name: "PropertySubType",
                  },
                  {
                    name: "PropertyType",
                  },
                  {
                    name: "SubOwnerShipCategory",
                  },
                  {
                    name: "UsageCategoryDetail",
                  },
                  {
                    name: "UsageCategoryMajor",
                  },
                  {
                    name: "UsageCategoryMinor",
                  },
                  {
                    name: "UsageCategorySubMinor",
                  },
                  {
                    name: "ConstructionType",
                  },
                  {
                    name: "Rebate",
                  },
                  {
                    name: "Interest",
                  },
                  {
                    name: "FireCess",
                  },
                  {
                    name: "RoadType",
                  },
                  {
                    name: "Thana",
                  },
                ],
              },
            ],
          },
        };
        dispatch(
          fetchGeneralMDMSData(requestBody, "PropertyTax", [
            "Floor",
            "OccupancyType",
            "OwnerShipCategory",
            "OwnerType",
            "PropertySubType",
            "PropertyType",
            "SubOwnerShipCategory",
            "UsageCategoryDetail",
            "UsageCategoryMajor",
            "UsageCategoryMinor",
            "UsageCategorySubMinor",
            "ConstructionType",
            "Rebate",
            "Penalty",
            "Interest",
            "FireCess",
            "RoadType",
            "Thana",
          ])
        );
        dispatch(
          fetchGeneralMDMSData(
            null,
            "BillingService",
            ["TaxPeriod", "TaxHeadMaster"],
            "",
            //  [{masterName:"TaxPeriod",filter:"[?(@.service=='PT')]"}, {masterName:"TaxHeadMaster",filter:"[?(@.service=='PT')]"}],
            field.value
          )
        );
      },
    },
    ...dummy,
    ...houseNumber,
    ...colony,
    ...street,
    ...mohalla,
    ...pincode,
    oldPID: {
      id: "oldpid",
      type: "textfield",
      className: "pt-old-pid-text-field-changes",
      text: "PT_SEARCH_BUTTON",
      // iconRedirectionURL: "https://pmidc.punjab.gov.in/propertymis/search.php",
      jsonPath: "Properties[0].oldPropertyId",
      floatingLabelText: "PT_PROPERTY_ADDRESS_EXISTING_PID",
      hintText: "PT_PROPERTY_ADDRESS_EXISTING_PID_PLACEHOLDER",
      numcols: 6,
      errorMessage: "PT_PROPERTY_DETAILS_PINCODE_ERRORMSG",
      errorStyle: { position: "absolute", bottom: -8, zIndex: 5 },
      toolTip: true,
      pattern: /^[^\$\"'<>?\\\\~`!@$%^+={}*,.:;“”‘’]{1,64}$/i,
      toolTipMessage: "PT_OLDPID_TOOLTIP_MESSAGE",
      maxLength: 64,
    },
    roadType: {
      id: "roadType",
      jsonPath: "Properties[0].propertyDetails[0].additionalDetails.roadType",
      //localePrefix: { moduleName: "PropertyTax", masterName: "RoadType" },
      type: "singleValueList",
      floatingLabelText: "PT_PROPERTY_ADDRESS_ROAD_TYPE",
      errorStyle: { position: "absolute", bottom: -8, zIndex: 5 },
      fullWidth: true,
      hintText: "PT_COMMONS_SELECT_PLACEHOLDER",
      numcols: 6,
      labelsFromLocalisation: true,
      menuHeight: "85px",
    },
    thanaType: {
      id: "Thana",
      jsonPath: "Properties[0].propertyDetails[0].additionalDetails.thana",
      //localePrefix: { moduleName: "PROPERTYTAX_THANA_", masterName: "Thana" },
      type: "singleValueList",
      floatingLabelText: "PT_PROPERTY_ADDRESS_THANA",
      errorStyle: { position: "absolute", bottom: -8, zIndex: 5 },
      fullWidth: true,
      hintText: "PT_COMMONS_SELECT_PLACEHOLDER",
      numcols: 6,
      labelsFromLocalisation: true,
    },
  },
  afterInitForm: (action, store, dispatch) => {
    try {
      let tenantId = getTenantId();
      let state = store.getState();
      const { localizationLabels } = state.app;
      const { cities, citiesByModule, loadMdmsData } = state.common;

      /* const roadTypeData =
        get(loadMdmsData, "PropertyTax.RoadType") &&
        Object.values(get(loadMdmsData, "PropertyTax.RoadType")).map((item, index) => {
          return { value: item.code, label: item.name };
        });

      dispatch(setFieldProperty("propertyAddress", "roadType", "dropDownData", roadTypeData));
      const locale = getLocale() || "en_IN";
      const localizationLabelsData = initLocalizationLabels(locale);
 */
      const locale = getLocale() || "en_IN";
      const localizationLabelsData = initLocalizationLabels(locale);
      const roadTypeData =
        get(loadMdmsData, "PropertyTax.RoadType") &&
        Object.values(get(loadMdmsData, "PropertyTax.RoadType")).map((item, index) => {
          return { value: item.code, label: getTranslatedLabel("PROPERTYTAX_ROADTYPE_" + item.code.toUpperCase(), localizationLabelsData) };
        });

      dispatch(setFieldProperty("propertyAddress", "roadType", "dropDownData", roadTypeData));

      const thanaData =
        get(loadMdmsData, "PropertyTax.Thana") &&
        Object.values(get(loadMdmsData, "PropertyTax.Thana")).map((item, index) => {
          return {
            value: item.code,
            label: getTranslatedLabel(
              "PROPERTYTAX_THANA_" + tenantId.replace(".", "_").toUpperCase() + "_" + item.code.toUpperCase(),
              localizationLabelsData
            ),
          };
        });
      console.log("thanaData------->>>", thanaData);

      //let isRequired = process.env.REACT_APP_NAME === "Citizen"? false:true;
      let isRequired = true;

      if (window.location.href.includes("dataentry")) {
        isRequired = false;
      }

      dispatch(setFieldProperty("propertyAddress", "thanaType", "dropDownData", thanaData));
      dispatch(
        setFieldProperty(
          "propertyAddress",
          "thanaType",
          "label",
          get(state.form.prepareFormData, "Properties[0].propertyDetails[0].additionalDetails.thana", "")
        )
      );
      dispatch(
        setFieldProperty(
          "propertyAddress",
          "roadType",
          "label",
          get(state.form.prepareFormData, "Properties[0].propertyDetails[0].additionalDetails.roadType", "")
        )
      );
      dispatch(setFieldProperty("propertyAddress", "roadType", "required", isRequired));
      dispatch(setFieldProperty("propertyAddress", "thanaType", "required", isRequired));

      const PT = citiesByModule && citiesByModule.PT;
      if (PT) {
        const tenants = PT.tenants;
        const dd = tenants.reduce((dd, tenant) => {
          let selected = cities.find((city) => {
            return city.code === tenant.code;
          });
          const label = `TENANT_TENANTS_${selected.code.toUpperCase().replace(/[.]/g, "_")}`;
          dd.push({ label: getTranslatedLabel(label, localizationLabels), value: selected.code });
          return dd;
        }, []);
        dispatch(setFieldProperty("propertyAddress", "city", "dropDownData", sortBy(dd, ["label"])));
      }

      if (PT) {
        const tenants = PT.tenants;
        let found = tenants.find((city) => {
          return city.code === tenantId;
        });

        if (found) {
          const { cities } = state.common;
          let tenantInfo = cities.find((t) => {
            if (t.code == found.code) return t;
          });
          let cityName = tenantId;
          if (tenantInfo && tenantInfo.city && tenantInfo.city.name) cityName = tenantInfo.city.name;
          dispatch(handleFieldChange("propertyAddress", "city", tenantId));
          dispatch(prepareFormData("Properties[0].address.city", cityName));
        }
      }
      // const tenant = get(state, "form.propertyAddress.fields.city.value", null);
      // const mohallaDropDownData = get(state, "form.propertyAddress.fields.mohalla.dropDownData", []);

      // if (process.env.REACT_APP_NAME === "Citizen" && tenant && mohallaDropDownData.length == 0) {
      //   const dataFetchConfig = {
      //     url: "egov-location/location/v11/boundarys/_search?hierarchyTypeCode=REVENUE&boundaryType=Locality",
      //     action: "",
      //     queryParams: [
      //       {
      //         key: "tenantId",
      //         value: tenant,
      //       },
      //     ],
      //     requestBody: {},
      //     isDependent: true,
      //     hierarchyType: "REVENUE",
      //   };
      //   fetchDropdownData(dispatch, dataFetchConfig, "propertyAddress", "mohalla", state, true);
      // }
      set(action, "form.fields.city.required", true);
      set(action, "form.fields.pincode.disabled", false);
      return action;
    } catch (e) {
      console.log(e);
      return action;
    }
  },
  action: "",
  redirectionRoute: "",
  saveUrl: "",
  isFormValid: false,
};

export default formConfig;
