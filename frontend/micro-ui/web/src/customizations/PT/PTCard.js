import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { EmployeeModuleCard, PropertyHouse } from "@egovernments/digit-ui-react-components";
import { ptAccess } from "../utils/index"

const PTCard = () => {

  const { t } = useTranslation();

  const [total, setTotal] = useState("-");
  const { data, isLoading, isFetching, isSuccess } = window.Digit.Hooks.useNewInboxGeneral({
    tenantId: window.Digit.ULBService.getCurrentTenantId(),
    ModuleCode: "PT",
    filters: { limit: 10, offset: 0, services: ["PT.CREATE", "PT.MUTATION"] },
    config: {
      select: (data) => {
        return data?.totalCount || "-";
      },
      enabled: ptAccess(),
    },
  });

  useEffect(() => {
    if (!isFetching && isSuccess) setTotal(data);
  }, [isFetching]);

  if (!ptAccess()) {
    return null;
  }

  const propsForModuleCard = {
    Icon: <PropertyHouse />,
    moduleName: t("ES_TITLE_PROPERTY_TAX"),
    kpis: [
      {
        count: total,
        label: t("ES_TITLE_INBOX"),
        link: "/employee/pt-mutation/propertySearch",
      },
    ],
    links: [
      {
        count: isLoading ? "-" : total,
        label: t("ES_COMMON_INBOX"),
        link: "/employee/pt-mutation/propertySearch",
      },
    ],
  };

  const PT_CEMP = window.Digit.UserService.hasAccess(["PTCEMP"]) || false;
  if (PT_CEMP && !propsForModuleCard.links?.[1]) {
    propsForModuleCard.links.push({
      label: t("ES_TITLE_NEW_REGISTRATION"),
      link: "/employee/property-tax/assessment-form-dataentry",
    });
  }

  return ptAccess() && <div className="employeeCard card-home">
  <div className="complaint-links-container">
      <div className="header">
          <span className="text">PT</span>
          <span className="logo">
{/*               {Icon}
 */}          </span>
      </div>
      <div className="body" style={{ margin: "0px", padding: "0px" }}>
          <div className="flex-fit">
              <h3>PT </h3>
          </div>
          <div className="links-wrapper">
          <span className="link"><a href="/employee/property-tax">{t("PROPERTYTAX_HOME")}</a></span>
          <span className="link"><a href="/employee/pt-mutation/propertySearch">{t("PT_PAY_PROPERTYTAX")}</a></span>
          <span className="link"><a href="/employee/property-tax/my-properties">{t("PT_MY_PROPERTIES")}</a></span>
          <span className="link"><a href="/employee/pt-mutation/my-applications">{t("PT_MY_APPLICATIONS")}</a></span>
          </div>
      </div>
  </div>
</div>;
};

const customize = (props) => {
    window.Digit.ComponentRegistryService.setComponent("PTCard", PTCard);
    return <PTCard {...props}/>
  };

  export default customize;
