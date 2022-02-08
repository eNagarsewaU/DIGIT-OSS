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
    filters: { limit: 10, offset: 0, services: ["PT.CREATE", "PT.MUTATION", "PT.UPDATE"] },
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

  return <EmployeeModuleCard {...propsForModuleCard} />;
};

const customize = (props) => {
    window.Digit.ComponentRegistryService.setComponent("PTCard", PTCard);
    return <PTCard {...props}/>
  };

  export default customize;
