
import {
    getCommonCard
  } from "egov-ui-framework/ui-config/screens/specs/utils";
  import { searchPropertyDetails } from "./publicMutationMethods";
 // import "./index.css"
  
  const publicSearchTabs = getCommonCard({
    searchPropertyDetails
    // header: getCommonSubHeader(
    //   { labelName: "Capture Payment", labelKey: "NOC_PAYMENT_CAP_PMT" },
    //   {
    //     style: {
    //       marginBottom: "8px"
    //     }
    //   }
    // ),
/*     tabSection: {
      uiFramework: "custom-containers-local",
      moduleName: "egov-pt",
      componentPath: "CustomTabContainer",
      props: {
        className:"ptTabs",
        tabs: [
          {
            tabButton: {labelName:"Search Property", labelKey:"PT_SEARCH_PROPERTY"},
            tabContent: { searchPropertyDetails }
          },
           {
            tabButton: {labelName: "Search application", labelKey:"PT_SEARCH_APPLICATION"},
            tabContent: { searchApplicationDetails }
          } 
        ]
      },
      type: "array"
    } */
  });
  
  export default publicSearchTabs;
  